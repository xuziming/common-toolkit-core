package com.simon.credit.toolkit.concurrent;

import java.io.Serializable;
import java.lang.reflect.Field;
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
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");// Internal reference
			unsafeField.setAccessible(true);
			unsafe = (Unsafe) unsafeField.get(null);

			stateOffset = unsafe.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("state"));
			headOffset = unsafe.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("head"));
			tailOffset = unsafe.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("tail"));

			waitStatusOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
			nextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	/** 同步状态 */
	private volatile int state;

	/** 等待队列头，懒加载 ，除了初始化，它只通过方法setHead()修改。注意：若头存在，原来的头它的等待状态不保证被取消 */
	private transient volatile Node head;

	/** 等待队列尾巴，懒加载，仅通过方法enq()以添加新的等待节点修改 */
	private transient volatile Node tail;

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

	/** 将node插入队尾，返回插入的node的前一个(即：原队尾) */
	private Node enq(final Node node) {
		for (;;) {
			Node originalTail = tail;// originalTail指向tail节点(原队尾)
			if (originalTail == null) {// 如果为空则必须初始化队头
				if (compareAndSetHead(new Node())) {
					tail = head;
				}
			} else {// 进行双向关联，先向前关联
				node.prev = originalTail;// 要插入的node这个的前面指向originalTail
				if (compareAndSetTail(originalTail, node)) {
					originalTail.next = node;// originalTail的下一个指向要插入的node，进行双向关联
					return originalTail;
				}
			}
		}
	}

	/** 对当前队列和你指定的mode(Node.EXCLUSIVE和Node.SHARED)放入到node节点中，并加入队尾， 返回要插入的node，即新node节点 */
	private Node addWaiter(Node mode) {
		Node node = new Node(Thread.currentThread(), mode);
		Node pred = tail;// pred为队尾
		if (pred != null) {// 进行关联
			node.prev = pred;
			if (compareAndSetTail(pred, node)) {
				pred.next = node;
				return node;
			}
		}
		enq(node);// 如果队列为空，就用enq()创建队列进行添加node
		return node;
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

	/** 唤醒node它的下一个，如果下一个存在就唤醒，如果下个不存在就从后向前找到离你传的node最近的被阻塞的node唤醒 */
	private void unparkSuccessor(Node node) {
		int nodeWaitStatus = node.waitStatus;// 查看node当前的等待状态
		if (nodeWaitStatus < 0) {
			compareAndSetWaitStatus(node, nodeWaitStatus, 0);// 如果被阻塞，则置为0
		}

		Node notifyNode = node.next;// 遍历节点
		if (notifyNode == null || notifyNode.waitStatus > 0) {// >0表示线程被取消，被取消后从尾部找离node近的唤醒
			notifyNode = null;
			for (Node currentTail = tail; currentTail != null && currentTail != node; currentTail = currentTail.prev) {
				if (currentTail.waitStatus <= 0) {// 从后往前查找需要唤醒的线程, 找到离node最近的需要唤醒信号的节点
					notifyNode = currentTail;
				}
			}
		}

		if (notifyNode != null) {
			LockSupport.unpark(notifyNode.thread);// 唤醒
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
					if (!compareAndSetWaitStatus(originalHead, Node.SIGNAL, 0)) {// 如果比较一样，将状态置为0
						continue;// loop to recheck cases
					}
					unparkSuccessor(originalHead);// 成功，释放head后继节点
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
	 * @param propagate 试图在共享模式下获取对象状态
	 */
	private void setHeadAndPropagate(Node node, int propagate) {
		Node originalHead = head;// 为下面的检查进行记录老的head节点
		setHead(node);
		// 状态>0或者，老head为空或者，老head为阻塞,只要老head不是取消
		if (propagate > 0 || originalHead == null || originalHead.waitStatus < 0) {
			Node nextNode = node.next;
			if (nextNode == null || nextNode.isShared()) {// nextNode==null说明只有head，或者是共享模式
				doReleaseShared();// 释放共享模式的阻塞node
			}
		}
	}

	/** 将传入的节点，取消，并组成新的链，并跳过取消的前驱node，如果直到头节点，那么就唤醒node的下一个阻塞node */
	private void cancelAcquire(Node node) {
		if (node == null) {
			return;
		}
		node.thread = null;
		Node pred = node.prev;
		while (pred.waitStatus > 0) {// 跳过取消的前驱node
			node.prev = pred = pred.prev;
		}
		Node predNext = pred.next;
		node.waitStatus = Node.CANCELLED;// 设置为取消
		if (node == tail && compareAndSetTail(node, pred)) {// 若为尾部，则置为空
			compareAndSetNext(pred, predNext, null);
		} else {
			int ws;
			// 只要pred的不为头节点和处于阻塞状态
			if (pred != head && ((ws = pred.waitStatus) == Node.SIGNAL || 
					(ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) && pred.thread != null) {
				Node next = node.next;
				if (next != null && next.waitStatus <= 0) {
					compareAndSetNext(pred, predNext, next);// pred链接node取消后的node
				}
			} else {
				unparkSuccessor(node);// 为头唤醒node的下一个阻塞node
			}
			node.next = node;// help GC
		}
	}

	/** 如果node的前驱时信号状态则返回true，否则返回false，且在返回false时，将他的前驱置为信号状态或阻塞状态 */
	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
		int predNodeWaitStatus = pred.waitStatus;
		if (predNodeWaitStatus == Node.SIGNAL) {
			return true;
		}
		if (predNodeWaitStatus > 0) {
			do {
				node.prev = pred = pred.prev;
			} while (pred.waitStatus > 0);// 查找它的前驱直到它处于<0时
			pred.next = node;
		} else {// <0则置为信号(被唤醒)
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

	/** 返回：等待中的是否被中断，被中断返回true，没有被中断则返回false */
	final boolean acquireQueued(final Node node, int acquires) {
		boolean failed = true;
		try {
			// interrupted表示在队列的调度中, 当前线程在休眠时，有没有被中断过
			boolean interrupted = false;
			for (;;) {
				// 获取上一个节点, node是当前线程对应的节点, 这里就意味着获取上一个等待锁的线程
				final Node prevNode = node.predecessor();
				if (prevNode == head && tryAcquire(acquires)) {
					// 使用prevNode==head表示当前线程前面的线程已经得到执行, 来保证锁的公平性。 
					// 如果当前线程是因为“线程被中断”而唤醒, 那么显然就不是公平了
					setHead(node);
					prevNode.next = null;// help GC
					failed = false;
					return interrupted;// 只有在这里才能跳出死循环
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
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(prevNode, node) && parkAndCheckInterrupt()) {
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

	/** 获取独占锁：先尝试获取，锁没有被占用，则直接获取，加入队列尾部，阻塞等待直到获取锁 */
	public final void acquire(int acquires) {
		// 先尝试获取，锁没有被占用，则直接获取返回true，否则返回false
		// 如果没有获取锁，则加入尾部，并阻塞该锁，如果被中断，则执行下面的selfInterrupt自我中断
		if (!tryAcquire(acquires) && acquireQueued(addWaiter(Node.EXCLUSIVE), acquires)) {
			selfInterrupt();
		}
	}

	public final void acquireInterruptibly(int acquires) throws InterruptedException {
		if (Thread.interrupted()) {// 查看当前线程是否有中断flag，有的话，清除并抛出中断异常
			throw new InterruptedException();
		}
		if (!tryAcquire(acquires)) {// 尝试获取锁，如果失败，则调doAcquireInterruptibly独占锁的可中断模式
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
		if (tryRelease(acquires)) {// 试着释放当前线程持有的锁
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

	public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		if (tryAcquireShared(arg) < 0) {
			doAcquireSharedInterruptibly(arg);
		}
	}

	public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		return tryAcquireShared(arg) >= 0 || doAcquireSharedNanos(arg, nanosTimeout);
	}

	public final boolean releaseShared(int arg) {
		if (tryReleaseShared(arg)) {
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

	public final boolean hasQueuedPredecessors() {
		Node currentTail = tail; // Read fields in reverse initialization order
		Node currentHead = head;
		Node nextNode;
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
			Node prevNode = prev;
			if (prevNode == null) {
				throw new NullPointerException();
			} else {
				return prevNode;
			}
		}

		Node() {
			// Used to establish initial head or SHARED marker
		}

		Node(Thread thread, Node mode) { // Used by addWaiter
			this.nextWaiter = mode;
			this.thread = thread;
		}

		Node(Thread thread, int waitStatus) { // Used by Condition
			this.waitStatus = waitStatus;
			this.thread = thread;
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