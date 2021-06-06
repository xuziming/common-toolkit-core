package com.simon.credit.toolkit.core;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * LinkedHashMap = HashMap + LinkedList
 * @author XUZIMING 2019-11-28
 */
public class MyLinkedHashMap<K, V> extends MyHashMap<K, V> implements Map<K, V> {
	private static final long serialVersionUID = 3801124242820219131L;

	/** 链表头部节点(不存储数据) */
	private transient Entry<K, V> header;

	/** 访问顺序，为true表示按照最近访问顺序排，false表示按照插入顺序排，默认为false */
	private final boolean accessOrder;

	public MyLinkedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		accessOrder = false;
	}

	public MyLinkedHashMap(int initialCapacity) {
		super(initialCapacity);
		accessOrder = false;
	}

	public MyLinkedHashMap() {
		super();
		accessOrder = false;
	}

	public MyLinkedHashMap(Map<? extends K, ? extends V> m) {
		super(m);
		accessOrder = false;
	}

	public MyLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor);
		this.accessOrder = accessOrder;
	}

	@Override
    void init() {
		header = new Entry<K, V>(-1, null, null, null);
		header.before = header.after = header;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void transfer(MyHashMap.Entry[] newTable) {
		int newCapacity = newTable.length;
		for (Entry<K, V> entry = header.after; entry != header; entry = entry.after) {
			int index = indexFor(entry.hash, newCapacity);
			entry.next = newTable[index];
			newTable[index] = entry;
		}
	}

	@SuppressWarnings("rawtypes")
	public boolean containsValue(Object value) {
		// Overridden to take advantage of faster iterator
		if (value == null) {
			for (Entry entry = header.after; entry != header; entry = entry.after) {
				if (entry.value == null) {
					return true;
				}
			}
		} else {
			for (Entry entry = header.after; entry != header; entry = entry.after) {
				if (value.equals(entry.value)) {
					return true;
				}
			}
		}

		return false;
	}

	public V get(Object key) {
		Entry<K, V> entry = (Entry<K, V>) getEntry(key);
		if (entry == null) {
			return null;
		}
		entry.recordAccess(this);
		return entry.value;
	}

	public void clear() {
		super.clear();
		header.before = header.after = header;
	}

	private static class Entry<K, V> extends MyHashMap.Entry<K, V> {
		// These fields comprise the doubly linked list used for iteration.
		Entry<K, V> before, after;

		Entry(int hash, K key, V value, MyHashMap.Entry<K, V> next) {
			super(hash, key, value, next);
		}

		private void remove() {
			before.after = after;
			after.before = before;
		}

		private void addBefore(Entry<K, V> existingEntry) {// existingEntry的值为header
			after = existingEntry;		  // 1、将新加节点的后继节点指向原header
			before = existingEntry.before;// 2、将新加节点的前驱节点指向原header的前驱节点
			before.after = this;		  // 3、将新加节点的前驱节点的后继节点指向自己
			after.before = this;		  // 4、将新加节点的后继节点的前驱节点指向自己
		}

		@Override
		void recordAccess(MyHashMap<K, V> m) {
			MyLinkedHashMap<K, V> linkedHashMap = (MyLinkedHashMap<K, V>) m;

			// accessOrder为true，则访问时，将数据删除，并在链表header之前添加，按照header之前的顺序，离header越近则越新访问
			if (linkedHashMap.accessOrder) {
				linkedHashMap.modCount++;
				remove();// 删除元素
				addBefore(linkedHashMap.header);// 在尾部重新插入
			}
		}

		@Override
		void recordRemoval(MyHashMap<K, V> m) {
			remove();
		}
	}

	private abstract class LinkedHashIterator<T> implements Iterator<T> {
		Entry<K, V> nextEntry = header.after;
		Entry<K, V> lastReturned = null;

		int expectedModCount = modCount;

		public boolean hasNext() {
			return nextEntry != header;
		}

		public void remove() {
			if (lastReturned == null) {
				throw new IllegalStateException();
			}

			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}

			MyLinkedHashMap.this.remove(lastReturned.key);
			lastReturned = null;
			expectedModCount = modCount;
		}

		Entry<K, V> nextEntry() {
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}

			if (nextEntry == header) {
				throw new NoSuchElementException();
			}

			Entry<K, V> e = lastReturned = nextEntry;
			nextEntry = e.after;
			return e;
		}
	}

	private class KeyIterator extends LinkedHashIterator<K> {
		public K next() {
			return nextEntry().getKey();
		}
	}

	private class ValueIterator extends LinkedHashIterator<V> {
		public V next() {
			return nextEntry().value;
		}
	}

	private class EntryIterator extends LinkedHashIterator<Map.Entry<K, V>> {
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	@Override
	Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	@Override
	Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

	@Override
	void addEntry(int hash, K key, V value, int bucketIndex) {
		// 先创建新的键值对并且加入到header之前
		createEntry(hash, key, value, bucketIndex);

		Entry<K, V> eldest = header.after;// 获取头部节点

		if (removeEldestEntry(eldest)) {// 判断是否超过最大容量而移除年纪最大的键值对
			removeEntryForKey(eldest.key);
		} else {
			if (size >= threshold) {// 判断是否需要扩容
				resize(2 * table.length);
			}
		}
	}

	@Override
	void createEntry(int hash, K key, V value, int bucketIndex) {
		MyHashMap.Entry<K, V> old = table[bucketIndex];
		Entry<K, V> newEntry = new Entry<>(hash, key, value, old);
		table[bucketIndex] = newEntry;
		newEntry.addBefore(header);
		size++;
	}

	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return false;
	}

}