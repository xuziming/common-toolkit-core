package com.simon.credit.toolkit.concurrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

import sun.misc.Unsafe;

/**
 * AQS(AbstractQueuedSynchronizer)
 * <pre>
 * AQS是Java中管理“锁”的抽象类，锁的许多公共方法都在这个类中实现。
 * AQS是独占锁(如ReentrantLock)和共享锁(如Semaphore)的公共父类
 * </pre>
 */
@SuppressWarnings("restriction")
public abstract class MyAbstractQueuedSynchronizer extends MyAbstractOwnableSynchronizer implements Serializable {
	private static final long serialVersionUID = 7373984972572414691L;

	private static final Unsafe unsafe;
	private static final long stateOffset;
	private static final long headOffset;
	private static final long tailOffset;
	private static final long waitStatusOffset;
	private static final long nextOffset;

	/** 用纳秒旋转而不是用超时的挂起，粗略估计足以在非常短的超时时间内提高响应性 */
	static final long spinForTimeoutThreshold = 1000L;

	static {
		try {
			unsafe = UnsafeToolkits.getUnsafe();

			stateOffset = unsafe.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("state"));
			headOffset  = unsafe.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("head"));
			tailOffset  = unsafe.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("tail"));

			waitStatusOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
			nextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	/** 同步状态(初始状态默认为0) */
	private volatile int state = 0;// 锁状态，加锁成功则为1，重入+1，解锁则为0

	/** 等待队列头，懒加载 ，除了初始化，它只通过方法setHead()修改。注意：若头存在，原来的头它的等待状态不保证被取消 */
	private transient volatile Node head;// 队首

	/** 等待队列尾巴，懒加载，仅通过方法enq()以添加新的等待节点修改 */
	private transient volatile Node tail;// 队尾

	protected MyAbstractQueuedSynchronizer() {}

	protected final boolean compareAndSetState(int expect, int update) {
		return unsafe.compareAndSwapInt(this, stateOffset, expect, update);// 1、AQS.state
	}

	private final boolean compareAndSetHead(Node update) {
		return unsafe.compareAndSwapObject(this, headOffset, null, update);// 2、AQS.head
	}

	private final boolean compareAndSetTail(Node expect, Node update) {
		return unsafe.compareAndSwapObject(this, tailOffset, expect, update);// 3、AQS.tail
	}

	private static final boolean compareAndSetWaitStatus(Node node, int expect, int update) {
		return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);// 4、Node.waitStatus
	}

	private static final boolean compareAndSetNext(Node node, Node expect, Node update) {
		return unsafe.compareAndSwapObject(node, nextOffset, expect, update);// 5、Node.next
	}

	protected final int getState() {
		return state;
	}

	protected final void setState(int newState) {
		state = newState;
	}

	/** 对当前队列和你指定的mode(Node.EXCLUSIVE和Node.SHARED)放入到node节点中，并加入队尾， 返回要插入的node，即新node节点 */
	private Node addWaiter(Node mode) {
		// 由于AQS队列当中的元素类型为Node，故而需要把当前线程tc封装成为一个Node对象,下文我们叫做nc
		Node newTail = new Node(Thread.currentThread(), mode);
		// tail为对尾(原来的队尾)，赋值给pred
		Node originalTail = tail;
		// 判断pred是否为空，其实就是判断对尾是否有节点，其实只要队列被初始化了对尾肯定不为空，
	    // 假设队列里面只有一个元素，那么对尾和对首都是这个元素
	    // 换言之就是判断队列有没有初始化
	    // 上面我们说过代码执行到这里有两种情况，1、队列没有初始化和2、队列已经初始化了
		// [10] pred不等于空表示第二种情况，队列被初始化了，如果是第二种情况那比较简单
		// [11] 直接把当前线程封装的nc的上一个节点设置成为pred即原来的对尾
		// 继而把pred的下一个节点设置为当nc，这个nc自己成为队尾了
		if (originalTail != null) {// 加入队列末端(新节点成为队尾)
			// 直接把当前线程封装的nc的上一个节点设置成为pred即原来的对尾，对应[10]行的注释
			newTail.prev = originalTail;// 新队尾的前一个节点为原队尾
			// 这里需要CAS，因为防止多个线程加锁，确保nc入队的时候是原子操作
			if (compareAndSetTail(originalTail, newTail)) {
				// 继而把pred的下一个节点设置为当nc，这个nc自己成为对尾了 对应第[11]行注释
				originalTail.next = newTail;// 原队尾的下一个节点为新队尾
				// 然后把nc返回出去，方法结束
				return newTail;
			}
		}
		// 如果上面的if不成了就会执行到这里，表示第一种情况队列并没有初始化
		enq(newTail);// 如果队列为空，就用enq()创建队列进行添加newTail
		return newTail;// 返回nc
	}

	/**
	 * 将newTail插入队尾，返回原队尾
	 **/
	private Node enq(final Node newTail) {
		for (;;) {
			// 队尾复制给t，上面已经说过队列没有初始化，
	        // 故而第一次循环t==null（因为是死循环，因此强调第一次，后面可能还有第二次、第三次，每次t的情况肯定不同）
			// 队尾复制给t，由于第二次循环，故而tail==nn，即new出来的那个node
			Node originalTail = tail;// 读取原队尾节点
			// 第一次循环成了成立，第二次循环不成立
			if (originalTail == null) {// 如果为空则必须初始化队头
				// new Node就是实例化一个Node对象下文我们称为nn，
	            // 调用无参构造方法实例化出来的Node里面三个属性都为null，可以关联Node类的结构，
	            // compareAndSetHead入队操作；把这个nn设置成为队列当中的头部，cas防止多线程、确保原子操作；
	            // 记住这个时候队列当中只有一个，即nn
				if (compareAndSetHead(new Node())) {
					// 这个时候AQS队列当中只有一个元素，即头部=nn，所以为了确保队列的完整，设置头部等于尾部，即nn即是头也是尾
	                // 然后第一次循环结束；接着执行第二次循环，第二次循环代码我写在了下面，接着往下看就行
					tail = head;
				}
			} else {// 进行双向关联，先向前关联
				// 不成立故而进入else
	            // 首先把nc，当前线程所代表的的node的上一个节点改变为nn，因为这个时候nc需要入队，入队的时候需要把关系维护好
	            // 所谓的维护关系就是形成链表，nc的上一个节点只能为nn，这个很好理解
				newTail.prev = originalTail;// 新队尾的前一个节点为原队尾
				// 入队操作--把nc设置为对尾，对首是nn，
				if (compareAndSetTail(originalTail, newTail)) {
					// 上面我们说了为了维护关系把nc的上一个节点设置为nn
	                // 这里同样为了维护关系，把nn的下一个节点设置为nc
					originalTail.next = newTail;// 原队尾的下一个节点为新队尾
					// 然后返回t，即nn，死循环结束，enq(node);方法返回
	                // 这个返回其实就是为了终止循环，返回出去的t，没有意义
					return originalTail;
				}
			}
		}
	}

	/**
	 * 设置node到队列的头，去排队，返回当前node，头节点只是一个节点，里面没有线程。
	 * 注意：就算你添加的node是全的，进入队头线程就会被置为空
	 */
	private void setHead(Node node) {
		head = node;
		node.thread = null;
		node.prev = null;
	}

	/** 唤醒node的下一个，如果下一个存在就唤醒，如果下一个不存在就从后往前找到离你传的node最近的被阻塞的node唤醒 */
	private void unparkSuccessor(Node node) {
		int nodeWaitStatus = node.waitStatus;// 查看node当前的等待状态
		if (nodeWaitStatus < 0) {
			// 预置当前结点的状态为0，表示后续结点即将被唤醒
			compareAndSetWaitStatus(node, nodeWaitStatus, 0);
		}

		Node notifyNode = node.next;// 后继节点

		// 正常情况下额，会直接唤醒后继结点
		// 但是如果后继结点处于1:CANCELLED状态时(说明被取消了)，会从队尾开始，向前查找第一个未被CANCELLED的结点
		if (notifyNode == null || notifyNode.waitStatus > 0) {// >0表示线程被取消，被取消后从尾部找离node近的唤醒
			notifyNode = null;
			// 从tail开始向前查找是为了考虑并发入队(enq)的情况
			for (Node currentTail = tail; currentTail != null && currentTail != node; currentTail = currentTail.prev) {
				if (currentTail.waitStatus <= 0) {// 从后往前查找需要唤醒的线程, 找到离node最近的需要唤醒信号的节点
					notifyNode = currentTail;
				}
			}
		}

		if (notifyNode != null) {
			LockSupport.unpark(notifyNode.thread);// 唤醒结点
		}
	}

	/**
	 * 释放共享模式，从头部开始的第一个需要信号（被唤醒）的node释放，确保传播。
	 * 注意：对于互斥模式，如果需要被唤醒，相当于调用unpackSuccessor()的头部
	 */
	private void doReleaseShared() {
		for (;;) {
			Node originalHead = head;// 转换到头部
			if (originalHead != null && originalHead != tail) {// 如果节点大于1个，一直循环
				int nodeWaitStatus = originalHead.waitStatus;// 获取头部状态
				if (nodeWaitStatus == Node.SIGNAL) {// 如果头部是刚好需要信号（唤醒）
					// 将头结点的等待状态置为0，表示将要唤醒后继结点
					if (!compareAndSetWaitStatus(originalHead, Node.SIGNAL, 0)) {
						continue;// loop to recheck cases
					}
					unparkSuccessor(originalHead);// 唤醒后继节点
				} else if (nodeWaitStatus == 0 && !compareAndSetWaitStatus(originalHead, 0, Node.PROPAGATE)) {
					continue;// 成功的 CAS，如果成功则转为传播模式，继续循环。
				}
			}
			if (originalHead == head) {// 只有head，返回
				break;
			}
		}
	}

	/**
	 * 设置队列头部，并且检查后继节点是否处在共享模式的的阻塞中，并释放后继阻塞的node节点。
	 * @param node      为将要设置为头node
	 * @param propagate 试图在共享模式下获取对象状态(传播状态)
	 */
	private void setHeadAndPropagate(Node node, int propagate) {
		Node originalHead = head;// 先记录老head节点
		// 将当前结点设置为头结点
		setHead(node);

		// 判断是否需要唤醒后继结点
		if (propagate > 0 || originalHead == null || originalHead.waitStatus < 0) {
			Node nextNode = node.next;
			if (nextNode == null || nextNode.isShared()) {// nextNode==null说明只有head，或者是共享模式
				doReleaseShared();// 释放并唤醒后继结点
			}
		}
	}

	/** 将传入的节点，取消，并组成新的链，并跳过取消的前驱node，如果直到头节点，那么就唤醒node的下一个阻塞node */
	private void cancelAcquire(Node node) {
		if (node == null) {
			return;
		}
		node.thread = null;
		Node predNode = node.prev;
		while (predNode.waitStatus > 0) {// 跳过取消的前驱node
			node.prev = predNode = predNode.prev;
		}
		Node predNext = predNode.next;
		node.waitStatus = Node.CANCELLED;// 设置为取消
		if (node == tail && compareAndSetTail(node, predNode)) {// 若为尾部，则置为空
			compareAndSetNext(predNode, predNext, null);
		} else {
			// 只要pred的不为头节点和处于阻塞状态
			if (isCancelNode(predNode)) {
				Node next = node.next;
				if (next != null && next.waitStatus <= 0) {
					compareAndSetNext(predNode, predNext, next);// pred链接node取消后的node
				}
			} else {
				unparkSuccessor(node);// 为头唤醒node的下一个阻塞node
			}
			node.next = node;// help GC
		}
	}

	private boolean isCancelNode(Node predNode) {
		if (predNode == head) {
			return false;
		}

		int predNodeWaitStatus = predNode.waitStatus;
		if (predNodeWaitStatus != Node.SIGNAL) {
			if (predNodeWaitStatus > 0) {
				return false;
			}
			if (!compareAndSetWaitStatus(predNode, predNodeWaitStatus, Node.SIGNAL)) {
				return false;
			}
		}

		if (predNode.thread == null) {
			return false;
		}
		return true;
	}

	/**
	 * 如果node的前驱时信号状态则返回true，否则返回false，
	 * 且在返回false时，将他的前驱置为信号状态或阻塞状态
	 */
	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
		int predNodeWaitStatus = pred.waitStatus;// 前驱结点的状态
		// SIGNAL:后续结点需要被唤醒
		// (这个状态说明当前结点的前驱将来会来唤醒我，我可以安心地被阻塞)
		if (predNodeWaitStatus == Node.SIGNAL) {
			return true;
		}

		// 1(CANCELLED):取消(说明前驱结点(线程)因意外被中断或取消，需要将其从等待队列中移除)
		if (predNodeWaitStatus > 0) {
			do {
				node.prev = pred = pred.prev;
			} while (pred.waitStatus > 0);// 查找它的前驱直到它处于<0时
			pred.next = node;
		} else {
			// 对于独占功能来说，这里表示当前结点等待状态为初始状态0
			// 后继结点入队时，会将前驱结点的状态更新为SIGNAL
			compareAndSetWaitStatus(pred, predNodeWaitStatus, Node.SIGNAL);
		}
		return false;
	}

	/** 设置当前运行的线程中断flag */
	static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	/** 阻塞当前node，并查看中断状态并清除中断flag */
	private final boolean parkAndCheckInterrupt() {
		LockSupport.park(this);// 阻塞线程
		return Thread.interrupted();
	}

	/**
	 * 如果代码能执行到这里说tc需要排队，需要排队有两种情况，换而言之，代码能够执行到这里有2种情况：
	 * 1、tf持有了锁，并没有释放，所以tc来加锁的时候需要排队，但这个时候—队列并没有初始化
	 * 2、tn(无所谓哪个线程，反正就是一个线程)持有了锁，那么由于加锁tn!=tf(tf是属于第一种情况，我们现在不考虑tf了)，
	 *    所以队列是一定被初始化了的，tc来加锁，那么队列当中有人在排队，故而他也去排队
	 * 返回：等待中的是否被中断，被中断返回true，没有被中断则返回false */
	final boolean acquireQueued(final Node node, int acquires) {
		boolean failed = true;
		try {
			// interrupted表示在队列的调度中, 当前线程在休眠时，有没有被中断过
			boolean interrupted = false;
			for (;;) {
				// 获取上一个节点, node是当前线程对应的节点, 这里就意味着获取上一个等待锁的线程
				final Node prevNode = node.predecessor();// 获取nc的上一个节点，有两种情况；1、上一个节点为头部；2上一个节点不为头部
				// 如果nc的上一个节点为头部，则表示nc为队列当中的第二个元素，为队列当中的第一个排队的Node；
	            // 这里的第一和第二不冲突；我上文有解释；
	            // 如果nc为队列当中的第二个元素，第一个排队的则调用tryAcquire去尝试加锁---关于tryAcquire看上面的分析
	            // 只有nc为第二个元素；第一个排队的情况下才会尝试加锁，其他情况直接去park了，
	            // 因为第一个排队的执行到这里的时候需要看看持有有锁的线程有没有释放锁，释放了就轮到我了，就不park了
	            // 有人会疑惑说开始调用tryAcquire加锁失败了（需要排队），这里为什么还要进行tryAcquire不是重复了吗？
	            // 其实不然，因为第一次tryAcquire判断是否需要排队，如果需要排队，那么我就入队；
	            // 当我入队之后我发觉前面那个人就是第一个，持有锁的那个，那么我不死心，再次问问前面那个人搞完没有
	            // 如果他搞完了，我就不park，接着他搞我自己的事；如果他没有搞完，那么我则在队列当中去park，等待别人叫我
	            // 但是如果我去排队，发觉前面那个人在睡觉，前面那个人都在睡觉，那么我也睡觉把---------------好好理解一下
				if (prevNode == head && tryAcquire(acquires)) {
					// 使用prevNode==head表示当前线程前面的线程已经得到执行, 来保证锁的公平性。 
					// 如果当前线程是因为“线程被中断”而唤醒, 那么显然就不是公平了
					// 能够执行到这里表示我来加锁的时候，锁被持有了，我去排队，进到队列当中的时候发觉我前面那个人没有park，
	                // 前面那个人就是当前持有锁的那个人，那么我问问他搞完没有
	                // 能够进到这个里面就表示前面那个人搞完了；所以这里能执行到的几率比较小；但是在高并发的世界中这种情况真的需要考虑
	                // 如果我前面那个人搞完了，我nc得到锁了，那么前面那个人直接出队列，我自己则是对首；这行代码就是设置自己为队首
					setHead(node);
					// 这里的P代表的就是刚刚搞完事的那个人，由于他的事情搞完了，要出队；怎么出队？把链表关系删除
					prevNode.next = null;// help GC
					// 设置表示---记住记加锁成功的时候为false
					failed = false;
					// 返回false；为什么返回false？
					return interrupted;
				}
				// 进到这里分为两种情况
	            // 1、nc的上一个节点不是头部，说白了，就是我去排队了，但是我上一个人不是队列第一个
	            // 2、第二种情况，我去排队了，发觉上一个节点是第一个，但是他还在搞事没有释放锁
	            // 不管哪种情况这个时候我都需要park，park之前我需要把上一个节点的状态改成park状态
	            // 这里比较难以理解为什么我需要去改变上一个节点的park状态呢？每个node都有一个状态，默认为0，表示无状态
	            // -1表示在park；当时不能自己把自己改成-1状态？为什么呢？因为你得确定你自己park了才是能改为-1；
	            // 不然你自己改成自己为-1；但是改完之后你没有park那不就骗人？
	            // 你对外宣布自己是单身状态，但是实际和刘宏斌私下约会；这有点坑人
	            // 所以只能先park；在改状态；但是问题你自己都park了；完全释放CPU资源了，故而没有办法执行任何代码了，
	            // 所以只能别人来改；故而可以看到每次都是自己的后一个节点把自己改成-1状态
	            // 关于shouldParkAfterFailedAcquire这个方法的源码下次博客继续讲吧
				if (shouldParkAfterFailedAcquire(prevNode, node) && parkAndCheckInterrupt()) {// 改上一个节点的状态成功之后；自己park；到此加锁过程说完了
					interrupted = true;
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node);
			}
		}
	}

	/** 获取独占锁的可中断模式 */
	private void doAcquireInterruptibly(int acquires) throws InterruptedException {
		// 创建"当前线程"的Node节点，且Node中记录的锁是"独占锁"类型；并将该节点添加到CLH队列末尾。
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				// 获取上一个节点。
				// 如果上一节点是CLH队列的表头，则"尝试获取独占锁"。
				final Node prevNode = node.predecessor();
				if (prevNode == head && tryAcquire(acquires)) {
					setHead(node);
					prevNode.next = null;// help GC
					failed = false;
					return;
				}
				// (上一节点不是CLH队列的表头)当前线程一直等待，直到获取到独占锁。
				// 如果线程在等待过程中被中断过，则再次中断该线程(还原之前的中断状态)
				if (shouldParkAfterFailedAcquire(prevNode, node) && parkAndCheckInterrupt()) {
					throw new InterruptedException();
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node); // 将当前线程node清除
			}
		}
	}

	private boolean doAcquireNanos(int acquires, long nanosTimeout) throws InterruptedException {
		if (nanosTimeout <= 0L) {
			return false;
		}
		final long deadline = System.nanoTime() + nanosTimeout;
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(acquires)) {
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return true;
				}
				nanosTimeout = deadline - System.nanoTime();
				if (nanosTimeout <= 0L) {
					return false;
				}
				if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold) {
					LockSupport.parkNanos(this, nanosTimeout);
				}
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node);
			}
		}
	}

	private void doAcquireShared(int acquires) {
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			boolean interrupted = false;
			for (;;) {
				final Node prevNode = node.predecessor();
				if (prevNode == head) {
					int remaining = tryAcquireShared(acquires);
					if (remaining >= 0) {
						setHeadAndPropagate(node, remaining);
						prevNode.next = null;// help GC
						if (interrupted) {
							selfInterrupt();
						}
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(prevNode, node) && parkAndCheckInterrupt()) {
					interrupted = true;
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node);
			}
		}
	}

	private void doAcquireSharedInterruptibly(int acquires) throws InterruptedException {
		final Node node = addWaiter(Node.SHARED);// 包装成共享结点，插入等待队列
		boolean failed = true;
		try {
			for (;;) {// 自旋阻塞线程或尝试获取锁
				final Node prevNode = node.predecessor();
				if (prevNode == head) {
					// 尝试获取锁，返回值的含义为：
					// 小于0:获取失败; 等于0:获取成功; 大于0:获取成功,且后继争用线程可能成功;
					int remaining = tryAcquireShared(acquires);// 尝试获取锁
					if (remaining >= 0) {// 大于等于0表示获取成功
						setHeadAndPropagate(node, remaining);
						prevNode.next = null;// help GC
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(prevNode, node) &&
					/* 检查是否需要阻塞当前结点 */parkAndCheckInterrupt()) {
					throw new InterruptedException();
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node);
			}
		}
	}

	private boolean doAcquireSharedNanos(int acquires, long nanosTimeout) throws InterruptedException {
		if (nanosTimeout <= 0L) {
			return false;
		}
		final long deadline = System.nanoTime() + nanosTimeout;
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			for (;;) {
				final Node prevNode = node.predecessor();
				if (prevNode == head) {
					int remaining = tryAcquireShared(acquires);
					if (remaining >= 0) {
						setHeadAndPropagate(node, remaining);
						prevNode.next = null;// help GC
						failed = false;
						return true;
					}
				}
				nanosTimeout = deadline - System.nanoTime();
				if (nanosTimeout <= 0L) {
					return false;
				}
				if (shouldParkAfterFailedAcquire(prevNode, node) && nanosTimeout > spinForTimeoutThreshold) {
					LockSupport.parkNanos(this, nanosTimeout);
				}
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node);
			}
		}
	}

	// Main exported methods,这些方法需要被重写，这里只定义了接口

    /** 尝试获取独占锁 */
	protected boolean tryAcquire(int acquires) {
		throw new UnsupportedOperationException();
	}

	/** 尝试释放独占锁 */
	protected boolean tryRelease(int releases) {
		throw new UnsupportedOperationException();
	}

	/** 尝试获取共享锁 */
	protected int tryAcquireShared(int acquires) {
		throw new UnsupportedOperationException();
	}

	/** 尝试释放共享锁 */
	protected boolean tryReleaseShared(int releases) {
		throw new UnsupportedOperationException();
	}

	/** 在排它模式下，状态是否被占用 */
	protected boolean isHeldExclusively() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取独占锁：先尝试获取，锁没有被占用，则直接获取，否则加入队列尾部，阻塞等待直到获取锁
	 * 总结：
	 * addWaiter方法就是让nc入队-并且维护队列的链表关系，但是由于情况复杂做了不同处理
	 * ---主要针对队列是否有初始化，没有初始化则new一个新的Node nn作为对首，nn里面的线程为null
	 **/
	public final void acquire(int acquires) {
		// 先尝试获取锁，若锁没有被占用，那么返回true，否则返回false
		// 若没有获取到锁，则加入尾部，并阻塞该锁，如果被中断，则执行下面的selfInterrupt自我中断
		// tryAcquire(arg)尝试加锁，如果加锁失败则会调用acquireQueued方法加入队列去排队，如果加锁成功则不会调用
	    // 加入队列之后线程会立马park，等到解锁之后会被unpark，醒来之后判断自己是否被打断了
		if (!tryAcquire(acquires) && acquireQueued(addWaiter(Node.EXCLUSIVE), acquires)) {
			selfInterrupt();
		}
	}

	public final void acquireInterruptibly(int acquires) throws InterruptedException {
		if (Thread.interrupted()) {// 查看当前线程是否有中断flag，有的话，清除并抛出中断异常
			throw new InterruptedException();
		}
		if (!tryAcquire(acquires)) {// 尝试获取锁，如果失败，则调doAcquireInterruptibly()独占锁的可中断模式
			doAcquireInterruptibly(acquires);
		}
	}

	public final boolean tryAcquireNanos(int acquires, long nanosTimeout) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		return tryAcquire(acquires) || doAcquireNanos(acquires, nanosTimeout);
	}

	/** 试着释放当前线程持有的独占锁并唤醒后继节点 */
	public final boolean release(int acquires) {
		if (tryRelease(acquires)) {// 尝试释放当前线程持有的锁
			Node currentHead = head;
			if (currentHead != null && currentHead.waitStatus != 0) {
				unparkSuccessor(currentHead);// 唤醒后继节点
			}
			return true;
		}
		return false;
	}

	/** 获取共享锁，先尝试获取，<0说明获取不到，再次获取 */
	public final void acquireShared(int acquires) {
		if (tryAcquireShared(acquires) < 0) {
			doAcquireShared(acquires);
		}
	}

	public final void acquireSharedInterruptibly(int acquires) throws InterruptedException {
		//  Thread.interrupted()实现代码：currentThread().isInterrupted(true);
		if (Thread.interrupted()) {// 响应线程中断
			throw new InterruptedException();
		}

		if (tryAcquireShared(acquires) < 0) {// 尝试获取锁，小于0表示获取失败
			doAcquireSharedInterruptibly(acquires);// 加入等待队列
		}
	}

	public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		return tryAcquireShared(arg) >= 0 || doAcquireSharedNanos(arg, nanosTimeout);
	}

	public final boolean releaseShared(int arg) {
		if (tryReleaseShared(arg)) {// 尝试一次释放锁
			doReleaseShared();
			return true;
		}
		return false;
	}

	// Queue inspection methods

	public final boolean hasQueuedThreads() {
		return head != tail;
	}

	public final boolean hasContended() {
		return head != null;
	}

	public final Thread getFirstQueuedThread() {
		// handle only fast path, else relay
		return (head == tail) ? null : fullGetFirstQueuedThread();
	}

	private Thread fullGetFirstQueuedThread() {
		Node currentHead, nextNode;
		Thread nextNodeThread;
		if (((currentHead = head) != null && (nextNode = currentHead.next) != null && nextNode.prev == head && (nextNodeThread = nextNode.thread) != null)
			|| ((currentHead = head) != null && (nextNode = currentHead.next) != null && nextNode.prev == head && (nextNodeThread = nextNode.thread) != null)) {
			return nextNodeThread;
		}

		Node currentTail = tail;
		Thread firstThread = null;
		while (currentTail != null && currentTail != head) {
			Thread currentTailThread = currentTail.thread;
			if (currentTailThread != null) {
				firstThread = currentTailThread;
			}
			currentTail = currentTail.prev;
		}
		return firstThread;
	}

	public final boolean isQueued(Thread thread) {
		if (thread == null) {
			throw new NullPointerException();
		}
		for (Node prevNode = tail; prevNode != null; prevNode = prevNode.prev) {
			if (prevNode.thread == thread) {
				return true;
			}
		}
		return false;
	}

	final boolean apparentlyFirstQueuedIsExclusive() {
		Node currentHead, nextNode;
		return (currentHead = head) != null && (nextNode = currentHead.next) != null && !nextNode.isShared() && nextNode.thread != null;
	}

	/**
	 * 判断是否需要排队
	 * 这里需要记住一点，整个方法如果最后返回false，则去加锁；如果返回true，则不加锁，因为这个方法被取反了。
	 */
	public final boolean hasQueuedPredecessors() {
		Node currentTail = tail; // Read fields in reverse initialization order
		Node currentHead = head;
		Node nextNode;
		/**
	     * 下面提到的所有不需要排队，并不是字面意义，我实在想不出什么词语来描述这个“不需要排队”；不需要排队有两种情况
	     * 一：队列没有初始化，不需要排队，不需要排队，不需要排队；直接去加锁，但是可能会失败；为什么会失败呢？
	     * 假设两个线程同时来lock，都看到队列没有初始化，都认为不需要排队，都去进行CAS修改计数器；有一个必然失败
	     * 比如t1先拿到锁，那么另外一个t2则会CAS失败，这个时候t2就会去初始化队列，并排队
	     *
	     * 二：队列被初始化了，但是tc过来加锁，发觉队列当中第一个排队的就是自己；比如重入；
	     * 那么什么叫做第一个排队的呢？下面解释了，很重要往下看；
	     * 这个时候他也不需要排队，不需要排队，不需要排队；为什么不需要排对？
	     * 因为队列当中第一个排队的线程他会去尝试获取一下锁，因为有可能这个时候持有锁锁的那个线程可能释放了锁；
	     * 如果释放了就直接获取锁执行。但是如果没有释放他就会去排队，
	     * 所以这里的不需要排队，不是真的不需要排队
	     *
	     * h != t 判断首不等于尾这里要分三种情况
	     * 1、队列没有初始化，也就是第一个线程tf来加锁的时候那么这个时候队列没有初始化，
	     * h和t都是null，那么这个时候判断不等于则不成立（false）那么由于是&&运算后面的就不会走了，
	     * 直接返回false表示不需要排队，而前面又是取反（if (!hasQueuedPredecessors()），所以会直接去cas加锁。
	     * ----------第一种情况总结：队列没有初始化没人排队，那么我直接不排队，直接上锁；合情合理、有理有据令人信服；
	     * 好比你去火车站买票，服务员都闲的蛋疼，整个队列都没有形成；没人排队，你直接过去交钱拿票
	     *
	     *
	     * 2、队列被初始化了，后面会分析队列初始化的流程，如果队列被初始化那么h!=t则成立；（不绝对，还有第3中情况）
	     * h != t 返回true；但是由于是&&运算，故而代码还需要进行后续的判断
	     * （有人可能会疑问，比如队列初始化了；里面只有一个数据，那么头和尾都是同一个怎么会成立呢？
	     * 其实这是第3种情况--对头等于对尾；但是这里先不考虑，我们假设现在队列里面有大于1个数据）
	     * 大于1个数据则成立;继续判断把h.next赋值给s；s有是对头的下一个Node，
	     * 这个时候s则表示他是队列当中参与排队的线程而且是排在最前面的；
	     * 为什么是s最前面不是h嘛？诚然h是队列里面的第一个，但是不是排队的第一个；下文有详细解释
	     * 因为h也就是对头对应的Node对象或者线程他是持有锁的，但是不参与排队；
	     * 这个很好理解，比如你去买车票，你如果是第一个这个时候售票员已经在给你服务了，你不算排队，你后面的才算排队；
	     * 队列里面的h是不参与排队的这点一定要明白；参考下面关于队列初始化的解释；
	     * 因为h要么是虚拟出来的节点，要么是持有锁的节点；什么时候是虚拟的呢？什么时候是持有锁的节点呢？下文分析
	     * 然后判断s是否等于空，其实就是判断队列里面是否只有一个数据；
	     * 假设队列大于1个，那么肯定不成立（s==null---->false），因为大于一个Node的时候h.next肯定不为空；
	     * 由于是||运算如果返回false，还要判断s.thread != Thread.currentThread()；这里又分为两种情况
	     *        2.1 s.thread != Thread.currentThread() 返回true，就是当前线程不等于在排队的第一个线程s；
	     *              那么这个时候整体结果就是h!=t：true; （s==null false || s.thread != Thread.currentThread() true  最后true）
	     *              结果： true && true 方法最终放回true，所以需要去排队
	     *              其实这样符合情理，试想一下买火车票，队列不为空，有人在排队；
	     *              而且第一个排队的人和现在来参与竞争的人不是同一个，那么你就乖乖去排队
	     *        2.2 s.thread != Thread.currentThread() 返回false 表示当前来参与竞争锁的线程和第一个排队的线程是同一个线程
	     *             这个时候整体结果就是h!=t---->true; （s==null false || s.thread != Thread.currentThread() false-----> 最后false）
	     *            结果：true && false 方法最终放回false，所以不需要去排队
	     *            不需要排队则调用 compareAndSetState(0, acquires) 去改变计数器尝试上锁；
	     *            这里又分为两种情况（日了狗了这一行代码；有同学课后反应说子路老师老师老是说这个AQS难，
	     *            你现在仔细看看这一行代码的意义，真的不简单的）
	     *             2.2.1  第一种情况加锁成功？有人会问为什么会成功啊，如这个时候h也就是持有锁的那个线程执行完了
	     *                      释放锁了，那么肯定成功啊；成功则执行 setExclusiveOwnerThread(current); 然后返回true 自己看代码
	     *             2.2.2  第二种情况加锁失败？有人会问为什么会失败啊。假如这个时候h也就是持有锁的那个线程没执行完
	     *                       没释放锁，那么肯定失败啊；失败则直接返回false，不会进else if（else if是相对于 if (c == 0)的）
	     *                      那么如果失败怎么办呢？后面分析；
	     *
	     *----------第二种情况总结，如果队列被初始化了，而且至少有一个人在排队那么自己也去排队；但是有个插曲；
	     * ---------他会去看看那个第一个排队的人是不是自己，如果是自己那么他就去尝试加锁；尝试看看锁有没有释放
	     *----------也合情合理，好比你去买票，如果有人排队，那么你乖乖排队，但是你会去看第一个排队的人是不是你女朋友；
	     *----------如果是你女朋友就相当于是你自己（这里实在想不出现实世界关于重入的例子，只能用男女朋友来替代）；
	     * ---------你就叫你女朋友看看售票员有没有搞完，有没有轮到你女朋友，因为你女朋友是第一个排队的
	     * 疑问：比如如果在在排队，那么他是park状态，如果是park状态，自己怎么还可能重入啊。
	     * 希望有同学可以想出来为什么和我讨论一下，作为一个菜逼，希望有人教教我
	     * 
	     * 
	     * 3、队列被初始化了，但是里面只有一个数据；什么情况下才会出现这种情况呢？ts加锁的时候里面就只有一个数据？
	     * 其实不是，因为队列初始化的时候会虚拟一个h作为头结点，tc=ts作为第一个排队的节点；tf为持有锁的节点
	     * 为什么这么做呢？因为AQS认为h永远是不排队的，假设你不虚拟节点出来那么ts就是h，
	     *  而ts其实需要排队的，因为这个时候tf可能没有执行完，还持有着锁，ts得不到锁，故而他需要排队；
	     * 那么为什么要虚拟为什么ts不直接排在tf之后呢，上面已经时说明白了，tf来上锁的时候队列都没有，他不进队列，
	     * 故而ts无法排在tf之后，只能虚拟一个thread=null的节点出来（Node对象当中的thread为null）；
	     * 那么问题来了；究竟什么时候会出现队列当中只有一个数据呢？假设原队列里面有5个人在排队，当前面4个都执行完了
	     * 轮到第五个线程得到锁的时候；他会把自己设置成为头部，而尾部又没有，故而队列当中只有一个h就是第五个
	     * 至于为什么需要把自己设置成头部；其实已经解释了，因为这个时候五个线程已经不排队了，他拿到锁了；
	     * 所以他不参与排队，故而需要设置成为h；即头部；所以这个时间内，队列当中只有一个节点
	     * 关于加锁成功后把自己设置成为头部的源码，后面会解析到；继续第三种情况的代码分析
	     * 记得这个时候队列已经初始化了，但是只有一个数据，并且这个数据所代表的线程是持有锁
	     * h != t false 由于后面是&&运算，故而返回false可以不参与运算，整个方法返回false；不需要排队
	     *
	     *
	     *-------------第三种情况总结：如果队列当中只有一个节点，而这种情况我们分析了，
	     *-------------这个节点就是当前持有锁的那个节点，故而我不需要排队，进行cas；尝试加锁
	     *-------------这是AQS的设计原理，他会判断你入队之前，队列里面有没有人排队；
	     *-------------有没有人排队分两种情况；队列没有初始化，不需要排队
	     *-------------队列初始化了，按时只有一个节点，也是没人排队，自己先也不排队
	     *-------------只要认定自己不需要排队，则先尝试加锁；加锁失败之后再排队；
	     *-------------再一次解释了不需要排队这个词的歧义性
	     *-------------如果加锁失败了，在去park，下文有详细解释这样设计源码和原因
	     *-------------如果持有锁的线程释放了锁，那么我能成功上锁
	     **/
		return currentHead != currentTail && ((nextNode = currentHead.next) == null || nextNode.thread != Thread.currentThread());
	}

	public final int getQueueLength() {
		int length = 0;
		for (Node prevNode = tail; prevNode != null; prevNode = prevNode.prev) {
			if (prevNode.thread != null) {
				++length;
			}
		}
		return length;
	}

	public final Collection<Thread> getQueuedThreads() {
		List<Thread> queuedThreads = new ArrayList<Thread>();
		for (Node prevNode = tail; prevNode != null; prevNode = prevNode.prev) {
			Thread thread = prevNode.thread;
			if (thread != null) {
				queuedThreads.add(thread);
			}
		}
		return queuedThreads;
	}

	public final Collection<Thread> getExclusiveQueuedThreads() {
		List<Thread> exclusiveQueuedThreads = new ArrayList<Thread>(8);
		for (Node prevNode = tail; prevNode != null; prevNode = prevNode.prev) {
			if (!prevNode.isShared()) {
				Thread thread = prevNode.thread;
				if (thread != null) {
					exclusiveQueuedThreads.add(thread);
				}
			}
		}
		return exclusiveQueuedThreads;
	}

	public final Collection<Thread> getSharedQueuedThreads() {
		List<Thread> sharedQueuedThreads = new ArrayList<Thread>();
		for (Node prevNode = tail; prevNode != null; prevNode = prevNode.prev) {
			if (prevNode.isShared()) {
				Thread thread = prevNode.thread;
				if (thread != null) {
					sharedQueuedThreads.add(thread);
				}
			}
		}
		return sharedQueuedThreads;
	}

	public String toString() {
		int currentState = getState();
		String queue = hasQueuedThreads() ? "non" : "";
		return super.toString() + "[State = " + currentState + ", " + queue + "empty queue]";
	}

	// Internal support methods for Conditions

	final boolean isOnSyncQueue(Node node) {
		if (node.waitStatus == Node.CONDITION || node.prev == null) {
			return false;
		}
		if (node.next != null) {// If has successor, it must be on queue
			return true;
		}
		return findNodeFromTail(node);
	}

	private boolean findNodeFromTail(Node node) {
		Node currentTail = tail;
		for (;;) {
			if (currentTail == node) {
				return true;
			}
			if (currentTail == null) {
				return false;
			}
			currentTail = currentTail.prev;
		}
	}

	final boolean transferForSignal(Node node) {
		if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			return false;
		}

		Node originalTail = enq(node);
		int originalTailWaitStatus = originalTail.waitStatus;
		if (originalTailWaitStatus > 0 || !compareAndSetWaitStatus(originalTail, originalTailWaitStatus, Node.SIGNAL)) {
			LockSupport.unpark(node.thread);
		}
		return true;
	}

	final boolean transferAfterCancelledWait(Node node) {
		if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			enq(node);
			return true;
		}
		while (!isOnSyncQueue(node)) {
			Thread.yield();
		}
		return false;
	}

	final int fullyRelease(Node node) {
		boolean failed = true;
		try {
			int savedState = getState();
			if (release(savedState)) {
				failed = false;
				return savedState;
			} else {
				throw new IllegalMonitorStateException();
			}
		} finally {
			if (failed) {
				node.waitStatus = Node.CANCELLED;
			}
		}
	}

	// Instrumentation methods for conditions

	public final boolean owns(ConditionObject condition) {
		return condition.isOwnedBy(this);
	}

	public final boolean hasWaiters(ConditionObject condition) {
		if (!owns(condition)) {
			throw new IllegalArgumentException("Not owner");
		}
		return condition.hasWaiters();
	}

	public final int getWaitQueueLength(ConditionObject condition) {
		if (!owns(condition)) {
			throw new IllegalArgumentException("Not owner");
		}
		return condition.getWaitQueueLength();
	}

	public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
		if (!owns(condition)) {
			throw new IllegalArgumentException("Not owner");
		}
		return condition.getWaitingThreads();
	}

	static final class Node {
		/** Marker to indicate a node is waiting in shared mode */
		static final Node SHARED = new Node();
		/** Marker to indicate a node is waiting in exclusive mode */
		static final Node EXCLUSIVE = null;

		/** waitStatus value to indicate thread has cancelled */
		static final int CANCELLED =  1;
		/** waitStatus value to indicate successor's thread needs unparking */
		static final int SIGNAL    = -1;
		/** waitStatus value to indicate thread is waiting on condition */
		static final int CONDITION = -2;
		/** waitStatus value to indicate the next acquireShared should unconditionally propagate */
		static final int PROPAGATE = -3;
		/**
		 *<pre>结点状态
		 * Node定义: Node结点是对每一个等待获取资源的线程的封装，其包含了需要同步的线程本身及其等待状态，
		 * 如: 是否被阻塞、是否等待唤醒、是否已经被取消等。变量waitStatus则表示当前Node结点的等待状态。
		 * 等待状态共有5种取值，分别是: CANCELLED、SIGNAL、CONDITION、PROPAGATE、0
		 * CANCELLED( 1)：表示当前结点已取消调度。当timeout或被中断(响应中断的情况下)，
		 *                会触发变更为此状态，进入该状态后的结点将不会再发生变化。
		 *    SIGNAL(-1)：表示后继结点在等待当前结点唤醒。后继结点入队时，会将前继结点的状态更新为SIGNAL。
		 * CONDITION(-2)：表示结点等待在Condition上，当其它线程调用了Condition的signal()方法后，
		 *                Condition状态的结点将从等待队列转移到同步队列中，等待获取同步锁。
		 * PROPAGATE(-3)：共享模式下，前继结点不仅会唤醒其后继结点，同时也可能会唤醒后继的后继结点。
		 *             0：新结点入队时的默认等待状态。
		 * 注意：负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常。
		 * </pre>
		 */
		volatile int waitStatus = 0;

		volatile Node prev;

		volatile Node next;

		volatile Thread thread;

		Node nextWaiter;

		final boolean isShared() {
			return nextWaiter == SHARED;
		}

		final Node predecessor() throws NullPointerException {
			Node prevNode = prev;// 读取当前节点的前一个节点
			if (prevNode != null) {
				return prevNode;
			}
			throw new NullPointerException();
		}

		Node() {
			// Used to establish initial head or SHARED marker
		}

		Node(Thread thread, Node mode) {// Used by addWaiter
			this.thread = thread;
			this.nextWaiter = mode;
		}

		Node(Thread thread, int waitStatus) {// Used by Condition
			this.thread = thread;
			this.waitStatus = waitStatus;
		}
	}

	public class ConditionObject implements Condition, Serializable {
		private static final long serialVersionUID = 1173984872572414699L;
		/** First node of condition queue. */
		private transient Node firstWaiter;
		/** Last node of condition queue. */
		private transient Node lastWaiter;

		public ConditionObject() {}

		private Node addConditionWaiter() {
			Node currentLastWaiter = lastWaiter;
			// If lastWaiter is cancelled, clean out.
			if (currentLastWaiter != null && currentLastWaiter.waitStatus != Node.CONDITION) {
				unlinkCancelledWaiters();
				currentLastWaiter = lastWaiter;
			}
			Node node = new Node(Thread.currentThread(), Node.CONDITION);
			if (currentLastWaiter == null) {
				firstWaiter = node;
			} else {
				currentLastWaiter.nextWaiter = node;
			}
			lastWaiter = node;
			return node;
		}

		private void doSignal(Node first) {
			do {
				if ((firstWaiter = first.nextWaiter) == null) {
					lastWaiter = null;
				}
				first.nextWaiter = null;
			} while (!transferForSignal(first) && (first = firstWaiter) != null);
		}

		private void doSignalAll(Node first) {
			lastWaiter = firstWaiter = null;
			do {
				Node next = first.nextWaiter;
				first.nextWaiter = null;
				transferForSignal(first);
				first = next;
			} while (first != null);
		}

		private void unlinkCancelledWaiters() {
			Node currentFirstWaiter = firstWaiter;
			Node trail = null;
			while (currentFirstWaiter != null) {
				Node next = currentFirstWaiter.nextWaiter;
				if (currentFirstWaiter.waitStatus != Node.CONDITION) {
					currentFirstWaiter.nextWaiter = null;
					if (trail == null) {
						firstWaiter = next;
					} else {
						trail.nextWaiter = next;
					}
					if (next == null) {
						lastWaiter = trail;
					}
				} else {
					trail = currentFirstWaiter;
				}
				currentFirstWaiter = next;
			}
		}

		public final void signal() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			Node first = firstWaiter;
			if (first != null) {
				doSignal(first);
			}
		}

		public final void signalAll() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			Node first = firstWaiter;
			if (first != null) {
				doSignalAll(first);
			}
		}

		public final void awaitUninterruptibly() {
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			boolean interrupted = false;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if (Thread.interrupted()) {
					interrupted = true;
				}
			}
			if (acquireQueued(node, savedState) || interrupted) {
				selfInterrupt();
			}
		}

		/** Mode meaning to reinterrupt on exit from wait */
		private static final int REINTERRUPT = 1;
		/** Mode meaning to throw InterruptedException on exit from wait */
		private static final int THROW_IE = -1;

		private int checkInterruptWhileWaiting(Node node) {
			return Thread.interrupted() ? (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) : 0;
		}

		private void reportInterruptAfterWait(int interruptMode) throws InterruptedException {
			if (interruptMode == THROW_IE) {
				throw new InterruptedException();
			} else if (interruptMode == REINTERRUPT) {
				selfInterrupt();
			}
		}

		public final void await() throws InterruptedException {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
					break;
				}
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
				interruptMode = REINTERRUPT;
			}
			if (node.nextWaiter != null) {// clean up if cancelled
				unlinkCancelledWaiters();
			}
			if (interruptMode != 0) {
				reportInterruptAfterWait(interruptMode);
			}
		}

		public final long awaitNanos(long nanosTimeout) throws InterruptedException {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			final long deadline = System.nanoTime() + nanosTimeout;
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (nanosTimeout <= 0L) {
					transferAfterCancelledWait(node);
					break;
				}
				if (nanosTimeout >= spinForTimeoutThreshold) {
					LockSupport.parkNanos(this, nanosTimeout);
				}
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
					break;
				}
				nanosTimeout = deadline - System.nanoTime();
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
				interruptMode = REINTERRUPT;
			}
			if (node.nextWaiter != null) {
				unlinkCancelledWaiters();
			}
			if (interruptMode != 0) {
				reportInterruptAfterWait(interruptMode);
			}
			return deadline - System.nanoTime();
		}

		public final boolean awaitUntil(Date deadline) throws InterruptedException {
			long abstime = deadline.getTime();// 绝对时间-->deadline
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			boolean timedout = false;
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (System.currentTimeMillis() > abstime) {
					timedout = transferAfterCancelledWait(node);
					break;
				}
				LockSupport.parkUntil(this, abstime);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
					break;
				}
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
				interruptMode = REINTERRUPT;
			}
			if (node.nextWaiter != null) {
				unlinkCancelledWaiters();
			}
			if (interruptMode != 0) {
				reportInterruptAfterWait(interruptMode);
			}
			return !timedout;
		}

		public final boolean await(long time, TimeUnit unit) throws InterruptedException {
			long nanosTimeout = unit.toNanos(time);
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			final long deadline = System.nanoTime() + nanosTimeout;
			boolean timedout = false;
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (nanosTimeout <= 0L) {
					timedout = transferAfterCancelledWait(node);
					break;
				}
				if (nanosTimeout >= spinForTimeoutThreshold) {
					LockSupport.parkNanos(this, nanosTimeout);
				}
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
					break;
				}
				nanosTimeout = deadline - System.nanoTime();
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
				interruptMode = REINTERRUPT;
			}
			if (node.nextWaiter != null) {
				unlinkCancelledWaiters();
			}
			if (interruptMode != 0) {
				reportInterruptAfterWait(interruptMode);
			}
			return !timedout;
		}

		// support for instrumentation

		final boolean isOwnedBy(MyAbstractQueuedSynchronizer sync) {
			return sync == MyAbstractQueuedSynchronizer.this;
		}

		protected final boolean hasWaiters() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			for (Node currentWaiter = firstWaiter; currentWaiter != null; currentWaiter = currentWaiter.nextWaiter) {
				if (currentWaiter.waitStatus == Node.CONDITION) {
					return true;
				}
			}
			return false;
		}

		protected final int getWaitQueueLength() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			int length = 0;
			for (Node currentWaiter = firstWaiter; currentWaiter != null; currentWaiter = currentWaiter.nextWaiter) {
				if (currentWaiter.waitStatus == Node.CONDITION) {
					++length;
				}
			}
			return length;
		}

		protected final Collection<Thread> getWaitingThreads() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			List<Thread> waitingThreads = new ArrayList<Thread>(8);
			for (Node currentWaiter = firstWaiter; currentWaiter != null; currentWaiter = currentWaiter.nextWaiter) {
				if (currentWaiter.waitStatus == Node.CONDITION) {
					Thread waiterThread = currentWaiter.thread;
					if (waiterThread != null) {
						waitingThreads.add(waiterThread);
					}
				}
			}
			return waitingThreads;
		}
	}

}