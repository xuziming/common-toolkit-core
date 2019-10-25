package com.simon.credit.toolkit.lang;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;

import com.simon.credit.toolkit.hash.MapToolkits;
import com.simon.credit.toolkit.reflect.DataFetcher;

public class DeadlockChecker {

	private static final ThreadMXBean MBEAN = ManagementFactory.getThreadMXBean();

	static final Runnable DEADLOCK_CHECK_TASK = new Runnable() {

		public void run() {
			for (;;) {
				long[] deadlockedThreadIds = MBEAN.findDeadlockedThreads();
				if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
					// 根据死锁线程ID列表获取线程信息
					ThreadInfo[] deadlockedThreadInfos = MBEAN.getThreadInfo(deadlockedThreadIds);
					// 获取所有线程
					Map<Long, Thread> allThreads = getAllThreads();

					for (int i = 0; i < deadlockedThreadInfos.length; i++) {
						long deadlockThreadId = deadlockedThreadInfos[i].getThreadId();
						Thread deadlockedThread = allThreads.get(deadlockThreadId);
						if (deadlockedThread != null) {
							// 中断死锁线程
							deadlockedThread.interrupt();
						}
					}
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// ignore
				}
			}

		}
	};

	private static final Map<Long, Thread> getAllThreads() {
		// Returns a map of stack traces for all live threads. 
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();

		return MapToolkits.parseMap(allStackTraces.keySet(), new DataFetcher<Thread, Long>() {
			public Long fetch(Thread t) {
				return t.getId();
			}
		});
	}

	public static void checkDeadlock() {
		Thread t = new Thread(DEADLOCK_CHECK_TASK);
		t.setDaemon(true);
		t.start();
	}

	public static void main(String[] args) {
		DeadlockChecker.checkDeadlock();
	}

}
