package com.simon.credit.toolkit.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class MyAbstractExecutorService implements ExecutorService {

	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new FutureTask<T>(runnable, value);
	}

	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new FutureTask<T>(callable);
	}

	@Override
	public Future<?> submit(Runnable task) {
		if (task == null) {
			throw new NullPointerException();
		}
		RunnableFuture<Void> futureTask = newTaskFor(task, null);
		execute(futureTask);
		return futureTask;
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		if (task == null) {
			throw new NullPointerException();
		}
		RunnableFuture<T> futureTask = newTaskFor(task, result);
		execute(futureTask);
		return futureTask;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		if (task == null) {
			throw new NullPointerException();
		}
		RunnableFuture<T> futureTask = newTaskFor(task);
		execute(futureTask);
		return futureTask;
	}

	private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks, boolean timed, long nanos)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (tasks == null) {
			throw new NullPointerException();
		}

		int taskSize = tasks.size();
		if (taskSize == 0) {
			throw new IllegalArgumentException();
		}

		List<Future<T>> futures = new ArrayList<Future<T>>(taskSize);
		ExecutorCompletionService<T> executorCompletionService = new ExecutorCompletionService<T>(this);

		try {
			ExecutionException executionException = null;
			final long deadline = timed ? System.nanoTime() + nanos : 0L;
			Iterator<? extends Callable<T>> iterator = tasks.iterator();

			// Start one task for sure; the rest incrementally
			futures.add(executorCompletionService.submit(iterator.next()));
			--taskSize;
			int active = 1;

			for (;;) {
				Future<T> future = executorCompletionService.poll();
				if (future == null) {
					if (taskSize > 0) {
						--taskSize;
						futures.add(executorCompletionService.submit(iterator.next()));
						++active;
					} else if (active == 0) {
						break;
					} else if (timed) {
						future = executorCompletionService.poll(nanos, TimeUnit.NANOSECONDS);
						if (future == null) {
							throw new TimeoutException();
						}
						nanos = deadline - System.nanoTime();
					} else {
						future = executorCompletionService.take();
					}
				}
				if (future != null) {
					--active;
					try {
						return future.get();
					} catch (ExecutionException ee) {
						executionException = ee;
					} catch (RuntimeException re) {
						executionException = new MyExecutionException(re);
					}
				}
			}

			if (executionException == null) {
				executionException = new MyExecutionException();
			}
			throw executionException;
		} finally {
			for (int i = 0, size = futures.size(); i < size; i++) {
				futures.get(i).cancel(true);
			}
		}
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		try {
			return doInvokeAny(tasks, false, 0);
		} catch (TimeoutException cannotHappen) {
			assert false;
			return null;
		}
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return doInvokeAny(tasks, true, unit.toNanos(timeout));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		if (tasks == null) {
			throw new NullPointerException();
		}
		List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
		boolean done = false;
		try {
			for (Callable<T> task : tasks) {
				RunnableFuture<T> futureTask = newTaskFor(task);
				futures.add(futureTask);
				execute(futureTask);
			}

			for (int i = 0, size = futures.size(); i < size; i++) {
				Future<T> future = futures.get(i);
				if (!future.isDone()) {
					try {
						future.get();
					} catch (CancellationException ignore) {
						// ignore
					} catch (ExecutionException ignore) {
						// ignore
					}
				}
			}
			done = true;
			return futures;
		} finally {
			if (!done) {
				for (int i = 0, size = futures.size(); i < size; i++) {
					futures.get(i).cancel(true);
				}
			}
		}
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		if (tasks == null) {
			throw new NullPointerException();
		}
		long nanos = unit.toNanos(timeout);
		ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
		boolean done = false;
		try {
			for (Callable<T> task : tasks) {
				futures.add(newTaskFor(task));
			}

			final long deadline = System.nanoTime() + nanos;
			final int size = futures.size();

			// Interleave time checks and calls to execute in case executor doesn't have any/much parallelism.
			for (int i = 0; i < size; i++) {
				execute((Runnable) futures.get(i));
				nanos = deadline - System.nanoTime();
				if (nanos <= 0L) {
					return futures;
				}
			}

			for (int i = 0; i < size; i++) {
				Future<T> future = futures.get(i);
				if (!future.isDone()) {
					if (nanos <= 0L) {
						return futures;
					}
					try {
						future.get(nanos, TimeUnit.NANOSECONDS);
					} catch (CancellationException ignore) {
						// ignore
					} catch (ExecutionException ignore) {
						// ignore
					} catch (TimeoutException toe) {
						return futures;
					}
					nanos = deadline - System.nanoTime();
				}
			}
			done = true;
			return futures;
		} finally {
			if (!done) {
				for (int i = 0, size = futures.size(); i < size; i++) {
					futures.get(i).cancel(true);
				}
			}
		}
	}

}