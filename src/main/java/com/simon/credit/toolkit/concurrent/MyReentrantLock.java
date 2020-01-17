package com.simon.credit.toolkit.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 重入锁
 */
public class MyReentrantLock implements Lock, Serializable {
	private static final long serialVersionUID = 7373984872572414699L;

	private final Sync sync;

	public MyReentrantLock() {
		sync = new NonfairSync();
	}

	public MyReentrantLock(boolean fair) {
		sync = fair ? new FairSync() : new NonfairSync();
	}

	/**
	 * 加锁过程总结：
	 * 如果是第一个线程tf(thread first)，那么和队列无关，线程直接持有锁。并且也不会初始化队列，
	 * 如果接下来的线程都是交替执行，那么永远和AQS队列无关，都是直接线程持有锁，
	 * 如果发生了竞争，比如tf持有锁的过程中T2来lock，那么这个时候就会初始化AQS，
	 * 初始化AQS的时候会在队列的头部虚拟一个Thread为NULL的Node，
	 * 因为队列当中的head永远是持有锁的那个node（除了第一次会虚拟一个，其他时候都是持有锁的那个线程锁封装的node），
	 * 现在第一次的时候持有锁的是tf而tf不在队列当中所以虚拟了一个node节点，队列当中的除了head之外的所有的node都在park，
	 * 当tf释放锁之后unpark某个（基本是队列当中的第二个，为什么是第二个呢？前面说过head永远是持有锁的那个node，
	 * 当有时候也不会是第二个，比如第二个被cancel之后，至于为什么会被cancel，不在我们讨论范围之内，
	 * cancel的条件很苛刻，基本不会发生）node之后，node被唤醒，
	 * 假设node是t2，那么这个时候会首先把t2变成head（sethead），在sethead方法里面会把t2代表的node设置为head，
	 * 并且把node的Thread设置为null，为什么需要设置null？
	 * 其实原因很简单，现在t2已经拿到锁了，node就不要排队了，那么node对Thread的引用就没有意义了。
	 * 所以队列的head里面的Thread永远为null
	 */
	public void lock() {
		sync.lock();
	}

	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	public boolean tryLock() {
		return sync.nonfairTryAcquire(1);
	}

	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(timeout));
	}

	public void unlock() {
		sync.release(1);
	}

	public Condition newCondition() {
		return sync.newCondition();
	}

	public int getHoldCount() {
		return sync.getHoldCount();
	}

	public boolean isHeldByCurrentThread() {
		return sync.isHeldExclusively();
	}

	public boolean isLocked() {
		return sync.isLocked();
	}

	public final boolean isFair() {
		return sync instanceof FairSync;
	}

	protected Thread getOwner() {
		return sync.getOwner();
	}

	public final boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}

	public final boolean hasQueuedThread(Thread thread) {
		return sync.isQueued(thread);
	}

	public final int getQueueLength() {
		return sync.getQueueLength();
	}

	protected Collection<Thread> getQueuedThreads() {
		return sync.getQueuedThreads();
	}

	public boolean hasWaiters(Condition condition) {
		if (condition == null) {
			throw new NullPointerException();
		}
		if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
			throw new IllegalArgumentException("not owner");
		}
		return sync.hasWaiters((MyAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	public int getWaitQueueLength(Condition condition) {
		if (condition == null) {
			throw new NullPointerException();
		}
		if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
			throw new IllegalArgumentException("not owner");
		}
		return sync.getWaitQueueLength((MyAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	protected Collection<Thread> getWaitingThreads(Condition condition) {
		if (condition == null) {
			throw new NullPointerException();
		}
		if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
			throw new IllegalArgumentException("not owner");
		}
		return sync.getWaitingThreads((MyAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	public String toString() {
		Thread o = sync.getOwner();
		return super.toString() + ((o == null) ? "[Unlocked]" : "[Locked by thread " + o.getName() + "]");
	}

	abstract static class Sync extends MyAbstractQueuedSynchronizer {
		private static final long serialVersionUID = -5179523762034025860L;

		/**
		 * [上锁过程重点]
		 * 锁对象：其实就是ReentrantLock的实例对象。
		 * 自由状态：表示锁对象没有被别的线程持有，计数器为零。
		 * 计数器：在lock对象中有一个state字段用来记录上锁次数，比如：lock对象是自由状态则state为0，如果大于0则表示被线程持有了，重入时state大于1。
		 * waitStatus：仅仅是一个状态而已；ws是一个过渡状态，在不同方法里面判断ws的状态做不同的处理，所以ws=0有其存在的必要性。
		 * tail：队列的队尾， head：队列的对首，ts：第二个给lock加锁的线程， tf：第一个给lock加锁的线程，tc：当前给lock加锁的线程。
		 * tl：最后一个加锁的线程，tn：随便某个线程。
		 * 当然这些线程有可能重复，比如：第一次加锁时，tf=tc=tl=tn
		 * 节点Node：就是内部类Node的对象，里面封装了线程，所以，从某种意义上说：node就等于一个线程。
		 */
		abstract void lock();

		final boolean nonfairTryAcquire(int acquires) {
			final Thread current = Thread.currentThread();
			int c = getState();
			if (c == 0) {
				if (compareAndSetState(0, acquires)) {
					setExclusiveOwnerThread(current);
					return true;
				}
			} else if (current == getExclusiveOwnerThread()) {
				int nextc = c + acquires;
				if (nextc < 0) {// overflow
					throw new Error("Maximum lock count exceeded");
				}
				setState(nextc);
				return true;
			}
			return false;
		}

		protected final boolean tryRelease(int releases) {
			int c = getState() - releases;
			if (Thread.currentThread() != getExclusiveOwnerThread()) {
				throw new IllegalMonitorStateException();
			}
			boolean free = false;
			if (c == 0) {
				free = true;
				setExclusiveOwnerThread(null);
			}
			setState(c);
			return free;
		}

		protected final boolean isHeldExclusively() {
			// While we must in general read state before owner,
			// we don't need to do so to check if current thread is owner
			return getExclusiveOwnerThread() == Thread.currentThread();
		}

		final ConditionObject newCondition() {
			return new ConditionObject();
		}

		// Methods relayed from outer class

		final Thread getOwner() {
			return getState() == 0 ? null : getExclusiveOwnerThread();
		}

		final int getHoldCount() {
			return isHeldExclusively() ? getState() : 0;
		}

		final boolean isLocked() {
			return getState() != 0;
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
			setState(0); // reset to unlocked state
		}
	}

	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = 7316153563782823691L;

		final void lock() {
			if (compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
			} else {
				acquire(1);
			}
		}

		protected final boolean tryAcquire(int acquires) {
			return nonfairTryAcquire(acquires);
		}
	}

	/**
	 * 公平锁的上锁是必须判断自己是不是需要排队；
	 * 而非公平锁是直接进行CAS修改计数器看能不能加锁成功，如果加锁不成功则乖乖排队(调用acquire)；
	 * 所以不管公平还是非公平，只要进到了AQS队列当中，那么它就会排队；
	 * 记住这点：一朝排队，永远排队。
	 */
	static final class FairSync extends Sync {
		private static final long serialVersionUID = -3000897897090466540L;

		/**
		 * [上锁过程重点]
		 * 锁对象：其实就是ReentrantLock的实例对象。
		 * 自由状态：表示锁对象没有被别的线程持有，计数器为零。
		 * 计数器：在lock对象中有一个state字段用来记录上锁次数，比如：lock对象是自由状态则state为0，如果大于0则表示被线程持有了，重入时state大于1。
		 * waitStatus：仅仅是一个状态而已；ws是一个过渡状态，在不同方法里面判断ws的状态做不同的处理，所以ws=0有其存在的必要性。
		 * tail：队列的队尾， head：队列的对首，ts：第二个给lock加锁的线程， tf：第一个给lock加锁的线程，tc：当前给lock加锁的线程。
		 * tl：最后一个加锁的线程，tn：随便某个线程。
		 * 当然这些线程有可能重复，比如：第一次加锁时，tf=tc=tl=tn
		 * 节点Node：就是内部类Node的对象，里面封装了线程，所以，从某种意义上说：node就等于一个线程。
		 */
		final void lock() {
			acquire(1);// 1:标识加锁成功之后改变的值
		}

		protected final boolean tryAcquire(int acquires) {
			// 获取当前线程
			final Thread current = Thread.currentThread();
			// 获取lock对象的上锁状态，如果锁是自由状态则=0，如果被上锁则为1，大于1表示重入
			int c = getState();
			if (c == 0) {// 没人占用锁--->我要去上锁----1、锁是自由状态
				// hasQueuedPredecessors，判断自己是否需要排队这个方法比较复杂，下面我会单独介绍，
				// 如果不需要排队，则进行CAS尝试加锁；如果加锁成功，则把当前线程设置为拥有锁的线程，继而返回true
				if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
					// 设置当前线程为拥有锁的线程，方便后面判断是不是重入(判断当前线程是否是拥有锁的线程)
					setExclusiveOwnerThread(current);
					return true;
				}
			}
			// 如果c不等于0，而且当前线程不是拥有锁的线程，则不会进else if，直接返回false，加锁失败。
		    // 如果c不等于0，但是当前线程就是拥有锁的线程，则表示这是一次重入，那么直接把状态+1，表示重入次数+1。
		    // 这里也侧面说明了ReentrantLock是可以重入的，因为若是重入也返回true，也能lock成功。
			else if (current == getExclusiveOwnerThread()) {
				int nextc = c + acquires;
				if (nextc < 0) {
					throw new Error("Maximum lock count exceeded");
				}
				setState(nextc);
				return true;
			}
			return false;
		}
	}

}