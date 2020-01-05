package com.simon.credit.toolkit.concurrent;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyScheduledThreadPoolExecutor extends MyThreadPoolExecutor implements ScheduledExecutorService {

	/**
	 * False if should cancel/suppress periodic tasks on shutdown.
	 */
	private volatile boolean continueExistingPeriodicTasksAfterShutdown;

	/**
	 * False if should cancel non-periodic tasks on shutdown.
	 */
	private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

	/**
	 * True if ScheduledFutureTask.cancel should remove from queue
	 */
	private volatile boolean removeOnCancel = false;

	/**
	 * Sequence number to break scheduling ties, and in turn to guarantee FIFO order among tied entries.
	 */
	private static final AtomicLong sequencer = new AtomicLong();

	/**
	 * Returns current nanosecond time.
	 */
	final long now() {
		return System.nanoTime();
	}

	private class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

		/** Sequence number to break ties FIFO */
		private final long sequenceNumber;

		/** The time that the task is enabled to execute in nanoTime units */
		private long time;

		/**
         * Period in nanoseconds for repeating tasks. 
         * A positive value indicates fixed-rate execution. 
         * A negative value indicates fixed-delay execution. 
         * A value of 0 indicates a non-repeating task.
         */
		private final long period;

		/** The actual task to be re-enqueued by reExecutePeriodic */
		RunnableScheduledFuture<V> outerTask = this;

		/**
		 * Index into delay queue, to support faster cancellation.
		 */
		int heapIndex;

		/**
		 * Creates a one-shot action with given nanoTime-based trigger time.
		 */
		ScheduledFutureTask(Runnable task, V result, long nanoSeconds) {
			super(task, result);
			this.time = nanoSeconds;
			this.period = 0;
			this.sequenceNumber = sequencer.getAndIncrement();
		}

		/**
		 * Creates a periodic action with given nano time and period.
		 */
		ScheduledFutureTask(Runnable task, V result, long nanoSeconds, long period) {
			super(task, result);
			this.time = nanoSeconds;
			this.period = period;
			this.sequenceNumber = sequencer.getAndIncrement();
		}

		/**
		 * Creates a one-shot action with given nanoTime-based trigger time.
		 */
		ScheduledFutureTask(Callable<V> callable, long nanoSeconds) {
			super(callable);
			this.time = nanoSeconds;// 纳秒
			this.period = 0;
			this.sequenceNumber = sequencer.getAndIncrement();
		}

		public long getDelay(TimeUnit unit) {
			return unit.convert(time - now(), NANOSECONDS);// 转为纳秒
		}

		public int compareTo(Delayed other) {
			if (other == this) {// compare zero if same object
				return 0;
			}
			if (other instanceof ScheduledFutureTask) {
				ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
				long diff = time - x.time;
				if (diff < 0) {
					return -1;
				} else if (diff > 0) {
					return 1;
				} else if (sequenceNumber < x.sequenceNumber) {
					return -1;
				} else {
					return 1;
				}
			}
			long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
			return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
		}

		public boolean isPeriodic() {
			return period != 0;
		}

		private void setNextRunTime() {
			long p = period;
			if (p > 0) {
				time += p;
			} else {
				time = triggerTime(-p);
			}
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean cancelled = super.cancel(mayInterruptIfRunning);
			if (cancelled && removeOnCancel && heapIndex >= 0) {
				remove(this);
			}
			return cancelled;
		}

		public void run() {
			boolean periodic = isPeriodic();
			if (!canRunInCurrentRunState(periodic)) {
				cancel(false);
			} else if (!periodic) {
				ScheduledFutureTask.super.run();
			} else if (ScheduledFutureTask.super.runAndReset()) {
				setNextRunTime();
				reExecutePeriodic(outerTask);
			}
		}
	}

	boolean canRunInCurrentRunState(boolean periodic) {
		return isRunningOrShutdown(periodic ? 
			continueExistingPeriodicTasksAfterShutdown : executeExistingDelayedTasksAfterShutdown);
	}

	private void delayedExecute(RunnableScheduledFuture<?> task) {
		if (isShutdown()) {// 线程池运行状态判断
			reject(task);
		} else {
			super.getQueue().add(task);// 加入工作队列

			// 如果任务添加到队列之后，线程池状态变为非运行状态，
	        // 需要将任务从队列移除，同时通过任务的cancel()方法来取消任务
			if (isShutdown() && !canRunInCurrentRunState(task.isPeriodic()) && remove(task)) {
				task.cancel(false);
			} else {
				// 如果任务添加到队列之后，线程池状态是运行状态，需要提前启动线程
				ensurePrestart();
			}
		}
	}

	void reExecutePeriodic(RunnableScheduledFuture<?> task) {
		if (canRunInCurrentRunState(true)) {
			super.getQueue().add(task);
			if (!canRunInCurrentRunState(true) && remove(task)) {
				task.cancel(false);
			} else {
				ensurePrestart();
			}
		}
	}

	@Override
	protected final void onShutdown() {
		BlockingQueue<Runnable> currentWorkQueue = super.getQueue();
		boolean keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy();
		boolean keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy();
		if (!keepDelayed && !keepPeriodic) {
			for (Object task : currentWorkQueue.toArray()) {
				if (task instanceof RunnableScheduledFuture<?>) {
					((RunnableScheduledFuture<?>) task).cancel(false);
				}
			}
			currentWorkQueue.clear();
		} else {
			// Traverse snapshot to avoid iterator exceptions
			for (Object futureTask : currentWorkQueue.toArray()) {
				if (futureTask instanceof RunnableScheduledFuture) {
					RunnableScheduledFuture<?> task = (RunnableScheduledFuture<?>) futureTask;
					 // also remove if already cancelled
					if ((task.isPeriodic() ? !keepPeriodic : !keepDelayed) || task.isCancelled()) {
						if (currentWorkQueue.remove(task)) {
							task.cancel(false);
						}
					}
				}
			}
		}
		tryTerminate();
	}

	protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
		return task;
	}

	protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
		return task;
	}

	public MyScheduledThreadPoolExecutor(int corePoolSize) {
		super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue());
	}

	public MyScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
		super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory);
	}

	public MyScheduledThreadPoolExecutor(int corePoolSize, MyRejectedExecutionHandler handler) {
		super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), handler);
	}

	public MyScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, MyRejectedExecutionHandler handler) {
		super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory, handler);
	}

	/**
	 * Returns the trigger time of a delayed action.
	 */
	private long triggerTime(long delay, TimeUnit unit) {
		return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
	}

	/**
	 * Returns the trigger time of a delayed action.
	 */
	long triggerTime(long delay) {
		return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
	}

	private long overflowFree(long delay) {
		Delayed head = (Delayed) super.getQueue().peek();
		if (head != null) {
			long headDelay = head.getDelay(NANOSECONDS);
			if (headDelay < 0 && (delay - headDelay < 0)) {
				delay = Long.MAX_VALUE + headDelay;
			}
		}
		return delay;
	}

	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		if (command == null || unit == null) {
			throw new NullPointerException();
		}

		// 装饰为任务
		RunnableScheduledFuture<?> task = decorateTask(command,
			new ScheduledFutureTask<Void>(command, null, triggerTime(delay, unit)));

		delayedExecute(task);
		return task;
	}

	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		if (callable == null || unit == null) {
			throw new NullPointerException();
		}

		RunnableScheduledFuture<V> task = decorateTask(callable,
			new ScheduledFutureTask<V>(callable, triggerTime(delay, unit)));

		delayedExecute(task);
		return task;
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		if (command == null || unit == null) {
			throw new NullPointerException();
		}
		if (period <= 0) {
			throw new IllegalArgumentException();
		}

		ScheduledFutureTask<Void> futureTask = new ScheduledFutureTask<Void>(
			command, null, triggerTime(initialDelay, unit), unit.toNanos(period));

		RunnableScheduledFuture<Void> task = decorateTask(command, futureTask);
		futureTask.outerTask = task;
		delayedExecute(task);
		return task;
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		if (command == null || unit == null) {
			throw new NullPointerException();
		}
		if (delay <= 0) {
			throw new IllegalArgumentException();
		}

		ScheduledFutureTask<Void> futureTask = new ScheduledFutureTask<Void>(
			command, null, triggerTime(initialDelay, unit), unit.toNanos(-delay));

		RunnableScheduledFuture<Void> task = decorateTask(command, futureTask);
		futureTask.outerTask = task;
		delayedExecute(task);
		return task;
	}

	public void execute(Runnable command) {
		schedule(command, 0, NANOSECONDS);
	}

	// Override AbstractExecutorService methods

	public Future<?> submit(Runnable task) {
		return schedule(task, 0, NANOSECONDS);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return schedule(Executors.callable(task, result), 0, NANOSECONDS);
	}

	public <T> Future<T> submit(Callable<T> task) {
		return schedule(task, 0, NANOSECONDS);
	}

	public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
		continueExistingPeriodicTasksAfterShutdown = value;
		if (!value && isShutdown()) {
			onShutdown();
		}
	}

	public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
		return continueExistingPeriodicTasksAfterShutdown;
	}

	public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
		executeExistingDelayedTasksAfterShutdown = value;
		if (!value && isShutdown()) {
			onShutdown();
		}
	}

	public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
		return executeExistingDelayedTasksAfterShutdown;
	}

	public void setRemoveOnCancelPolicy(boolean value) {
		removeOnCancel = value;
	}

	public boolean getRemoveOnCancelPolicy() {
		return removeOnCancel;
	}

	public void shutdown() {
		super.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return super.shutdownNow();
	}

	public BlockingQueue<Runnable> getQueue() {
		return super.getQueue();
	}

	/**
	 * Specialized delay queue. To mesh with TPE declarations, this class must be declared 
	 * as a BlockingQueue<Runnable> even though it can only hold RunnableScheduledFutures.
	 * <pre>
     * A DelayedWorkQueue is based on a heap-based data structure like those in DelayQueue and PriorityQueue, 
     * except that every ScheduledFutureTask also records its index into the heap array. 
     * This eliminates the need to find a task upon cancellation, greatly speeding up removal (down from O(n) to O(log n)), 
     * and reducing garbage retention that would otherwise occur by waiting for the element to rise to top before clearing. 
     * But because the queue may also hold RunnableScheduledFutures that are not ScheduledFutureTasks,
     * we are not guaranteed to have such indices available, in which case we fall back to linear search. 
     * (We expect that most tasks will not be decorated, and that the faster cases will be much more common.)
     *
     * All heap operations must record index changes -- mainly within siftUp and siftDown. 
     * Upon removal, a task's heapIndex is set to -1. 
     * Note that ScheduledFutureTasks can appear at most once in the queue 
     * (this need not be true for other kinds of tasks or work queues), so are uniquely identified by heapIndex.
     * </pre>
	 */
	static class DelayedWorkQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {

		private static final int INITIAL_CAPACITY = 16;
		private RunnableScheduledFuture<?>[] queue = new RunnableScheduledFuture<?>[INITIAL_CAPACITY];
		private final ReentrantLock lock = new ReentrantLock();
		private int size = 0;

		private Thread leader = null;

		private final Condition available = lock.newCondition();

		private void setIndex(RunnableScheduledFuture<?> f, int idx) {
			if (f instanceof ScheduledFutureTask) {
				((ScheduledFutureTask<?>) f).heapIndex = idx;
			}
		}

		private void siftUp(int k, RunnableScheduledFuture<?> key) {
			while (k > 0) {
				int parent = (k - 1) >>> 1;
				RunnableScheduledFuture<?> task = queue[parent];
				if (key.compareTo(task) >= 0) {
					break;
				}
				queue[k] = task;
				setIndex(task, k);
				k = parent;
			}
			queue[k] = key;
			setIndex(key, k);
		}

		private void siftDown(int k, RunnableScheduledFuture<?> key) {
			int half = size >>> 1;
			while (k < half) {
				int child = (k << 1) + 1;
				RunnableScheduledFuture<?> task = queue[child];
				int right = child + 1;
				if (right < size && task.compareTo(queue[right]) > 0) {
					task = queue[child = right];
				}
				if (key.compareTo(task) <= 0) {
					break;
				}
				queue[k] = task;
				setIndex(task, k);
				k = child;
			}
			queue[k] = key;
			setIndex(key, k);
		}

		private void grow() {
			int oldCapacity = queue.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
			if (newCapacity < 0) {// overflow
				newCapacity = Integer.MAX_VALUE;
			}
			queue = Arrays.copyOf(queue, newCapacity);
		}

		private int indexOf(Object task) {
			if (task != null) {
				if (task instanceof ScheduledFutureTask) {
					int i = ((ScheduledFutureTask<?>) task).heapIndex;
					// Sanity check; x could conceivably be a ScheduledFutureTask from some other pool.
					if (i >= 0 && i < size && queue[i] == task) {
						return i;
					}
				} else {
					for (int i = 0; i < size; i++) {
						if (task.equals(queue[i])) {
							return i;
						}
					}
				}
			}
			return -1;
		}

		public boolean contains(Object task) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return indexOf(task) != -1;
			} finally {
				lock.unlock();
			}
		}

		public boolean remove(Object task) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = indexOf(task);
				if (i < 0) {
					return false;
				}

				setIndex(queue[i], -1);
				int s = --size;
				RunnableScheduledFuture<?> replacement = queue[s];
				queue[s] = null;
				if (s != i) {
					siftDown(i, replacement);
					if (queue[i] == replacement) {
						siftUp(i, replacement);
					}
				}
				return true;
			} finally {
				lock.unlock();
			}
		}

		public int size() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return size;
			} finally {
				lock.unlock();
			}
		}

		public boolean isEmpty() {
			return size() == 0;
		}

		public int remainingCapacity() {
			return Integer.MAX_VALUE;
		}

		public RunnableScheduledFuture<?> peek() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return queue[0];
			} finally {
				lock.unlock();
			}
		}

		public boolean offer(Runnable task) {
			if (task == null) {
				throw new NullPointerException();
			}
			RunnableScheduledFuture<?> futureTask = (RunnableScheduledFuture<?>) task;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = size;
				if (i >= queue.length) {
					grow();
				}
				size = i + 1;
				if (i == 0) {
					queue[0] = futureTask;
					setIndex(futureTask, 0);
				} else {
					siftUp(i, futureTask);
				}
				if (queue[0] == futureTask) {
					leader = null;
					available.signal();
				}
			} finally {
				lock.unlock();
			}
			return true;
		}

		public void put(Runnable task) {
			offer(task);
		}

		public boolean add(Runnable task) {
			return offer(task);
		}

		public boolean offer(Runnable task, long timeout, TimeUnit unit) {
			return offer(task);
		}

		private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> task) {
			int s = --size;
			RunnableScheduledFuture<?> x = queue[s];
			queue[s] = null;
			if (s != 0) {
				siftDown(0, x);
			}
			setIndex(task, -1);
			return task;
		}

		public RunnableScheduledFuture<?> poll() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				RunnableScheduledFuture<?> first = queue[0];
				if (first == null || first.getDelay(NANOSECONDS) > 0) {
					return null;
				} else {
					return finishPoll(first);
				}
			} finally {
				lock.unlock();
			}
		}

		public RunnableScheduledFuture<?> take() throws InterruptedException {
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				for (;;) {
					RunnableScheduledFuture<?> first = queue[0];
					if (first == null) {
						available.await();
					} else {
						long delay = first.getDelay(NANOSECONDS);
						if (delay <= 0) {
							return finishPoll(first);
						}
						first = null; // don't retain ref while waiting
						if (leader != null) {
							available.await();
						} else {
							Thread currentThread = Thread.currentThread();
							leader = currentThread;
							try {
								//System.out.println(currentThread.getName()+" waiting...");
								available.awaitNanos(delay);// TODO === 等待延时时间
								//System.out.println(currentThread.getName()+" going on...");
							} finally {
								if (leader == currentThread) {
									leader = null;
								}
							}
						}
					}
				}
			} finally {
				if (leader == null && queue[0] != null) {
					available.signal();
				}
				lock.unlock();
			}
		}

		public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit) throws InterruptedException {
			long nanos = unit.toNanos(timeout);
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				for (;;) {
					RunnableScheduledFuture<?> first = queue[0];
					if (first == null) {
						if (nanos <= 0) {
							return null;
						} else {
							nanos = available.awaitNanos(nanos);
						}
					} else {
						long delay = first.getDelay(NANOSECONDS);
						if (delay <= 0) {
							return finishPoll(first);
						}
						if (nanos <= 0) {
							return null;
						}
						first = null; // don't retain ref while waiting
						if (nanos < delay || leader != null) {
							nanos = available.awaitNanos(nanos);
						} else {
							Thread thisThread = Thread.currentThread();
							leader = thisThread;
							try {
								long timeLeft = available.awaitNanos(delay);
								nanos -= delay - timeLeft;
							} finally {
								if (leader == thisThread) {
									leader = null;
								}
							}
						}
					}
				}
			} finally {
				if (leader == null && queue[0] != null) {
					available.signal();
				}
				lock.unlock();
			}
		}

		public void clear() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				for (int i = 0; i < size; i++) {
					RunnableScheduledFuture<?> task = queue[i];
					if (task != null) {
						queue[i] = null;
						setIndex(task, -1);
					}
				}
				size = 0;
			} finally {
				lock.unlock();
			}
		}

		private RunnableScheduledFuture<?> peekExpired() {
			// assert lock.isHeldByCurrentThread();
			RunnableScheduledFuture<?> first = queue[0];
			return (first == null || first.getDelay(NANOSECONDS) > 0) ? null : first;
		}

		public int drainTo(Collection<? super Runnable> taskColl) {
			if (taskColl == null) {
				throw new NullPointerException();
			}
			if (taskColl == this) {
				throw new IllegalArgumentException();
			}
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				RunnableScheduledFuture<?> first;
				int count = 0;
				while ((first = peekExpired()) != null) {
					taskColl.add(first);// In this order, in case add() throws.
					finishPoll(first);
					++count;
				}
				return count;
			} finally {
				lock.unlock();
			}
		}

		public int drainTo(Collection<? super Runnable> taskColl, int maxElements) {
			if (taskColl == null) {
				throw new NullPointerException();
			}
			if (taskColl == this) {
				throw new IllegalArgumentException();
			}
			if (maxElements <= 0) {
				return 0;
			}
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				RunnableScheduledFuture<?> first;
				int count = 0;
				while (count < maxElements && (first = peekExpired()) != null) {
					taskColl.add(first); // In this order, in case add() throws.
					finishPoll(first);
					++count;
				}
				return count;
			} finally {
				lock.unlock();
			}
		}

		public Object[] toArray() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return Arrays.copyOf(queue, size, Object[].class);
			} finally {
				lock.unlock();
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] newArray) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				if (newArray.length < size) {
					return (T[]) Arrays.copyOf(queue, size, newArray.getClass());
				}
				System.arraycopy(queue, 0, newArray, 0, size);
				if (newArray.length > size) {
					newArray[size] = null;
				}
				return newArray;
			} finally {
				lock.unlock();
			}
		}

		public Iterator<Runnable> iterator() {
			return new Itr(Arrays.copyOf(queue, size));
		}

		private class Itr implements Iterator<Runnable> {

			final RunnableScheduledFuture<?>[] array;
			int  cursor =  0;// index of next element to return
			int lastRet = -1;// index of last element, or -1 if no such

			Itr(RunnableScheduledFuture<?>[] array) {
				this.array = array;
			}

			public boolean hasNext() {
				return cursor < array.length;
			}

			public Runnable next() {
				if (cursor >= array.length) {
					throw new NoSuchElementException();
				}
				lastRet = cursor;
				return array[cursor++];
			}

			public void remove() {
				if (lastRet < 0) {
					throw new IllegalStateException();
				}
				DelayedWorkQueue.this.remove(array[lastRet]);
				lastRet = -1;
			}
		}
	}

}