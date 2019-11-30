package com.simon.credit.toolkit.core;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class MyHashMap<K, V> extends MyAbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

	/** 默认初始容量-必须是2的指数幂 */
	static final int DEFAULT_INITIAL_CAPACITY = 16;

    /** 最大容量 */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /** 加载因子(默认是0.75) */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** 长度必须是2的指数幂 */
    transient Entry<K, V>[] table;

    /** map的大小 */
    transient int size;

    /** 扩容阈值，值为：capacity * load factor */
    int threshold;

    /** 加载因子 */
    final float loadFactor;

    /** 修改次数 */
    transient int modCount;

    @SuppressWarnings("unchecked")
	public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }

        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;// capacity = capacity << 1; 即: capacity = capacity * 2;
		}

		this.loadFactor = loadFactor;
		threshold = (int) (capacity * loadFactor);
		table = new Entry[capacity];
		init();
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
	public MyHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
        init();
    }

    public MyHashMap(Map<? extends K, ? extends V> map) {
        this(Math.max((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAllForCreate(map);
    }

    void init() {}

    static int hash(int hashCode) {
    	// JDK7实现逻辑：
        hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
        return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);

        // JDK8实现逻辑：
        // int h;
        // return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

	static int indexFor(int h, int length) {
		return h & (length - 1);
	}

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

	public V get(Object key) {
		if (key == null) {
			return getForNullKey();
		}
		int hash = hash(key.hashCode());
		Entry<K, V> savedEntry = table[indexFor(hash, table.length)];
		for (; savedEntry != null; savedEntry = savedEntry.next) {
			Object savedKey;
			if (savedEntry.hash == hash && ((savedKey = savedEntry.key) == key || key.equals(savedKey))) {
				return savedEntry.value;
			}
		}
		return null;
	}

    private V getForNullKey() {
        for (Entry<K,V> entry = table[0]; entry != null; entry = entry.next) {
            if (entry.key == null) {
                return entry.value;
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

	final Entry<K, V> getEntry(Object key) {
		int hash = (key == null) ? 0 : hash(key.hashCode());
		Entry<K, V> entry = table[indexFor(hash, table.length)];
		for (; entry != null; entry = entry.next) {
			Object savedKey;// 当前Map中已存在的key
			if (entry.hash == hash && ((savedKey = entry.key) == key || (key != null && key.equals(savedKey)))) {
				return entry;
			}
		}
		return null;
	}

    public V put(K key, V value) {
        if (key == null) {
            return putForNullKey(value);
        }

        int hash = hash(key.hashCode());// 待插入hash
        int index = indexFor(hash, table.length);

        Entry<K,V> entry = table[index];
		for (; entry != null; entry = entry.next) {// 进入循环体，说明key的散列位置已存在链表
            Object savedKey;// 当前Map中已存在的key
            if (entry.hash == hash && ((savedKey = entry.key) == key || key.equals(savedKey))) {
            	// 覆盖原来的元素(key & value)
                V oldValue = entry.value;
                entry.value = value;
                entry.recordAccess(this);
                return oldValue;// 返回旧值
            }
        }

        modCount++;
        addEntry(hash, key, value, index);
        return null;
    }

    private V putForNullKey(V value) {
        for (Entry<K,V> entry = table[0]; entry != null; entry = entry.next) {
            if (entry.key == null) {
                V oldValue = entry.value;
                entry.value = value;
                entry.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
        addEntry(0, null, value, 0);
        return null;
    }

    private void putForCreate(K key, V value) {
        int hash = (key == null) ? 0 : hash(key.hashCode());
        int i = indexFor(hash, table.length);

        for (Entry<K,V> entry = table[i]; entry != null; entry = entry.next) {
            Object k;
            if (entry.hash == hash && ((k = entry.key) == key || (key != null && key.equals(k)))) {
                entry.value = value;
                return;
            }
        }

        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            putForCreate(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	void resize(int newCapacity) {// newCapacity = 2 * table.length;
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

		Entry[] newTable = new Entry[newCapacity];
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void transfer(Entry[] newTable) {
		Entry[] oldTable = table;
		int newCapacity = newTable.length;
		for (int index = 0; index < oldTable.length; index++) {
			Entry<K, V> entry = oldTable[index];
			if (entry != null) {
				oldTable[index] = null;
				do {
					Entry<K, V> next = entry.next;
					int i = indexFor(entry.hash, newCapacity);
					entry.next = newTable[i];// 防止newTable的i位置已存在值，则将原表头置为新表头entry的下一个元素next
					newTable[i] = entry;
					entry = next;// 循环重新hash
				} while (entry != null);
			}
		}
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		int numKeysToBeAdded = m.size();
		if (numKeysToBeAdded == 0) {
			return;
		}

		if (numKeysToBeAdded > threshold) {
			int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
			if (targetCapacity > MAXIMUM_CAPACITY) {
				targetCapacity = MAXIMUM_CAPACITY;
			}
			int newCapacity = table.length;
			while (newCapacity < targetCapacity) {
				newCapacity <<= 1;
			}
			if (newCapacity > table.length) {
				resize(newCapacity);
			}
		}

		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

    public V remove(Object key) {
        Entry<K,V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

	final Entry<K, V> removeEntryForKey(Object key) {
		int hash = (key == null) ? 0 : hash(key.hashCode());
		int index = indexFor(hash, table.length);
		Entry<K, V> prev = table[index];
		Entry<K, V> entry = prev;

		while (entry != null) {
			Entry<K, V> next = entry.next;
			Object k;
			if (entry.hash == hash && ((k = entry.key) == key || (key != null && key.equals(k)))) {
				modCount++;
				size--;
				if (prev == entry) {
					table[index] = next;
				} else {
					prev.next = next;
				}
				entry.recordRemoval(this);
				return entry;
			}
			prev = entry;
			entry = next;
		}

		return entry;
	}

	@SuppressWarnings("unchecked")
	final Entry<K, V> removeMapping(Object obj) {
		if (!(obj instanceof Map.Entry)) {
			return null;
		}

		Map.Entry<K, V> targetEntry = (Map.Entry<K, V>) obj;
		Object key = targetEntry.getKey();
		int hash = (key == null) ? 0 : hash(key.hashCode());
		int i = indexFor(hash, table.length);
		Entry<K, V> prev = table[i];
		Entry<K, V> entry = prev;

		while (entry != null) {
			Entry<K, V> next = entry.next;
			if (entry.hash == hash && entry.equals(targetEntry)) {
				modCount++;
				size--;
				if (prev == entry) {
					table[i] = next;
				} else {
					prev.next = next;
				}
				entry.recordRemoval(this);
				return entry;
			}
			prev = entry;
			entry = next;
		}

		return entry;
	}

	@SuppressWarnings("rawtypes")
	public void clear() {
		modCount++;
		Entry[] tableCopy = table;// 读取副本
		for (int i = 0; i < tableCopy.length; i++) {
			tableCopy[i] = null;
		}
		size = 0;
	}

	@SuppressWarnings("rawtypes")
	public boolean containsValue(Object value) {
		if (value == null) {
			return containsNullValue();
		}

		Entry[] tableCopy = table;// 读取副本
		for (int i = 0; i < tableCopy.length; i++) {
			for (Entry entry = tableCopy[i]; entry != null; entry = entry.next) {
				if (value.equals(entry.value)) {
					return true;
				}
			}
		}

		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean containsNullValue() {
		Entry[] tableCopy = table;// 读取副本
		for (int i = 0; i < tableCopy.length; i++) {
			for (Entry entry = tableCopy[i]; entry != null; entry = entry.next) {
				if (entry.value == null) {
					return true;
				}
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		MyHashMap<K, V> result = null;
		try {
			result = (MyHashMap<K, V>) super.clone();
		} catch (CloneNotSupportedException e) {
			// assert false;
		}
		result.table = new Entry[table.length];
		result.entrySet = null;
		result.modCount = 0;
		result.size = 0;
		result.init();
		result.putAllForCreate(this);

		return result;
	}

    static class Entry<K,V> implements Map.Entry<K,V> {
        final K key;
        V value;
        Entry<K,V> next;
        final int hash;

        Entry(int h, K k, V v, Entry<K,V> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

		@SuppressWarnings("rawtypes")
		public final boolean equals(Object targetObj) {
			if (!(targetObj instanceof Map.Entry)) {
				return false;
			}
			Map.Entry targetEntry = (Map.Entry) targetObj;
			Object currentKey = this.getKey();
			Object targetKey = targetEntry.getKey();

			if (currentKey == targetKey || (currentKey != null && currentKey.equals(targetKey))) {
				Object currentValue = this.getValue();
				Object targetValue = targetEntry.getValue();
				if (currentValue == targetValue || (currentValue != null && currentValue.equals(targetValue))) {
					return true;
				}
			}
			return false;
		}

        public final int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }

        void recordAccess(MyHashMap<K,V> m) {}

        void recordRemoval(MyHashMap<K,V> m) {}
    }

    void addEntry(int hash, K key, V value, int bucketIndex) {
        Entry<K,V> savedEntry = table[bucketIndex];// 先获取index位置对应的链表表头元素
        // 新加入的元素作为表头，元链表头作为新表头的下一个元素
        table[bucketIndex] = new Entry<K, V>(hash, key, value, savedEntry);
        if (size++ >= threshold) {// 如果加入一个元素之后超过阈值，则要进行两倍扩容
            resize(2 * table.length);
        }
    }

    void createEntry(int hash, K key, V value, int bucketIndex) {
        Entry<K,V> savedEntry = table[bucketIndex];
        table[bucketIndex] = new Entry<K, V>(hash, key, value, savedEntry);
        size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        Entry<K,V> next;        // next entry to return
        int expectedModCount;   // For fast-fail
        int index;              // current slot
        Entry<K,V> current;     // current entry

        @SuppressWarnings({ "rawtypes", "unchecked" })
		HashIterator() {
			expectedModCount = modCount;
			if (size > 0) { // advance to first entry
				Entry[] t = table;
				while (index < t.length && (next = t[index++]) == null) {
					;
				}
			}
		}

        public final boolean hasNext() {
            return next != null;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
		final Entry<K,V> nextEntry() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            Entry<K,V> e = next;
            if (e == null) {
                throw new NoSuchElementException();
            }

            if ((next = e.next) == null) {
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null) {
                    ;
                }
            }
            current = e;
            return e;
        }

        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }

            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            Object k = current.key;
            current = null;
            MyHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }

    }

    private final class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private final class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

	private final class EntryIterator extends HashIterator<Map.Entry<K, V>> {
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

    private transient Set<Map.Entry<K,V>> entrySet = null;

    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

	private final class KeySet extends AbstractSet<K> {
		public Iterator<K> iterator() {
			return newKeyIterator();
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsKey(o);
		}

		public boolean remove(Object o) {
			return MyHashMap.this.removeEntryForKey(o) != null;
		}

		public void clear() {
			MyHashMap.this.clear();
		}
	}

    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

	private final class Values extends AbstractCollection<V> {
		public Iterator<V> iterator() {
			return newValueIterator();
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsValue(o);
		}

		public void clear() {
			MyHashMap.this.clear();
		}
	}

    public Set<Map.Entry<K,V>> entrySet() {
        return entrySet0();
    }

    private Set<Map.Entry<K,V>> entrySet0() {
        Set<Map.Entry<K,V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

	private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		public Iterator<Map.Entry<K, V>> iterator() {
			return newEntryIterator();
		}

		@SuppressWarnings("unchecked")
		public boolean contains(Object targetObj) {
			if (!(targetObj instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<K, V> targetEntry = (Map.Entry<K, V>) targetObj;
			Entry<K, V> candidate = getEntry(targetEntry.getKey());
			return candidate != null && candidate.equals(targetEntry);
		}

		public boolean remove(Object o) {
			return removeMapping(o) != null;
		}

		public int size() {
			return size;
		}

		public void clear() {
			MyHashMap.this.clear();
		}
	}

	private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        Iterator<Map.Entry<K,V>> i = (size > 0) ? entrySet0().iterator() : null;

        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        s.writeInt(table.length);

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        if (i != null) {
            while (i.hasNext()) {
                Map.Entry<K,V> e = i.next();
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }

    private static final long serialVersionUID = 362498820763181265L;

    @SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        // Read in the threshold, loadfactor, and any hidden stuff
        s.defaultReadObject();

        // Read in number of buckets and allocate the bucket array;
        int numBuckets = s.readInt();
        table = new Entry[numBuckets];

        init();  // Give subclass a chance to do its thing.

        // Read in size (number of Mappings)
        int size = s.readInt();

        // Read the keys and values, and put the mappings in the HashMap
        for (int i=0; i<size; i++) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            putForCreate(key, value);
        }
    }

    // These methods are used when serializing HashSets
    int   capacity()     { return table.length; }
    float loadFactor()   { return loadFactor;   }

}