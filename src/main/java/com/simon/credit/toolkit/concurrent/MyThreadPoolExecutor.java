package com.simon.credit.toolkit.concurrent;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPoolExecutor extends MyAbstractExecutorService {

	/**
	 * <pre>
     * The main pool control state, ctl, is an atomic integer packing two conceptual fields workerCount, 
     * indicating the effective number of threads runState, indicating whether running, shutting down etc
     *
     * In order to pack them into one int, we limit workerCount to (2^29)-1 (about 500 million) threads 
     * rather than (2^31)-1 (2 billion) otherwise representable. If this is ever an issue in the future, 
     * the variable can be changed to be an AtomicLong, and the shift/mask constants below adjusted. 
     * But until the need arises, this code is a bit faster and simpler using an int.
     *
     * The workerCount is the number of workers that have been permitted to start and not permitted to stop. 
     * The value may be transiently different from the actual number of live threads,
     * for example when a ThreadFactory fails to create a thread when asked, 
     * and when exiting threads are still performing bookkeeping before terminating. 
     * The user-visible pool size is reported as the current size of the workers set.
     *
     * The runState provides the main lifecycle control, taking on values:
     *
     *      RUNNING: Accept new tasks and process queued tasks
     *     SHUTDOWN: Don't accept new tasks, but process queued tasks
     *         STOP: Don't accept new tasks, don't process queued tasks, and interrupt in-progress tasks
     *      TIDYING: All tasks have terminated, workerCount is zero,
     *               the thread transitioning to state TIDYING will run the terminated() hook method
     *   TERMINATED: terminated() has completed
     *
     * The numerical order among these values matters, to allow ordered comparisons. 
     * The runState monotonically increases over time, but need not hit each state. 
     * The transitions are:
     *
     * RUNNING -> SHUTDOWN
     *    On invocation of shutdown(), perhaps implicitly in finalize()
     * (RUNNING or SHUTDOWN) -> STOP
     *    On invocation of shutdownNow()
     * SHUTDOWN -> TIDYING
     *    When both queue and pool are empty
     * STOP -> TIDYING
     *    When pool is empty
     * TIDYING -> TERMINATED
     *    When the terminated() hook method has completed
     *
     * Threads waiting in awaitTermination() will return when the state reaches TERMINATED.
     *
     * Detecting the transition from SHUTDOWN to TIDYING is less straightforward than you'd like 
     * because the queue may become empty after non-empty and vice versa during SHUTDOWN state, 
     * but we can only terminate if, after seeing that it is empty, 
     * we see that workerCount is 0 (which sometimes entails a recheck -- see below).
     * </pre>
     */
	private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));// runState:RUNNING, workerCount:0

	private static final int COUNT_BITS = Integer.SIZE - 3;// 29(低29位来表示工作线程数量)
	private static final int CAPACITY 	= (1 << COUNT_BITS) - 1;// 000 11111111111111111111111111111(536870911)

	/*
	 * 观察线程池运行状态，总共有5个状态，可用3个bit来表示.
	 * 即用int的高3位来表示:线程运行状态，低29位来表示:工作线程数(最多可表示2^29-1=536870911, 即5亿+).
	 * RUNNING < SHUTDOWN < STOP < TIDYING < TERMINATED
	 */
	/** -1 << COUNT_BITS (111 00000000000000000000000000000 -> 实际值: -536870912) */
	private static final int RUNNING  	= -1 << COUNT_BITS;// 接受新task, 处理等待的task
	/**  0 << COUNT_BITS (000 00000000000000000000000000000 -> 实际值:          0) */
	private static final int SHUTDOWN 	=  0 << COUNT_BITS;// 不接受新task,但处理等待的task
	/**  1 << COUNT_BITS (001 00000000000000000000000000000 -> 实际值:  536870912) */
	private static final int STOP 		=  1 << COUNT_BITS;// 不接受新task
	/**  2 << COUNT_BITS (010 00000000000000000000000000000 -> 实际值: 1073741824) */
	private static final int TIDYING 	=  2 << COUNT_BITS;// 所有task都被终止,worCount为0时
	/**  3 << COUNT_BITS (011 00000000000000000000000000000 -> 实际值: 1610612736) */
	private static final int TERMINATED =  3 << COUNT_BITS;// 执行完terminated()方法

	/** 得到线程运行状态(高3位表示线程运行状态) */
	private static int runStateOf(int c) {
		// 111 00000000000000000000000000001 <===> c
		// 111 00000000000000000000000000000 <===> ~CAPACITY
		// 实际上只有高3位有值，低29位全部为0
		return c & ~CAPACITY;// c与(CAPACITY取反)
	}

	/** 得到工作线程数量(低29位表示工作线程数) */
	private static int workerCountOf(int c) {
		// 111 00000000000000000000000000001 <===> c
		// 000 11111111111111111111111111111 <===> CAPACITY
		// 实际上高3位全部为0，低29位有值
		return c & CAPACITY;// c与CAPACITY
	}

	/** 获取线程池运行状态与工作线程数量 */
	private static int ctlOf(int runState, int workerCount) {
		return getRunStateAndWorkerCount(runState, workerCount);
	}

	/** 获取线程池运行状态与工作线程数量 */
	private static int getRunStateAndWorkerCount(int runState, int workerCount) {
		// 111 00000000000000000000000000000 <===> runState(RUNNING为111)
		// 000 00000000000000000000000000001 <===> workerCount(示例为1个工作线程)
		// 111 00000000000000000000000000001 结果是高3位以及低29位或运算结果
		return runState | workerCount;
	}

	private static boolean runStateLessThan(int runStateAndWorkerCount, int state) {
		return runStateAndWorkerCount < state;
	}

	private static boolean runStateAtLeast(int runStateAndWorkerCount, int state) {
		return runStateAndWorkerCount >= state;
	}

	private static boolean isRunning(int runStateAndWorkerCount) {
		return runStateAndWorkerCount < SHUTDOWN;
	}

	private boolean compareAndIncrementWorkerCount(int expect) {
		return ctl.compareAndSet(expect, expect + 1);
	}

	private boolean compareAndDecrementWorkerCount(int expect) {
		return ctl.compareAndSet(expect, expect - 1);
	}

	private void decrementWorkerCount() {
		do {
			// ...
		} while (!compareAndDecrementWorkerCount(ctl.get()));
	}

	private final BlockingQueue<Runnable> workQueue;

	private final ReentrantLock  mainLock = new ReentrantLock();

	private final Condition termination = mainLock.newCondition();

	private final Set<Worker> workers = new HashSet<Worker>();

	private int largestPoolSize;// 当前线程池的最大线程数量

	private long completedTaskCount;

	private volatile ThreadFactory threadFactory;

	private volatile MyRejectedExecutionHandler handler;

	private volatile long keepAliveTime;

	private volatile boolean allowCoreThreadTimeOut = false;

	private volatile int corePoolSize;

	private volatile int maximumPoolSize;

	private static final MyRejectedExecutionHandler defaultHandler = new MyAbortPolicy();

	private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

	private final class Worker extends MyAbstractQueuedSynchronizer implements Runnable {
		private static final long serialVersionUID = 6138294804551838833L;

		final Thread thread;
		Runnable firstTask;
		volatile long completedTasks;

		Worker(Runnable firstTask) {
			setState(-1); // inhibit interrupts until runWorker
			this.firstTask = firstTask;
			this.thread = getThreadFactory().newThread(this);
		}

		@Override
		public void run() {
			runWorker(this);
		}

		@Override
		protected boolean isHeldExclusively() {
			return getState() != 0;
		}

		@Override
		protected boolean tryAcquire(int unused) {
			if (compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;
		}

		@Override
		protected boolean tryRelease(int unused) {
			setExclusiveOwnerThread(null);
			setState(0);
			return true;
		}

		public void lock() {
			acquire(1);
		}

		public boolean tryLock() {
			return tryAcquire(1);
		}

		public void unlock() {
			release(1);
		}

		public boolean isLocked() {
			return isHeldExclusively();
		}

		void interruptIfStarted() {
			Thread workerThread;
			if (getState() >= 0 && (workerThread = thread) != null && !workerThread.isInterrupted()) {
				try {
					workerThread.interrupt();
				} catch (SecurityException ignore) {
					// ignore
				}
			}
		}
	}

	private void advanceRunState(int targetState) {
		for (;;) {
			int c = ctl.get();
			if (runStateAtLeast(c, targetState) || ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c)))) {
				break;
			}
		}
	}

	protected final void tryTerminate() {
		for (;;) {
			int c = ctl.get();
			if (isRunning(c) || runStateAtLeast(c, TIDYING) || (runStateOf(c) == SHUTDOWN && !workQueue.isEmpty())) {
				return;
			}
			// Eligible to terminate
			if (workerCountOf(c) != 0) {
				interruptIdleWorkers(ONLY_ONE);
				return;
			}

			final ReentrantLock mainLock = this.mainLock;
			mainLock.lock();
			try {
				if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
					try {
						terminated();
					} finally {
						ctl.set(ctlOf(TERMINATED, 0));
						termination.signalAll();
					}
					return;
				}
			} finally {
				mainLock.unlock();
			}
			// else retry on failed CAS
		}
	}

	private void checkShutdownAccess() {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(shutdownPerm);
			final ReentrantLock mainLock = this.mainLock;
			mainLock.lock();
			try {
				for (Worker worker : workers) {
					security.checkAccess(worker.thread);
				}
			} finally {
				mainLock.unlock();
			}
		}
	}

	private void interruptWorkers() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker worker : workers) {
				worker.interruptIfStarted();
			}
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * 中断空闲工作线程
	 * @param onlyOne 是否只中断一个工作线程
	 */
	private void interruptIdleWorkers(boolean onlyOne) {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker worker : workers) {
				Thread workerThread = worker.thread;
				if (!workerThread.isInterrupted() && worker.tryLock()) {
					try {
						workerThread.interrupt();
					} catch (SecurityException ignore) {
						// ignore
					} finally {
						worker.unlock();
					}
				}
				if (onlyOne) {
					break;
				}
			}
		} finally {
			mainLock.unlock();
		}
	}

	private void interruptIdleWorkers() {
		interruptIdleWorkers(false);
	}

	private static final boolean ONLY_ONE = true;

	protected final void reject(Runnable task) {
		handler.rejectedExecution(task, this);
	}

	protected void onShutdown() {
		// nothing to do
	}

	protected final boolean isRunningOrShutdown(boolean shutdownOK) {
		int runState = runStateOf(ctl.get());
		return runState == RUNNING || (runState == SHUTDOWN && shutdownOK);
	}

	private List<Runnable> drainQueue() {
		BlockingQueue<Runnable> currentWorkQueue = workQueue;
		List<Runnable> tasks = new ArrayList<Runnable>();
		currentWorkQueue.drainTo(tasks);

		if (!currentWorkQueue.isEmpty()) {
			for (Runnable task : currentWorkQueue.toArray(new Runnable[0])) {
				if (currentWorkQueue.remove(task)) {
					tasks.add(task);
				}
			}
		}
		return tasks;
	}

	/**
	 * 增加工作线程
	 * @param firstTask 刚提交的任务
	 * @param core 是否核心工作线程(true|false)
	 * @return
	 */
	private boolean addWorker(Runnable firstTask, boolean core) {
		retry: for (;;) {
			int runStateAndWorkerCount = ctl.get();
			int runState = runStateOf(runStateAndWorkerCount);

			if (runState >= SHUTDOWN && !(runState == SHUTDOWN && firstTask == null && !workQueue.isEmpty())) {
				return false;
			}

			for (;;) {
				int workerCount = workerCountOf(runStateAndWorkerCount);
				if (workerCount >= CAPACITY || workerCount >= (core ? corePoolSize : maximumPoolSize)) {
					return false;
				}
				if (compareAndIncrementWorkerCount(runStateAndWorkerCount)) {
					break retry;
				}
				runStateAndWorkerCount = ctl.get(); // Re-read ctl
				if (runStateOf(runStateAndWorkerCount) != runState) {
					continue retry;
				}
				// else CAS failed due to workerCount change; retry inner loop
			}
		}

		boolean workerStarted = false;
		boolean workerAdded   = false;
		Worker worker = null;

		try {
			worker = new Worker(firstTask);
			// this.thread = getThreadFactory().newThread(this);

			final Thread workerThread = worker.thread;
			if (workerThread != null) {
				final ReentrantLock mainLock = this.mainLock;
				mainLock.lock();
				try {
					int runState = runStateOf(ctl.get());

					if (runState < SHUTDOWN || (runState == SHUTDOWN && firstTask == null)) {
						if (workerThread.isAlive()) {
							throw new IllegalThreadStateException();
						}
						workers.add(worker);
						int workerCount = workers.size();
						if (workerCount > largestPoolSize) {
							largestPoolSize = workerCount;
						}
						workerAdded = true;
					}
				} finally {
					mainLock.unlock();
				}
				if (workerAdded) {
					workerThread.start();// TODO === 启动线程执行任务
					workerStarted = true;
				}
			}
		} finally {
			if (!workerStarted) {
				addWorkerFailed(worker);
			}
		}
		return workerStarted;
	}

	private void addWorkerFailed(Worker worker) {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			if (worker != null) {
				workers.remove(worker);
			}
			// 工作线程数量递减
			decrementWorkerCount();
			tryTerminate();
		} finally {
			mainLock.unlock();
		}
	}

	private void processWorkerExit(Worker worker, boolean completedAbruptly) {
		if (completedAbruptly) {// If abrupt, then workerCount wasn't adjusted
			decrementWorkerCount();
		}

		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			completedTaskCount += worker.completedTasks;
			workers.remove(worker);
		} finally {
			mainLock.unlock();
		}

		tryTerminate();

		int c = ctl.get();
		if (runStateLessThan(c, STOP)) {
			if (!completedAbruptly) {
				int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
				if (min == 0 && !workQueue.isEmpty()) {
					min = 1;
				}
				if (workerCountOf(c) >= min) {
					return;// replacement not needed
				}
			}
			addWorker(null, false);
		}
	}

	/** === 获取工作任务 === */
	private Runnable getTask() {
		boolean timedOut = false; // Did the last poll() time out?

		for (;;) {
			int c = ctl.get();
			int runState = runStateOf(c);

			/** 线程池的状态已经是STOP、TIDYING、TERMINATED或者是(SHUTDOWN且工作队列为空) */
			// Check if queue empty only if necessary.
			if (runState >= SHUTDOWN && (runState >= STOP || workQueue.isEmpty())) {
				decrementWorkerCount();
				return null;
			}

			int workerCount = workerCountOf(c);

			// Are workers subject to culling?
			boolean timed = allowCoreThreadTimeOut || (workerCount > corePoolSize);// 工作线程数大于核心线程数

			if ((workerCount > maximumPoolSize || (timed && timedOut)) && (workerCount > 1 || workQueue.isEmpty())) {
				if (compareAndDecrementWorkerCount(c)) {
					return null;
				}
				continue;
			}

			try {
				// TODO === 从工作队列里获取新任务
				Runnable task = timed ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : workQueue.take();
				if (task != null) {
					return task;
				}
				timedOut = true;
			} catch (InterruptedException retry) {
				timedOut = false;
			}
		}
	}

	final void runWorker(Worker worker) {
		Thread workerThread = Thread.currentThread();
		Runnable task = worker.firstTask;
		worker.firstTask = null;
		worker.unlock(); // allow interrupts
		boolean completedAbruptly = true;
		try {
			/**
			 * 注意这段while循环的执行逻辑：
			 * 每执行完一个任务后，就会去工作队列中取下一个任务，
			 * 如果取出的任务为null，则当前worker线程终止
			 */
			while (task != null || (task = getTask()) != null) {
				worker.lock();
				if ((runStateAtLeast(ctl.get(), STOP) || (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) && !workerThread.isInterrupted()) {
					workerThread.interrupt();
				}

				try {
					beforeExecute(workerThread, task);
					Throwable thrown = null;
					try {
						task.run();
					} catch (RuntimeException re) {
						thrown = re;
						throw re;
					} catch (Error e) {
						thrown = e;
						throw e;
					} catch (Throwable t) {
						thrown = t;
						throw new Error(t);
					} finally {
						afterExecute(task, thrown);
					}
				} finally {
					task = null;
					worker.completedTasks++;
					worker.unlock();
				}
			}
			completedAbruptly = false;
		} finally {
			/** 在这个方法里把工作线程移除掉 */
			processWorkerExit(worker, completedAbruptly);
		}
	}

	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
																		BlockingQueue<Runnable> workQueue) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), defaultHandler);
	}

	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
											BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {

		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
	}

	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
										BlockingQueue<Runnable> workQueue, MyRejectedExecutionHandler handler) {

		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
	}

	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
		BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, MyRejectedExecutionHandler handler) {
		// 检查线程各配置参数是否合理
		if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
			throw new IllegalArgumentException();
		}
		if (workQueue == null || threadFactory == null || handler == null) {
			throw new NullPointerException();
		}
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.workQueue = workQueue;
		this.keepAliveTime = unit.toNanos(keepAliveTime);
		this.threadFactory = threadFactory;
		this.handler = handler;
	}

	@Override
	public void execute(Runnable task) {
		if (task == null) {
			throw new NullPointerException();
		}
		int runStateAndWorkerCount = ctl.get();

		// 1、线程池线程数小于核心线程数时，创建线程执行任务
		if (workerCountOf(runStateAndWorkerCount) < corePoolSize) {
			if (addWorker(task, true)) {
				return;
			}
			runStateAndWorkerCount = ctl.get();
		}

		// 2、线程池线程数达到核心线程数后，任务加入到工作队列进行等待
		if (isRunning(runStateAndWorkerCount) && workQueue.offer(task)) {
			// 复检线程池状态
			recheckAfterAddToWorkQueue(task);
			return;
		}

		// 3、增加非核心线程
		boolean addNonCoreThreadSuccess = addWorker(task, false);

		// 4、若工作队列已满，且线程池的线程数已达到最大线程数，则执行拒绝策略
		if (!addNonCoreThreadSuccess) {
			reject(task);
		}
	}

	/**
	 * 任务加入工作队列后复检线程池状态
	 * @param task
	 */
	private void recheckAfterAddToWorkQueue(Runnable task) {
		int runStateAndWorkerCount = ctl.get();
		if (!isRunning(runStateAndWorkerCount) && remove(task)) {
			reject(task);
		} else if (workerCountOf(runStateAndWorkerCount) == 0) {
			addWorker(null, false);
		}
	}

	@Override
	public void shutdown() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			checkShutdownAccess();
			advanceRunState(SHUTDOWN);
			interruptIdleWorkers();
			onShutdown(); // hook for ScheduledThreadPoolExecutor
		} finally {
			mainLock.unlock();
		}
		tryTerminate();
	}

	@Override
	public List<Runnable> shutdownNow() {
		List<Runnable> tasks;
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			checkShutdownAccess();
			advanceRunState(STOP);
			interruptWorkers();
			tasks = drainQueue();
		} finally {
			mainLock.unlock();
		}
		tryTerminate();
		return tasks;
	}

	@Override
	public boolean isShutdown() {
		return !isRunning(ctl.get());
	}

	public boolean isTerminating() {
		int runStateAndWorkerCount = ctl.get();
		return !isRunning(runStateAndWorkerCount) && runStateLessThan(runStateAndWorkerCount, TERMINATED);
	}

	@Override
	public boolean isTerminated() {
		return runStateAtLeast(ctl.get(), TERMINATED);
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (;;) {
				if (runStateAtLeast(ctl.get(), TERMINATED)) {
					return true;
				}
				if (nanos <= 0) {
					return false;
				}
				nanos = termination.awaitNanos(nanos);
			}
		} finally {
			mainLock.unlock();
		}
	}

	@Override
	protected void finalize() {
		shutdown();
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		if (threadFactory == null) {
			throw new NullPointerException();
		}
		this.threadFactory = threadFactory;
	}

	public ThreadFactory getThreadFactory() {
		return threadFactory;
	}

	public void setRejectedExecutionHandler(MyRejectedExecutionHandler handler) {
		if (handler == null) {
			throw new NullPointerException();
		}
		this.handler = handler;
	}

	public MyRejectedExecutionHandler getRejectedExecutionHandler() {
		return handler;
	}

	public void setCorePoolSize(int corePoolSize) {
		if (corePoolSize < 0) {
			throw new IllegalArgumentException();
		}
		int delta = corePoolSize - this.corePoolSize;// 增量值(数学或物理学中的△)
		this.corePoolSize = corePoolSize;
		if (workerCountOf(ctl.get()) > corePoolSize) {
			interruptIdleWorkers();
		} else if (delta > 0) {
			// === 需要增加的工作线程数量 ===
			int toAddWorkerCount = Math.min(delta, workQueue.size());
			while (toAddWorkerCount-- > 0 && addWorker(null, true)) {
				if (workQueue.isEmpty()) {
					break;
				}
			}
		}
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public boolean prestartCoreThread() {
		return workerCountOf(ctl.get()) < corePoolSize && addWorker(null, true);
	}

	protected void ensurePrestart() {
		int workerCount = workerCountOf(ctl.get());
		if (workerCount < corePoolSize) {
			addWorker(null, true);
		} else if (workerCount == 0) {
			addWorker(null, false);
		}
	}

	public int prestartAllCoreThreads() {
		int coreThreadCount = 0;
		while (addWorker(null, true)) {
			++coreThreadCount;
		}
		return coreThreadCount;
	}

	public boolean allowsCoreThreadTimeOut() {
		return allowCoreThreadTimeOut;
	}

	public void allowCoreThreadTimeOut(boolean value) {
		if (value && keepAliveTime <= 0) {
			throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
		}
		if (value != allowCoreThreadTimeOut) {
			allowCoreThreadTimeOut = value;
			if (value) {
				interruptIdleWorkers();
			}
		}
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize) {
			throw new IllegalArgumentException();
		}
		this.maximumPoolSize = maximumPoolSize;
		if (workerCountOf(ctl.get()) > maximumPoolSize) {
			interruptIdleWorkers();
		}
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setKeepAliveTime(long time, TimeUnit unit) {
		if (time < 0) {
			throw new IllegalArgumentException();
		}
		if (time == 0 && allowsCoreThreadTimeOut()) {
			throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
		}
		long keepAliveTime = unit.toNanos(time);
		long delta = keepAliveTime - this.keepAliveTime;
		this.keepAliveTime = keepAliveTime;
		if (delta < 0) {
			interruptIdleWorkers();
		}
	}

	public long getKeepAliveTime(TimeUnit unit) {
		return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
	}

	public BlockingQueue<Runnable> getQueue() {
		return workQueue;
	}

	public boolean remove(Runnable task) {
		boolean removed = workQueue.remove(task);
		tryTerminate(); // In case SHUTDOWN and now empty
		return removed;
	}

	public void purge() {
		final BlockingQueue<Runnable> currentWorkQueue = workQueue;
		try {
			Iterator<Runnable> iterator = currentWorkQueue.iterator();
			while (iterator.hasNext()) {
				Runnable task = iterator.next();
				if (task instanceof Future<?> && ((Future<?>) task).isCancelled()) {
					iterator.remove();
				}
			}
		} catch (ConcurrentModificationException fallThrough) {
			for (Object task : currentWorkQueue.toArray()) {
				if (task instanceof Future<?> && ((Future<?>) task).isCancelled()) {
					currentWorkQueue.remove(task);
				}
			}
		}

		tryTerminate(); // In case SHUTDOWN and now empty
	}

	public int getPoolSize() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			return runStateAtLeast(ctl.get(), TIDYING) ? 0 : workers.size();
		} finally {
			mainLock.unlock();
		}
	}

	public int getActiveCount() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			int activeCount = 0;
			for (Worker worker : workers) {
				if (worker.isLocked()) {
					++activeCount;
				}
			}
			return activeCount;
		} finally {
			mainLock.unlock();
		}
	}

	public int getLargestPoolSize() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			return largestPoolSize;
		} finally {
			mainLock.unlock();
		}
	}

	public long getTaskCount() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			long completedCount = completedTaskCount;
			for (Worker worker : workers) {
				completedCount += worker.completedTasks;
				if (worker.isLocked()) {
					++completedCount;
				}
			}
			return completedCount + workQueue.size();
		} finally {
			mainLock.unlock();
		}
	}

	public long getCompletedTaskCount() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			long completedCount = completedTaskCount;
			for (Worker worker : workers) {
				completedCount += worker.completedTasks;
			}
			return completedCount;
		} finally {
			mainLock.unlock();
		}
	}

	@Override
	public String toString() {
		long completedCount;
		int workerCount, activeCount;
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			completedCount = completedTaskCount;
			activeCount = 0;
			workerCount = workers.size();
			for (Worker worker : workers) {
				completedCount += worker.completedTasks;
				if (worker.isLocked()) {
					++activeCount;
				}
			}
		} finally {
			mainLock.unlock();
		}

		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[")
			   .append(getRunState())// 运行状态
			   .append(", pool size = ").append(workerCount)
			   .append(", active threads = ").append(activeCount)
			   .append(", queued tasks = ").append(workQueue.size())
			   .append(", completed tasks = ").append(completedCount)
			   .append("]");
		return builder.toString();
	}

	private String getRunState() {
		int runStateAndWorkerCount = ctl.get();
		if (runStateLessThan(runStateAndWorkerCount, SHUTDOWN)) {
			return "Running";
		}
		if (runStateAtLeast(runStateAndWorkerCount, TERMINATED)) {
			return "Terminated";
		}
		return "Shutting down";
	}

	protected void beforeExecute(Thread thread, Runnable runnable) {}

	protected void afterExecute(Runnable runnable, Throwable throwable) {}

	protected void terminated() {}

	/**
	 * 拒绝策略1：由调用线程处理该任务
	 */
	public static class MyCallerRunsPolicy implements MyRejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable runnable, MyThreadPoolExecutor executor) {
			if (!executor.isShutdown()) {
				runnable.run();
			}
		}
	}

	/**
	 * 拒绝策略2：丢弃任务并抛出RejectedExecutionException异常
	 */
	public static class MyAbortPolicy implements MyRejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable runnable, MyThreadPoolExecutor executor) {
			throw new RejectedExecutionException("Task " + runnable.toString() + " rejected from " + executor.toString());
		}
	}

	/**
	 * 拒绝策略3：直接丢弃任务，但是不抛出异常
	 */
	public static class MyDiscardPolicy implements MyRejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable runnable, MyThreadPoolExecutor executor) {
			// nothing to do, discard directly
		}
	}

	/**
	 * 拒绝策略4：丢弃队列最前面的任务，然后重新尝试执行任务(重复此过程)
	 */
	public static class MyDiscardOldestPolicy implements MyRejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable runnable, MyThreadPoolExecutor executor) {
			if (!executor.isShutdown()) {
				executor.getQueue().poll();
				executor.execute(runnable);
			}
		}
	}

}