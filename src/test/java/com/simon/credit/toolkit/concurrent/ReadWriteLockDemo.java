package com.simon.credit.toolkit.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 读写锁示例
 * <pre>
 * 多个线程可以同时读一个资源类没有任何问题，所以为了满足并发量，读取共享资源应该可以同时进行。
 * 但是，如果有一个线程想去写共享资源，就不应该再有其它线程可以对该资源进行读或写。
 * 小总结：
 * 	读-读 可以共存
 * 	读-写 不能共存
 * 	写-写 不能共存
 * 
 * 写操作：原子+独占，整个过程必须是一个完整的统一体，中间不许被分割、被打断
 * </pre>
 * @author xuziming 2019-11-03
 */
public class ReadWriteLockDemo {

	public static void main(String[] args) {
		Cache cache = new Cache();

		for (int i = 1; i <= 5; i++) {
			final int tempInt = i;
			new Thread(() -> {
				cache.put(tempInt + "", tempInt + "");
			}, "W" + String.valueOf(i)).start();
			cache.put(i + "", i + "");
		}

		for (int i = 1; i <= 5; i++) {
			final int tempInt = i;
			new Thread(() -> {
				cache.get(tempInt + "");
			}, "R" + String.valueOf(i)).start();
			cache.put(i + "", i + "");
		}
	}

}

/**
 * 资源类
 * @author xuziming 2019-11-03
 */
class Cache {

	private volatile Map<String, Object> map = new HashMap<String, Object>(16);
	private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	public void put(String key, Object value) {
		rwLock.writeLock().lock();
		try {
			System.out.println(Thread.currentThread().getName()+"\t 正在写入: "+key);
			// 暂停一会
			try {
				TimeUnit.MICROSECONDS.sleep(300);
			} catch (Exception e) {
				e.printStackTrace();
			}
			map.put(key, value);
			System.out.println(Thread.currentThread().getName()+"\t 写入完成");
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	public Object get(String key) {
		rwLock.readLock().lock();
		try {
			System.out.println(Thread.currentThread().getName()+"\t 正在读取: "+key);
			// 暂停一会
			try {
				TimeUnit.MICROSECONDS.sleep(300);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Object result = map.get(key);
			System.out.println(Thread.currentThread().getName()+"\t 读取完成");
			return result;
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
}