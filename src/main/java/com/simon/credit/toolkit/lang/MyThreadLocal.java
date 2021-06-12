package com.simon.credit.toolkit.lang;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 自定义ThreadLocal
 * <pre>
 *     ThreadLocal内存泄漏的场景只有：当threadLocalRef为null，并且之后该线程被放入线程池中，
 *     后面没有调用过get、set或remove方法，那么ThreadLocalMap对象中的ThreadLocal对象被回收，
 *     ThreadLocalMap的value对象就存在堆内存中无法回收，从而造成内存泄漏。
 * </pre>
 * @param <T>
 */
public class MyThreadLocal<T> {

	private final int threadLocalHashCode = nextHashCode();

	private static AtomicInteger nextHashCode = new AtomicInteger();

	private static final int HASH_INCREMENT = 0x61c88647;

	private static int nextHashCode() {
		return nextHashCode.getAndAdd(HASH_INCREMENT);
	}

	protected T initialValue() {
		return null;
	}

	public static <S> MyThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
		return new SuppliedThreadLocal<>(supplier);
	}

	public MyThreadLocal() {}

	public T get() {
		Thread t = Thread.currentThread();
		ThreadLocalMap map = getMap(t);
		if (map != null) {
			ThreadLocalMap.Entry entry = map.getEntry(this);
			if (entry != null) {
				@SuppressWarnings("unchecked")
				T result = (T) entry.value;
				return result;
			}
		}
		return setInitialValue();
	}

	private T setInitialValue() {
		T value = initialValue();
		Thread currentThread = Thread.currentThread();
		ThreadLocalMap map = getMap(currentThread);
		if (map != null) {
			map.set(this, value);
		} else {
			createMap(currentThread, value);
		}
		return value;
	}

	public void set(T value) {
		Thread currentThread = Thread.currentThread();
		ThreadLocalMap map = getMap(currentThread);
		if (map != null) {
			map.set(this, value);
		} else {
			createMap(currentThread, value);
		}
	}

	public void remove() {
		ThreadLocalMap map = getMap(Thread.currentThread());
		if (map != null) {
			map.remove(this);
		}
	}

	private Map<Thread, ThreadLocalMap> threadLocals = new ConcurrentHashMap<Thread, ThreadLocalMap>();

	ThreadLocalMap getMap(Thread t) {
		return threadLocals.get(t);
	}

	void createMap(Thread t, T firstValue) {
		threadLocals.put(t, new ThreadLocalMap(this, firstValue));
	}

	static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
		return new ThreadLocalMap(parentMap);
	}

	T childValue(T parentValue) {
		throw new UnsupportedOperationException();
	}

	static final class SuppliedThreadLocal<T> extends MyThreadLocal<T> {

		private final Supplier<? extends T> supplier;

		SuppliedThreadLocal(Supplier<? extends T> supplier) {
			this.supplier = Objects.requireNonNull(supplier);
		}

		@Override
		protected T initialValue() {
			return supplier.get();
		}
	}

	static class ThreadLocalMap {

		static class Entry extends WeakReference<MyThreadLocal<?>> {
			Object value;

			Entry(MyThreadLocal<?> threadLocalRef, Object valueObj) {
				super(threadLocalRef);
				value = valueObj;
			}
		}

		private static final int INITIAL_CAPACITY = 16;

		private Entry[] table;

		private int size = 0;

		private int threshold; // Default to 0

		private void setThreshold(int len) {
			threshold = len * 2 / 3;
		}

		private static int nextIndex(int i, int len) {
			return ((i + 1 < len) ? i + 1 : 0);
		}

		private static int prevIndex(int i, int len) {
			return ((i - 1 >= 0) ? i - 1 : len - 1);
		}

		ThreadLocalMap(MyThreadLocal<?> firstKey, Object firstValue) {
			table = new Entry[INITIAL_CAPACITY];
			int index = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
			table[index] = new Entry(firstKey, firstValue);
			size = 1;
			setThreshold(INITIAL_CAPACITY);
		}

		private ThreadLocalMap(ThreadLocalMap parentMap) {
			Entry[] parentTable = parentMap.table;
			int len = parentTable.length;
			setThreshold(len);
			table = new Entry[len];

			for (int i = 0; i < len; i++) {
				Entry entry = parentTable[i];
				if (entry != null) {
					@SuppressWarnings("unchecked")
					MyThreadLocal<Object> key = (MyThreadLocal<Object>) entry.get();
					if (key != null) {
						Object value = key.childValue(entry.value);
						Entry candidate = new Entry(key, value);
						int hash = key.threadLocalHashCode & (len - 1);
						while (table[hash] != null) {
							hash = nextIndex(hash, len);
						}
						table[hash] = candidate;
						size++;
					}
				}
			}
		}

		private Entry getEntry(MyThreadLocal<?> key) {
			int index = key.threadLocalHashCode & (table.length - 1);
			Entry entry = table[index];
			if (entry != null && entry.get() == key) {
				return entry;
			} else {
				return getEntryAfterMiss(key, index, entry);
			}
		}

		private Entry getEntryAfterMiss(MyThreadLocal<?> key, int i, Entry entry) {
			Entry[] tab = table;
			int len = tab.length;

			while (entry != null) {
				MyThreadLocal<?> existsKey = entry.get();
				if (existsKey == key) {
					return entry;
				}
				if (existsKey == null) {
					expungeStaleEntry(i);
				} else {
					i = nextIndex(i, len);
				}
				entry = tab[i];
			}
			return null;
		}

		private void set(MyThreadLocal<?> key, Object value) {
			Entry[] tab = table;
			int len = tab.length;
			int index = key.threadLocalHashCode & (len - 1);

			for (Entry entry = tab[index]; entry != null; entry = tab[index = nextIndex(index, len)]) {
				MyThreadLocal<?> existsKey = entry.get();

				if (existsKey == key) {
					entry.value = value;
					return;
				}

				if (existsKey == null) {
					replaceStaleEntry(key, value, index);
					return;
				}
			}

			tab[index] = new Entry(key, value);
			int newSize = ++size;
			if (!cleanSomeSlots(index, newSize) && newSize >= threshold) {
				rehash();
			}
		}

		private void remove(MyThreadLocal<?> key) {
			Entry[] tab = table;
			int len = tab.length;
			int index = key.threadLocalHashCode & (len - 1);
			for (Entry entry = tab[index]; entry != null; entry = tab[index = nextIndex(index, len)]) {
				if (entry.get() == key) {
					entry.clear();
					expungeStaleEntry(index);
					return;
				}
			}
		}

		private void replaceStaleEntry(MyThreadLocal<?> key, Object value, int staleSlot) {
			Entry[] tab = table;
			int len = tab.length;
			Entry entry;

			int slotToExpunge = staleSlot;
			for (int i = prevIndex(staleSlot, len); (entry = tab[i]) != null; i = prevIndex(i, len)) {
				if (entry.get() == null) {
					slotToExpunge = i;
				}
			}

			for (int i = nextIndex(staleSlot, len); (entry = tab[i]) != null; i = nextIndex(i, len)) {
				MyThreadLocal<?> existsKey = entry.get();

				if (existsKey == key) {
					entry.value = value;

					tab[i] = tab[staleSlot];
					tab[staleSlot] = entry;

					// Start expunge at preceding stale entry if it exists
					if (slotToExpunge == staleSlot) {
						slotToExpunge = i;
					}
					cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
					return;
				}

				if (existsKey == null && slotToExpunge == staleSlot) {
					slotToExpunge = i;
				}
			}

			// If key not found, put new entry in stale slot
			tab[staleSlot].value = null;
			tab[staleSlot] = new Entry(key, value);

			// If there are any other stale entries in run, expunge them
			if (slotToExpunge != staleSlot) {
				cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
			}
		}

		/**
		 * 删掉脏Entry
		 * @param staleSlot 脏槽下标
		 * @return
		 */
		private int expungeStaleEntry(int staleSlot) {
			Entry[] tab = table;
			int len = tab.length;

			tab[staleSlot].value = null;
			tab[staleSlot] = null;
			size--;

			Entry entry;
			int i;
			for (i = nextIndex(staleSlot, len); (entry = tab[i]) != null; i = nextIndex(i, len)) {
				MyThreadLocal<?> key = entry.get();
				if (key == null) {
					entry.value = null;
					tab[i] = null;
					size--;
				} else {
					int hash = key.threadLocalHashCode & (len - 1);
					if (hash != i) {
						tab[i] = null;

						// Unlike Knuth 6.4 Algorithm R, we must scan until null because multiple entries could have been stale.
						while (tab[hash] != null) {
							hash = nextIndex(hash, len);
						}
						tab[hash] = entry;
					}
				}
			}
			return i;
		}

		/**
		 * 清理一些槽
		 * @param i
		 * @param n
		 * @return
		 */
		private boolean cleanSomeSlots(int i, int n) {
			boolean removed = false;
			Entry[] tab = table;
			int len = tab.length;
			do {
				i = nextIndex(i, len);
				Entry entry = tab[i];
				if (entry != null && entry.get() == null) {
					n = len;
					removed = true;
					i = expungeStaleEntry(i);
				}
			} while ((n >>>= 1) != 0);
			return removed;
		}

		private void rehash() {
			expungeStaleEntries();

			// Use lower threshold for doubling to avoid hysteresis
			if (size >= threshold - threshold / 4)
				resize();
		}

		private void resize() {
			Entry[] oldTab = table;
			int oldLen = oldTab.length;
			int newLen = oldLen * 2;
			Entry[] newTab = new Entry[newLen];
			int count = 0;

			for (int j = 0; j < oldLen; ++j) {
				Entry entry = oldTab[j];
				if (entry != null) {
					MyThreadLocal<?> key = entry.get();
					if (key == null) {
						entry.value = null; // Help the GC
					} else {
						int hash = key.threadLocalHashCode & (newLen - 1);
						while (newTab[hash] != null) {
							hash = nextIndex(hash, newLen);
						}
						newTab[hash] = entry;
						count++;
					}
				}
			}

			setThreshold(newLen);
			size = count;
			table = newTab;
		}

		private void expungeStaleEntries() {
			Entry[] tab = table;
			int len = tab.length;
			for (int index = 0; index < len; index++) {
				Entry entry = tab[index];
				if (entry != null && entry.get() == null) {
					expungeStaleEntry(index);
				}
			}
		}
	}

}