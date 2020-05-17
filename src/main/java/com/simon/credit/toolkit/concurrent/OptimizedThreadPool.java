package com.simon.credit.toolkit.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池
 * <pre>
  * 线程池的底层工作原理：
  * 1、在创建了线程池之后，等待提交过来的任务请求。
  * 2、当调用execute()方法添加一个请求任务时，线程池会做如下判断：
  * 	2.1、如果正在运行的线程数数量小于corePoolSize，那么马上创建线程运行这个任务。
  * 	2.2、如果正在运行的线程数量大于或等于corePoolSize，那么将这个任务<B>放入队列</B>。
  * 	2.3、如果这个时候队列满了且正在运行的线程数量还小于maximumPoolSize，那么还是要创建非核心线程立刻运行这个任务。
  * 	2.4、如果队列满了且正在运行的线程数量大于或等于maximumPoolSize，那么线程池<B>会启动饱和拒绝策略来执行</B>。
  * 3、当一个线程完成任务时，它会从队列中取下一个任务来执行。
  * 4、当一个线程无事可做超过一定的时间(keepAliveTime)时，线程池会判断：
  * 	如果当前运行的线程数大于corePoolSize，那么这个线程就会被停掉。
  * 	所以线程池的所有任务完成后它<B>最终会缩到corePoolSize的大小</B>。
 * </pre>
 * @author XUZIMING 2017-12-11
 */
public class OptimizedThreadPool implements ExecutorService {

	private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
	private static final int DEFAULT_WORK_QUEUE_SIZE = 256;

	private ThreadPoolExecutor threadPool;

	private OptimizedThreadPool() {
		this(DEFAULT_WORK_QUEUE_SIZE);
	}

	private OptimizedThreadPool(int workQueueSize) {
		initThreadPool(workQueueSize);
	}

	public static final OptimizedThreadPool newOptimizedThreadPool(int workQueueSize) {
		return new OptimizedThreadPool(workQueueSize);
	}

	/**
	 * === 自定义线程池初始化方法 ===
	 * <pre>
	 * corePoolSize 核心线程池大小----CPU核心数 
	 * maximumPoolSize 最大线程池大小----CPU核心数 
	 * keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间
	 * keepAliveTime 时间单位----TimeUnit.SECONDS(秒)
	 * workQueue 阻塞队列----new ArrayBlockingQueue<Runnable>(256)====256容量的阻塞队列 
	 * threadFactory 新建线程工厂----new CustomThreadFactory()====自定义线程工厂 
	 * </pre>
	 */
	private void initThreadPool(int workQueueSize) {
		workQueueSize = workQueueSize == 0 ? DEFAULT_WORK_QUEUE_SIZE : workQueueSize;
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(workQueueSize);
		ThreadFactory threadFactory = new SelfNamingThreadFactory();
		RejectedExecutionHandler handler = new BlockingPolicy();

		// 创建线程池
		this.threadPool = new ThreadPoolExecutor(CPU_NUM, CPU_NUM, 30, TimeUnit.SECONDS, workQueue, threadFactory, handler);
	}

	/**
	 * 自命名线程工厂
	 * @author XUZIMING 2019-11-04
	 */
	private class SelfNamingThreadFactory implements ThreadFactory {
		private ThreadFactory factory = Executors.defaultThreadFactory();
		private AtomicInteger index = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = factory.newThread(runnable);
			thread.setName(OptimizedThreadPool.class.getName() + "_" + index.incrementAndGet());
			return thread;
		}
	}

	/**
	 * 自定义拒绝策略：阻塞
	 * <pre>
	 * JDK内置的线程池4种拒绝策略：
	 * CallerRunsPolicy 	："调用者运行"一种调节机制，该策略既不会抛弃任务，也不会抛出异常，而是将某些任务回退到调用者，由调用者来执行
	 * AbortPolicy(default)	：直接抛出RejectedExecutionException异常阻止系统正常运行
	 * DiscardPolicy		：直接丢弃任务，不予任何处理也不抛出异常。如果允许任务丢失，这是最好的一种方案
	 * DiscardOldestPolicy	：抛弃队列中等待最久的任务，然后把当前任务加入队列中尝试再次提交当前任务
	 * </pre>
	 * @author XUZIMING 2019-11-04
	 */
	private class BlockingPolicy implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
			executor.getQueue().offer(task);
		}
	}

	@Override
	public void execute(Runnable task) {
		threadPool.execute(task);
	}

	@Override
	public <V> Future<V> submit(Callable<V> task) {
		return threadPool.submit(task);
	}

	@Override
	public boolean isShutdown() {
		return threadPool.isShutdown();
	}

	@Override
	public void shutdown() {
		threadPool.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return threadPool.shutdownNow();
	}

	@Override
	public boolean isTerminated() {
		return threadPool.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return threadPool.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return threadPool.submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return threadPool.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return threadPool.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return threadPool.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return threadPool.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return threadPool.invokeAny(tasks, timeout, unit);
	}

}