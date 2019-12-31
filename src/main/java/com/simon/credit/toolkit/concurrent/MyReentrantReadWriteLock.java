package com.simon.credit.toolkit.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@SuppressWarnings("restriction")
public class MyReentrantReadWriteLock implements ReadWriteLock, Serializable {
	private static final long serialVersionUID = -944225623350753046L;

	private final MyReentrantReadWriteLock.ReadLock readerLock;
	private final MyReentrantReadWriteLock.WriteLock writerLock;
	final Sync sync;

	public MyReentrantReadWriteLock() {
		this(false);// 默认为非公平锁
	}

	/**
	 * 创建可重入读写锁
	 * @param fair true:公平锁, false:非公平锁
	 */
	public MyReentrantReadWriteLock(boolean fair) {
		sync = fair ? new FairSync() : new NonfairSync();
		readerLock = new ReadLock(this);
		writerLock = new WriteLock(this);
	}

	public MyReentrantReadWriteLock.WriteLock writeLock() {
		return writerLock;
	}

	public MyReentrantReadWriteLock.ReadLock readLock() {
		return readerLock;
	}

	abstract static class Sync extends MyAbstractQueuedSynchronizer {
		private static final long serialVersionUID = 6317671515068378041L;

		static final int SHARED_SHIFT = 16;
		static final int SHARED_UNIT = (1 << SHARED_SHIFT);
		static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
		static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

		/** Returns the number of shared holds represented in count */
		static int sharedCount(int c) {
			return c >>> SHARED_SHIFT;
		}

		/** Returns the number of exclusive holds represented in count */
		static int exclusiveCount(int c) {
			return c & EXCLUSIVE_MASK;
		}

		static final class HoldCounter {
			int count = 0;
			// Use id, not reference, to avoid garbage retention
			final long tid = getThreadId(Thread.currentThread());
		}

		static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
			public HoldCounter initialValue() {
				return new HoldCounter();
			}
		}

		private transient ThreadLocalHoldCounter readHolds;

		private transient HoldCounter cachedHoldCounter;

		private transient Thread firstReader = null;
		private transient int firstReaderHoldCount;

		Sync() {
			readHolds = new ThreadLocalHoldCounter();
			setState(getState()); // ensures visibility of readHolds
		}

		abstract boolean readerShouldBlock();

		abstract boolean writerShouldBlock();

		protected final boolean tryRelease(int releases) {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			int nextc = getState() - releases;
			boolean free = exclusiveCount(nextc) == 0;
			if (free) {
				setExclusiveOwnerThread(null);
			}
			setState(nextc);
			return free;
		}

		protected final boolean tryAcquire(int acquires) {
			Thread current = Thread.currentThread();
			int c = getState();
			int w = exclusiveCount(c);
			if (c != 0) {
				// (Note: if c != 0 and w == 0 then shared count != 0)
				if (w == 0 || current != getExclusiveOwnerThread()) {
					return false;
				}
				if (w + exclusiveCount(acquires) > MAX_COUNT) {
					throw new Error("Maximum lock count exceeded");
				}
				// Reentrant acquire
				setState(c + acquires);
				return true;
			}
			if (writerShouldBlock() || !compareAndSetState(c, c + acquires)) {
				return false;
			}
			setExclusiveOwnerThread(current);
			return true;
		}

		protected final boolean tryReleaseShared(int unused) {
			Thread current = Thread.currentThread();
			if (firstReader == current) {
				// assert firstReaderHoldCount > 0;
				if (firstReaderHoldCount == 1) {
					firstReader = null;
				} else {
					firstReaderHoldCount--;
				}
			} else {
				HoldCounter rh = cachedHoldCounter;
				if (rh == null || rh.tid != getThreadId(current)) {
					rh = readHolds.get();
				}
				int count = rh.count;
				if (count <= 1) {
					readHolds.remove();
					if (count <= 0) {
						throw unmatchedUnlockException();
					}
				}
				--rh.count;
			}
			for (;;) {
				int c = getState();
				int nextc = c - SHARED_UNIT;
				if (compareAndSetState(c, nextc))
					// Releasing the read lock has no effect on readers,
					// but it may allow waiting writers to proceed if both read and write locks are now free.
					return nextc == 0;
			}
		}

		private IllegalMonitorStateException unmatchedUnlockException() {
			return new IllegalMonitorStateException("attempt to unlock read lock, not locked by current thread");
		}

		protected final int tryAcquireShared(int unused) {
			Thread current = Thread.currentThread();
			int c = getState();
			if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current) {
				return -1;
			}
			int r = sharedCount(c);
			if (!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) {
				if (r == 0) {
					firstReader = current;
					firstReaderHoldCount = 1;
				} else if (firstReader == current) {
					firstReaderHoldCount++;
				} else {
					HoldCounter rh = cachedHoldCounter;
					if (rh == null || rh.tid != getThreadId(current)) {
						cachedHoldCounter = rh = readHolds.get();
					} else if (rh.count == 0) {
						readHolds.set(rh);
					}
					rh.count++;
				}
				return 1;
			}
			return fullTryAcquireShared(current);
		}

		final int fullTryAcquireShared(Thread current) {
			HoldCounter rh = null;
			for (;;) {
				int c = getState();
				if (exclusiveCount(c) != 0) {
					if (getExclusiveOwnerThread() != current) {
						return -1;
					}
					// else we hold the exclusive lock; blocking here would cause deadlock.
				} else if (readerShouldBlock()) {
					// Make sure we're not acquiring read lock reentrantly
					if (firstReader == current) {
						// assert firstReaderHoldCount > 0;
					} else {
						if (rh == null) {
							rh = cachedHoldCounter;
							if (rh == null || rh.tid != getThreadId(current)) {
								rh = readHolds.get();
								if (rh.count == 0) {
									readHolds.remove();
								}
							}
						}
						if (rh.count == 0) {
							return -1;
						}
					}
				}
				if (sharedCount(c) == MAX_COUNT) {
					throw new Error("Maximum lock count exceeded");
				}
				if (compareAndSetState(c, c + SHARED_UNIT)) {
					if (sharedCount(c) == 0) {
						firstReader = current;
						firstReaderHoldCount = 1;
					} else if (firstReader == current) {
						firstReaderHoldCount++;
					} else {
						if (rh == null) {
							rh = cachedHoldCounter;
						}
						if (rh == null || rh.tid != getThreadId(current)) {
							rh = readHolds.get();
						} else if (rh.count == 0) {
							readHolds.set(rh);
						}
						rh.count++;
						cachedHoldCounter = rh; // cache for release
					}
					return 1;
				}
			}
		}

		final boolean tryWriteLock() {
			Thread current = Thread.currentThread();
			int c = getState();
			if (c != 0) {
				int w = exclusiveCount(c);
				if (w == 0 || current != getExclusiveOwnerThread())
					return false;
				if (w == MAX_COUNT) {
					throw new Error("Maximum lock count exceeded");
				}
			}
			if (!compareAndSetState(c, c + 1)) {
				return false;
			}
			setExclusiveOwnerThread(current);
			return true;
		}

		final boolean tryReadLock() {
			Thread current = Thread.currentThread();
			for (;;) {
				int c = getState();
				if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current) {
					return false;
				}
				int r = sharedCount(c);
				if (r == MAX_COUNT) {
					throw new Error("Maximum lock count exceeded");
				}
				if (compareAndSetState(c, c + SHARED_UNIT)) {
					if (r == 0) {
						firstReader = current;
						firstReaderHoldCount = 1;
					} else if (firstReader == current) {
						firstReaderHoldCount++;
					} else {
						HoldCounter rh = cachedHoldCounter;
						if (rh == null || rh.tid != getThreadId(current)) {
							cachedHoldCounter = rh = readHolds.get();
						} else if (rh.count == 0) {
							readHolds.set(rh);
						}
						rh.count++;
					}
					return true;
				}
			}
		}

		protected final boolean isHeldExclusively() {
			return getExclusiveOwnerThread() == Thread.currentThread();
		}

		final ConditionObject newCondition() {
			return new ConditionObject();
		}

		final Thread getOwner() {
			// Must read state before owner to ensure memory consistency
			return ((exclusiveCount(getState()) == 0) ? null : getExclusiveOwnerThread());
		}

		final int getReadLockCount() {
			return sharedCount(getState());
		}

		final boolean isWriteLocked() {
			return exclusiveCount(getState()) != 0;
		}

		final int getWriteHoldCount() {
			return isHeldExclusively() ? exclusiveCount(getState()) : 0;
		}

		final int getReadHoldCount() {
			if (getReadLockCount() == 0) {
				return 0;
			}

			Thread current = Thread.currentThread();
			if (firstReader == current) {
				return firstReaderHoldCount;
			}

			HoldCounter rh = cachedHoldCounter;
			if (rh != null && rh.tid == getThreadId(current)) {
				return rh.count;
			}

			int count = readHolds.get().count;
			if (count == 0) {
				readHolds.remove();
			}
			return count;
		}

		private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
			s.defaultReadObject();
			readHolds = new ThreadLocalHoldCounter();
			setState(0); // reset to unlocked state
		}

		final int getCount() {
			return getState();
		}
	}

	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = -8159625535654395037L;

		final boolean writerShouldBlock() {
			return false; // writers can always barge
		}

		final boolean readerShouldBlock() {
			return apparentlyFirstQueuedIsExclusive();
		}
	}

	static final class FairSync extends Sync {
		private static final long serialVersionUID = -2274990926593161451L;

		final boolean writerShouldBlock() {
			return hasQueuedPredecessors();
		}

		final boolean readerShouldBlock() {
			return hasQueuedPredecessors();
		}
	}

	public static class ReadLock implements Lock, java.io.Serializable {
		private static final long serialVersionUID = -5992448646407690164L;
		private final Sync sync;

		protected ReadLock(MyReentrantReadWriteLock lock) {
			sync = lock.sync;
		}

		public void lock() {
			sync.acquireShared(1);
		}

		public void lockInterruptibly() throws InterruptedException {
			sync.acquireSharedInterruptibly(1);
		}

		public boolean tryLock() {
			return sync.tryReadLock();
		}

		public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
			return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
		}

		public void unlock() {
			sync.releaseShared(1);
		}

		public Condition newCondition() {
			throw new UnsupportedOperationException();
		}

		public String toString() {
			int r = sync.getReadLockCount();
			return super.toString() + "[Read locks = " + r + "]";
		}
	}

	public static class WriteLock implements Lock, java.io.Serializable {
		private static final long serialVersionUID = -4992448646407690164L;
		private final Sync sync;

		protected WriteLock(MyReentrantReadWriteLock lock) {
			sync = lock.sync;
		}

		public void lock() {
			sync.acquire(1);
		}

		public void lockInterruptibly() throws InterruptedException {
			sync.acquireInterruptibly(1);
		}

		public boolean tryLock() {
			return sync.tryWriteLock();
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

		public String toString() {
			Thread o = sync.getOwner();
			return super.toString() + ((o == null) ? "[Unlocked]" : "[Locked by thread " + o.getName() + "]");
		}

		public boolean isHeldByCurrentThread() {
			return sync.isHeldExclusively();
		}

		public int getHoldCount() {
			return sync.getWriteHoldCount();
		}
	}

	public final boolean isFair() {
		return sync instanceof FairSync;
	}

	protected Thread getOwner() {
		return sync.getOwner();
	}

	public int getReadLockCount() {
		return sync.getReadLockCount();
	}

	public boolean isWriteLocked() {
		return sync.isWriteLocked();
	}

	public boolean isWriteLockedByCurrentThread() {
		return sync.isHeldExclusively();
	}

	public int getWriteHoldCount() {
		return sync.getWriteHoldCount();
	}

	public int getReadHoldCount() {
		return sync.getReadHoldCount();
	}

	protected Collection<Thread> getQueuedWriterThreads() {
		return sync.getExclusiveQueuedThreads();
	}

	protected Collection<Thread> getQueuedReaderThreads() {
		return sync.getSharedQueuedThreads();
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
		if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject)) {
			throw new IllegalArgumentException("not owner");
		}
		return sync.hasWaiters((MyAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	public int getWaitQueueLength(Condition condition) {
		if (condition == null) {
			throw new NullPointerException();
		}
		if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject)) {
			throw new IllegalArgumentException("not owner");
		}
		return sync.getWaitQueueLength((MyAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	protected Collection<Thread> getWaitingThreads(Condition condition) {
		if (condition == null) {
			throw new NullPointerException();
		}
		if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject)) {
			throw new IllegalArgumentException("not owner");
		}
		return sync.getWaitingThreads((MyAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	public String toString() {
		int c = sync.getCount();
		int w = Sync.exclusiveCount(c);
		int r = Sync.sharedCount(c);

		return super.toString() + "[Write locks = " + w + ", Read locks = " + r + "]";
	}

	static final long getThreadId(Thread thread) {
		return UNSAFE.getLongVolatile(thread, TID_OFFSET);
	}

	// Unsafe mechanics
	private static final sun.misc.Unsafe UNSAFE;
	private static final long TID_OFFSET;
	static {
		try {
			UNSAFE = UnsafeToolkits.getUnsafe();
			Class<?> tk = Thread.class;
			TID_OFFSET = UNSAFE.objectFieldOffset(tk.getDeclaredField("tid"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}