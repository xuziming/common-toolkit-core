package com.simon.credit.toolkit.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MyTreeMap<K, V> extends MyAbstractMap<K, V> implements NavigableMap<K, V>, Cloneable, Serializable {

	private final Comparator<? super K> comparator;

	private transient Entry<K, V> root = null;

	private transient int size = 0;

	private transient int modCount = 0;

	public MyTreeMap() {
		comparator = null;
	}

	public MyTreeMap(Comparator<? super K> comparator) {
		this.comparator = comparator;
	}

	public MyTreeMap(Map<? extends K, ? extends V> m) {
		comparator = null;
		putAll(m);
	}

	public MyTreeMap(SortedMap<K, ? extends V> m) {
		comparator = m.comparator();
		try {
			buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
		} catch (IOException cannotHappen) {
		} catch (ClassNotFoundException cannotHappen) {
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean containsKey(Object key) {
		return getEntry(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
			if (valEquals(value, e.value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(Object key) {
		Entry<K, V> p = getEntry(key);
		return (p == null ? null : p.value);
	}

	@Override
	public Comparator<? super K> comparator() {
		return comparator;
	}

	@Override
	public K firstKey() {
		return key(getFirstEntry());
	}

	@Override
	public K lastKey() {
		return key(getLastEntry());
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		int mapSize = map.size();
		if (size == 0 && mapSize != 0 && map instanceof SortedMap) {
			Comparator<?> c = ((SortedMap<?, ?>) map).comparator();
			if (c == comparator || (c != null && c.equals(comparator))) {
				++modCount;
				try {
					buildFromSorted(mapSize, map.entrySet().iterator(), null, null);
				} catch (IOException cannotHappen) {
				} catch (ClassNotFoundException cannotHappen) {
				}
				return;
			}
		}
		super.putAll(map);
	}

	final Entry<K, V> getEntry(Object key) {
		// Offload comparator-based version for sake of performance
		if (comparator != null) {
			return getEntryUsingComparator(key);
		}
		if (key == null) {
			throw new NullPointerException();
		}
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) key;
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = k.compareTo(p.key);
			if (cmp < 0) {
				p = p.left;
			} else if (cmp > 0) {
				p = p.right;
			} else {
				return p;
			}
		}
		return null;
	}

	final Entry<K, V> getEntryUsingComparator(Object key) {
		@SuppressWarnings("unchecked")
		K k = (K) key;
		Comparator<? super K> cpr = comparator;
		if (cpr != null) {
			Entry<K, V> p = root;
			while (p != null) {
				int cmp = cpr.compare(k, p.key);
				if (cmp < 0) {
					p = p.left;
				} else if (cmp > 0) {
					p = p.right;
				} else {
					return p;
				}
			}
		}
		return null;
	}

	final Entry<K, V> getCeilingEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp < 0) {
				if (p.left != null) {
					p = p.left;
				} else {
					return p;
				}
			} else if (cmp > 0) {
				if (p.right != null) {
					p = p.right;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.right) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			} else {
				return p;
			}
		}

		return null;
	}

	final Entry<K, V> getFloorEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp > 0) {
				if (p.right != null) {
					p = p.right;
				} else {
					return p;
				}
			} else if (cmp < 0) {
				if (p.left != null) {
					p = p.left;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.left) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			} else {
				return p;
			}
		}

		return null;
	}

	final Entry<K, V> getHigherEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp < 0) {
				if (p.left != null) {
					p = p.left;
				} else {
					return p;
				}
			} else {
				if (p.right != null) {
					p = p.right;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.right) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			}
		}
		return null;
	}

	final Entry<K, V> getLowerEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp > 0) {
				if (p.right != null) {
					p = p.right;
				} else {
					return p;
				}
			} else {
				if (p.left != null) {
					p = p.left;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.left) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			}
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		Entry<K, V> rootEntry = root;
		if (rootEntry == null) {
			compare(key, key); // type (and possibly null) check

			root = new Entry<>(key, value, null);
			size = 1;
			modCount++;
			return null;
		}
		int compareResult;
		Entry<K, V> parent;
		// split comparator and comparable paths
		Comparator<? super K> currentComparator = comparator;
		if (currentComparator != null) {
			do {
				parent = rootEntry;
				compareResult = currentComparator.compare(key, rootEntry.key);
				if (compareResult < 0) {
					rootEntry = rootEntry.left;
				} else if (compareResult > 0) {
					rootEntry = rootEntry.right;
				} else {
					return rootEntry.setValue(value);
				}
			} while (rootEntry != null);
		} else {
			if (key == null) {
				throw new NullPointerException();
			}
			@SuppressWarnings("unchecked")
			Comparable<? super K> comparableKey = (Comparable<? super K>) key;
			do {
				parent = rootEntry;
				compareResult = comparableKey.compareTo(rootEntry.key);
				if (compareResult < 0) {
					rootEntry = rootEntry.left;
				} else if (compareResult > 0) {
					rootEntry = rootEntry.right;
				} else {
					return rootEntry.setValue(value);
				}
			} while (rootEntry != null);
		}
		Entry<K, V> e = new Entry<>(key, value, parent);
		if (compareResult < 0) {
			parent.left = e;
		} else {
			parent.right = e;
		}
		fixAfterInsertion(e);
		size++;
		modCount++;
		return null;
	}

	@Override
	public V remove(Object key) {
		Entry<K, V> p = getEntry(key);
		if (p == null) {
			return null;
		}

		V oldValue = p.value;
		deleteEntry(p);
		return oldValue;
	}

	@Override
	public void clear() {
		modCount++;
		size = 0;
		root = null;
	}

	@Override
	public Object clone() {
		MyTreeMap<?, ?> clone;
		try {
			clone = (MyTreeMap<?, ?>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}

		// Put clone into "virgin" state (except for comparator)
		clone.root = null;
		clone.size = 0;
		clone.modCount = 0;
		clone.entrySet = null;
		clone.navigableKeySet = null;
		clone.descendingMap = null;

		// Initialize clone with our mappings
		try {
			clone.buildFromSorted(size, entrySet().iterator(), null, null);
		} catch (java.io.IOException cannotHappen) {

		} catch (ClassNotFoundException cannotHappen) {

		}

		return clone;
	}

	// NavigableMap API methods

	@Override
	public Map.Entry<K, V> firstEntry() {
		return exportEntry(getFirstEntry());
	}

	@Override
	public Map.Entry<K, V> lastEntry() {
		return exportEntry(getLastEntry());
	}

	@Override
	public Map.Entry<K, V> pollFirstEntry() {
		Entry<K, V> p = getFirstEntry();
		Map.Entry<K, V> result = exportEntry(p);
		if (p != null) {
			deleteEntry(p);
		}
		return result;
	}

	@Override
	public Map.Entry<K, V> pollLastEntry() {
		Entry<K, V> p = getLastEntry();
		Map.Entry<K, V> result = exportEntry(p);
		if (p != null) {
			deleteEntry(p);
		}
		return result;
	}

	@Override
	public Map.Entry<K, V> lowerEntry(K key) {
		return exportEntry(getLowerEntry(key));
	}

	@Override
	public K lowerKey(K key) {
		return keyOrNull(getLowerEntry(key));
	}

	@Override
	public Map.Entry<K, V> floorEntry(K key) {
		return exportEntry(getFloorEntry(key));
	}

	@Override
	public K floorKey(K key) {
		return keyOrNull(getFloorEntry(key));
	}

	@Override
	public Map.Entry<K, V> ceilingEntry(K key) {
		return exportEntry(getCeilingEntry(key));
	}

	@Override
	public K ceilingKey(K key) {
		return keyOrNull(getCeilingEntry(key));
	}

	@Override
	public Map.Entry<K, V> higherEntry(K key) {
		return exportEntry(getHigherEntry(key));
	}

	@Override
	public K higherKey(K key) {
		return keyOrNull(getHigherEntry(key));
	}

	private transient EntrySet entrySet = null;
	private transient KeySet<K> navigableKeySet = null;
	private transient NavigableMap<K, V> descendingMap = null;

	@Override
	public Set<K> keySet() {
		return navigableKeySet();
	}

	@Override
	public NavigableSet<K> navigableKeySet() {
		KeySet<K> nks = navigableKeySet;
		return (nks != null) ? nks : (navigableKeySet = new KeySet<>(this));
	}

	@Override
	public NavigableSet<K> descendingKeySet() {
		return descendingMap().navigableKeySet();
	}

	@Override
	public Collection<V> values() {
		Collection<V> vs = values;
		return (vs != null) ? vs : (values = new Values());
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		EntrySet es = entrySet;
		return (es != null) ? es : (entrySet = new EntrySet());
	}

	@Override
	public NavigableMap<K, V> descendingMap() {
		NavigableMap<K, V> km = descendingMap;
		return (km != null) ? km : (descendingMap = new DescendingSubMap<>(this, true, null, true, true, null, true));
	}

	@Override
	public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		return new AscendingSubMap<>(this, false, fromKey, fromInclusive, false, toKey, toInclusive);
	}

	@Override
	public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
		return new AscendingSubMap<>(this, true, null, true, false, toKey, inclusive);
	}

	@Override
	public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
		return new AscendingSubMap<>(this, false, fromKey, inclusive, true, null, true);
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return subMap(fromKey, true, toKey, false);
	}

	@Override
	public SortedMap<K, V> headMap(K toKey) {
		return headMap(toKey, false);
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		return tailMap(fromKey, true);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		Entry<K, V> p = getEntry(key);
		if (p != null && Objects.equals(oldValue, p.value)) {
			p.value = newValue;
			return true;
		}
		return false;
	}

	@Override
	public V replace(K key, V value) {
		Entry<K, V> p = getEntry(key);
		if (p != null) {
			V oldValue = p.value;
			p.value = value;
			return oldValue;
		}
		return null;
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		Objects.requireNonNull(action);
		int expectedModCount = modCount;
		for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
			action.accept(e.key, e.value);

			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
		}
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		Objects.requireNonNull(function);
		int expectedModCount = modCount;

		for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
			e.value = function.apply(e.key, e.value);

			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
		}
	}

	// View class support

	class Values extends AbstractCollection<V> {

		@Override
		public Iterator<V> iterator() {
			return new ValueIterator(getFirstEntry());
		}

		@Override
		public int size() {
			return MyTreeMap.this.size();
		}

		@Override
		public boolean contains(Object o) {
			return MyTreeMap.this.containsValue(o);
		}

		@Override
		public boolean remove(Object o) {
			for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
				if (valEquals(e.getValue(), o)) {
					deleteEntry(e);
					return true;
				}
			}
			return false;
		}

		@Override
		public void clear() {
			MyTreeMap.this.clear();
		}

		@Override
		public Spliterator<V> spliterator() {
			return new ValueSpliterator<K, V>(MyTreeMap.this, null, null, 0, -1, 0);
		}
	}

	class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator(getFirstEntry());
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
			Object value = entry.getValue();
			Entry<K, V> p = getEntry(entry.getKey());
			return p != null && valEquals(p.getValue(), value);
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
			Object value = entry.getValue();
			Entry<K, V> p = getEntry(entry.getKey());
			if (p != null && valEquals(p.getValue(), value)) {
				deleteEntry(p);
				return true;
			}
			return false;
		}

		@Override
		public int size() {
			return MyTreeMap.this.size();
		}

		@Override
		public void clear() {
			MyTreeMap.this.clear();
		}

		@Override
		public Spliterator<Map.Entry<K, V>> spliterator() {
			return new EntrySpliterator<K, V>(MyTreeMap.this, null, null, 0, -1, 0);
		}
	}

	Iterator<K> keyIterator() {
		return new KeyIterator(getFirstEntry());
	}

	Iterator<K> descendingKeyIterator() {
		return new DescendingKeyIterator(getLastEntry());
	}

	static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
		private final NavigableMap<E, ?> m;

		KeySet(NavigableMap<E, ?> map) {
			m = map;
		}

		@Override
		public Iterator<E> iterator() {
			if (m instanceof MyTreeMap) {
				return ((MyTreeMap<E, ?>) m).keyIterator();
			} else {
				return ((MyTreeMap.NavigableSubMap<E, ?>) m).keyIterator();
			}
		}

		@Override
		public Iterator<E> descendingIterator() {
			if (m instanceof MyTreeMap) {
				return ((MyTreeMap<E, ?>) m).descendingKeyIterator();
			} else {
				return ((MyTreeMap.NavigableSubMap<E, ?>) m).descendingKeyIterator();
			}
		}

		@Override
		public int size() {
			return m.size();
		}

		@Override
		public boolean isEmpty() {
			return m.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return m.containsKey(o);
		}

		@Override
		public void clear() {
			m.clear();
		}

		@Override
		public E lower(E e) {
			return m.lowerKey(e);
		}

		@Override
		public E floor(E e) {
			return m.floorKey(e);
		}

		@Override
		public E ceiling(E e) {
			return m.ceilingKey(e);
		}

		@Override
		public E higher(E e) {
			return m.higherKey(e);
		}

		@Override
		public E first() {
			return m.firstKey();
		}

		@Override
		public E last() {
			return m.lastKey();
		}

		@Override
		public Comparator<? super E> comparator() {
			return m.comparator();
		}

		@Override
		public E pollFirst() {
			Map.Entry<E, ?> e = m.pollFirstEntry();
			return (e == null) ? null : e.getKey();
		}

		@Override
		public E pollLast() {
			Map.Entry<E, ?> e = m.pollLastEntry();
			return (e == null) ? null : e.getKey();
		}

		@Override
		public boolean remove(Object o) {
			int oldSize = size();
			m.remove(o);
			return size() != oldSize;
		}

		@Override
		public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
			return new KeySet<>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
		}

		@Override
		public NavigableSet<E> headSet(E toElement, boolean inclusive) {
			return new KeySet<>(m.headMap(toElement, inclusive));
		}

		@Override
		public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
			return new KeySet<>(m.tailMap(fromElement, inclusive));
		}

		@Override
		public SortedSet<E> subSet(E fromElement, E toElement) {
			return subSet(fromElement, true, toElement, false);
		}

		@Override
		public SortedSet<E> headSet(E toElement) {
			return headSet(toElement, false);
		}

		@Override
		public SortedSet<E> tailSet(E fromElement) {
			return tailSet(fromElement, true);
		}

		@Override
		public NavigableSet<E> descendingSet() {
			return new KeySet<>(m.descendingMap());
		}

		@Override
		public Spliterator<E> spliterator() {
			return keySpliteratorFor(m);
		}
	}

	abstract class PrivateEntryIterator<T> implements Iterator<T> {
		Entry<K, V> next;
		Entry<K, V> lastReturned;
		int expectedModCount;

		PrivateEntryIterator(Entry<K, V> first) {
			expectedModCount = modCount;
			lastReturned = null;
			next = first;
		}

		@Override
		public final boolean hasNext() {
			return next != null;
		}

		final Entry<K, V> nextEntry() {
			Entry<K, V> e = next;
			if (e == null) {
				throw new NoSuchElementException();
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			next = successor(e);
			lastReturned = e;
			return e;
		}

		final Entry<K, V> prevEntry() {
			Entry<K, V> e = next;
			if (e == null) {
				throw new NoSuchElementException();
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			next = predecessor(e);
			lastReturned = e;
			return e;
		}

		@Override
		public void remove() {
			if (lastReturned == null) {
				throw new IllegalStateException();
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			// deleted entries are replaced by their successors
			if (lastReturned.left != null && lastReturned.right != null) {
				next = lastReturned;
			}
			deleteEntry(lastReturned);
			expectedModCount = modCount;
			lastReturned = null;
		}
	}

	final class EntryIterator extends PrivateEntryIterator<Map.Entry<K, V>> {
		EntryIterator(Entry<K, V> first) {
			super(first);
		}

		@Override
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	final class ValueIterator extends PrivateEntryIterator<V> {
		ValueIterator(Entry<K, V> first) {
			super(first);
		}

		@Override
		public V next() {
			return nextEntry().value;
		}
	}

	final class KeyIterator extends PrivateEntryIterator<K> {
		KeyIterator(Entry<K, V> first) {
			super(first);
		}

		@Override
		public K next() {
			return nextEntry().key;
		}
	}

	final class DescendingKeyIterator extends PrivateEntryIterator<K> {
		DescendingKeyIterator(Entry<K, V> first) {
			super(first);
		}

		@Override
		public K next() {
			return prevEntry().key;
		}

		@Override
		public void remove() {
			if (lastReturned == null) {
				throw new IllegalStateException();
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			deleteEntry(lastReturned);
			lastReturned = null;
			expectedModCount = modCount;
		}
	}

	// Little utilities

	@SuppressWarnings("unchecked")
	final int compare(Object k1, Object k2) {
		return comparator == null ? ((Comparable<? super K>) k1).compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
	}

	static final boolean valEquals(Object o1, Object o2) {
		return (o1 == null ? o2 == null : o1.equals(o2));
	}

	static <K, V> Map.Entry<K, V> exportEntry(MyTreeMap.Entry<K, V> e) {
		return (e == null) ? null : new AbstractMap.SimpleImmutableEntry<>(e);
	}

	static <K, V> K keyOrNull(MyTreeMap.Entry<K, V> e) {
		return (e == null) ? null : e.key;
	}

	static <K> K key(Entry<K, ?> e) {
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e.key;
	}

	// SubMaps

	private static final Object UNBOUNDED = new Object();

	abstract static class NavigableSubMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Serializable {
		private static final long serialVersionUID = -654630310449926904L;

		final MyTreeMap<K, V> m;

		final K lo, hi;
		final boolean fromStart, toEnd;
		final boolean loInclusive, hiInclusive;

		NavigableSubMap(MyTreeMap<K, V> m, boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
			if (!fromStart && !toEnd) {
				if (m.compare(lo, hi) > 0) {
					throw new IllegalArgumentException("fromKey > toKey");
				}
			} else {
				if (!fromStart) { // type check
					m.compare(lo, lo);
				}
				if (!toEnd) {
					m.compare(hi, hi);
				}
			}

			this.m = m;
			this.fromStart = fromStart;
			this.lo = lo;
			this.loInclusive = loInclusive;
			this.toEnd = toEnd;
			this.hi = hi;
			this.hiInclusive = hiInclusive;
		}

		// internal utilities

		final boolean tooLow(Object key) {
			if (!fromStart) {
				int c = m.compare(key, lo);
				if (c < 0 || (c == 0 && !loInclusive)) {
					return true;
				}
			}
			return false;
		}

		final boolean tooHigh(Object key) {
			if (!toEnd) {
				int c = m.compare(key, hi);
				if (c > 0 || (c == 0 && !hiInclusive)) {
					return true;
				}
			}
			return false;
		}

		final boolean inRange(Object key) {
			return !tooLow(key) && !tooHigh(key);
		}

		final boolean inClosedRange(Object key) {
			return (fromStart || m.compare(key, lo) >= 0) && (toEnd || m.compare(hi, key) >= 0);
		}

		final boolean inRange(Object key, boolean inclusive) {
			return inclusive ? inRange(key) : inClosedRange(key);
		}

		final MyTreeMap.Entry<K, V> absLowest() {
			MyTreeMap.Entry<K, V> e = (fromStart ? m.getFirstEntry() : (loInclusive ? m.getCeilingEntry(lo) : m.getHigherEntry(lo)));
			return (e == null || tooHigh(e.key)) ? null : e;
		}

		final MyTreeMap.Entry<K, V> absHighest() {
			MyTreeMap.Entry<K, V> e = (toEnd ? m.getLastEntry() : (hiInclusive ? m.getFloorEntry(hi) : m.getLowerEntry(hi)));
			return (e == null || tooLow(e.key)) ? null : e;
		}

		final MyTreeMap.Entry<K, V> absCeiling(K key) {
			if (tooLow(key)) {
				return absLowest();
			}
			MyTreeMap.Entry<K, V> e = m.getCeilingEntry(key);
			return (e == null || tooHigh(e.key)) ? null : e;
		}

		final MyTreeMap.Entry<K, V> absHigher(K key) {
			if (tooLow(key)) {
				return absLowest();
			}
			MyTreeMap.Entry<K, V> e = m.getHigherEntry(key);
			return (e == null || tooHigh(e.key)) ? null : e;
		}

		final MyTreeMap.Entry<K, V> absFloor(K key) {
			if (tooHigh(key)) {
				return absHighest();
			}
			MyTreeMap.Entry<K, V> e = m.getFloorEntry(key);
			return (e == null || tooLow(e.key)) ? null : e;
		}

		final MyTreeMap.Entry<K, V> absLower(K key) {
			if (tooHigh(key)) {
				return absHighest();
			}
			MyTreeMap.Entry<K, V> e = m.getLowerEntry(key);
			return (e == null || tooLow(e.key)) ? null : e;
		}

		final MyTreeMap.Entry<K, V> absHighFence() {
			return (toEnd ? null : (hiInclusive ? m.getHigherEntry(hi) : m.getCeilingEntry(hi)));
		}

		final MyTreeMap.Entry<K, V> absLowFence() {
			return (fromStart ? null : (loInclusive ? m.getLowerEntry(lo) : m.getFloorEntry(lo)));
		}

		abstract MyTreeMap.Entry<K, V> subLowest();

		abstract MyTreeMap.Entry<K, V> subHighest();

		abstract MyTreeMap.Entry<K, V> subCeiling(K key);

		abstract MyTreeMap.Entry<K, V> subHigher(K key);

		abstract MyTreeMap.Entry<K, V> subFloor(K key);

		abstract MyTreeMap.Entry<K, V> subLower(K key);

		abstract Iterator<K> keyIterator();

		abstract Spliterator<K> keySpliterator();

		abstract Iterator<K> descendingKeyIterator();

		@Override
		public boolean isEmpty() {
			return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
		}

		@Override
		public int size() {
			return (fromStart && toEnd) ? m.size() : entrySet().size();
		}

		@Override
		public final boolean containsKey(Object key) {
			return inRange(key) && m.containsKey(key);
		}

		@Override
		public final V put(K key, V value) {
			if (!inRange(key)) {
				throw new IllegalArgumentException("key out of range");
			}
			return m.put(key, value);
		}

		@Override
		public final V get(Object key) {
			return !inRange(key) ? null : m.get(key);
		}

		@Override
		public final V remove(Object key) {
			return !inRange(key) ? null : m.remove(key);
		}

		@Override
		public final Map.Entry<K, V> ceilingEntry(K key) {
			return exportEntry(subCeiling(key));
		}

		@Override
		public final K ceilingKey(K key) {
			return keyOrNull(subCeiling(key));
		}

		@Override
		public final Map.Entry<K, V> higherEntry(K key) {
			return exportEntry(subHigher(key));
		}

		@Override
		public final K higherKey(K key) {
			return keyOrNull(subHigher(key));
		}

		@Override
		public final Map.Entry<K, V> floorEntry(K key) {
			return exportEntry(subFloor(key));
		}

		@Override
		public final K floorKey(K key) {
			return keyOrNull(subFloor(key));
		}

		@Override
		public final Map.Entry<K, V> lowerEntry(K key) {
			return exportEntry(subLower(key));
		}

		@Override
		public final K lowerKey(K key) {
			return keyOrNull(subLower(key));
		}

		@Override
		public final K firstKey() {
			return key(subLowest());
		}

		@Override
		public final K lastKey() {
			return key(subHighest());
		}

		@Override
		public final Map.Entry<K, V> firstEntry() {
			return exportEntry(subLowest());
		}

		@Override
		public final Map.Entry<K, V> lastEntry() {
			return exportEntry(subHighest());
		}

		@Override
		public final Map.Entry<K, V> pollFirstEntry() {
			MyTreeMap.Entry<K, V> e = subLowest();
			Map.Entry<K, V> result = exportEntry(e);
			if (e != null) {
				m.deleteEntry(e);
			}
			return result;
		}

		@Override
		public final Map.Entry<K, V> pollLastEntry() {
			MyTreeMap.Entry<K, V> e = subHighest();
			Map.Entry<K, V> result = exportEntry(e);
			if (e != null) {
				m.deleteEntry(e);
			}
			return result;
		}

		// Views
		transient NavigableMap<K, V> descendingMapView = null;
		transient EntrySetView entrySetView = null;
		transient KeySet<K> navigableKeySetView = null;

		@Override
		public final NavigableSet<K> navigableKeySet() {
			KeySet<K> nksv = navigableKeySetView;
			return (nksv != null) ? nksv : (navigableKeySetView = new MyTreeMap.KeySet<>(this));
		}

		@Override
		public final Set<K> keySet() {
			return navigableKeySet();
		}

		@Override
		public NavigableSet<K> descendingKeySet() {
			return descendingMap().navigableKeySet();
		}

		@Override
		public final SortedMap<K, V> subMap(K fromKey, K toKey) {
			return subMap(fromKey, true, toKey, false);
		}

		@Override
		public final SortedMap<K, V> headMap(K toKey) {
			return headMap(toKey, false);
		}

		@Override
		public final SortedMap<K, V> tailMap(K fromKey) {
			return tailMap(fromKey, true);
		}

		// View classes

		abstract class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
			private transient int size = -1, sizeModCount;

			@Override
			public int size() {
				if (fromStart && toEnd) {
					return m.size();
				}
				if (size == -1 || sizeModCount != m.modCount) {
					sizeModCount = m.modCount;
					size = 0;
					Iterator<?> i = iterator();
					while (i.hasNext()) {
						size++;
						i.next();
					}
				}
				return size;
			}

			@Override
			public boolean isEmpty() {
				MyTreeMap.Entry<K, V> n = absLowest();
				return n == null || tooHigh(n.key);
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry)) {
					return false;
				}
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
				Object key = entry.getKey();
				if (!inRange(key)) {
					return false;
				}
				MyTreeMap.Entry<?, ?> node = m.getEntry(key);
				return node != null && valEquals(node.getValue(), entry.getValue());
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry)) {
					return false;
				}
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
				Object key = entry.getKey();
				if (!inRange(key)) {
					return false;
				}
				MyTreeMap.Entry<K, V> node = m.getEntry(key);
				if (node != null && valEquals(node.getValue(), entry.getValue())) {
					m.deleteEntry(node);
					return true;
				}
				return false;
			}
		}

		abstract class SubMapIterator<T> implements Iterator<T> {
			MyTreeMap.Entry<K, V> lastReturned;
			MyTreeMap.Entry<K, V> next;
			final Object fenceKey;
			int expectedModCount;

			SubMapIterator(MyTreeMap.Entry<K, V> first, MyTreeMap.Entry<K, V> fence) {
				expectedModCount = m.modCount;
				lastReturned = null;
				next = first;
				fenceKey = fence == null ? UNBOUNDED : fence.key;
			}

			@Override
			public final boolean hasNext() {
				return next != null && next.key != fenceKey;
			}

			final MyTreeMap.Entry<K, V> nextEntry() {
				MyTreeMap.Entry<K, V> e = next;
				if (e == null || e.key == fenceKey) {
					throw new NoSuchElementException();
				}
				if (m.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				next = successor(e);
				lastReturned = e;
				return e;
			}

			final MyTreeMap.Entry<K, V> prevEntry() {
				MyTreeMap.Entry<K, V> e = next;
				if (e == null || e.key == fenceKey) {
					throw new NoSuchElementException();
				}
				if (m.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				next = predecessor(e);
				lastReturned = e;
				return e;
			}

			final void removeAscending() {
				if (lastReturned == null) {
					throw new IllegalStateException();
				}
				if (m.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				// deleted entries are replaced by their successors
				if (lastReturned.left != null && lastReturned.right != null) {
					next = lastReturned;
				}
				m.deleteEntry(lastReturned);
				lastReturned = null;
				expectedModCount = m.modCount;
			}

			final void removeDescending() {
				if (lastReturned == null) {
					throw new IllegalStateException();
				}
				if (m.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				m.deleteEntry(lastReturned);
				lastReturned = null;
				expectedModCount = m.modCount;
			}
		}

		final class SubMapEntryIterator extends SubMapIterator<Map.Entry<K, V>> {
			SubMapEntryIterator(MyTreeMap.Entry<K, V> first, MyTreeMap.Entry<K, V> fence) {
				super(first, fence);
			}

			@Override
			public Map.Entry<K, V> next() {
				return nextEntry();
			}

			@Override
			public void remove() {
				removeAscending();
			}
		}

		final class DescendingSubMapEntryIterator extends SubMapIterator<Map.Entry<K, V>> {
			DescendingSubMapEntryIterator(MyTreeMap.Entry<K, V> last, MyTreeMap.Entry<K, V> fence) {
				super(last, fence);
			}

			@Override
			public Map.Entry<K, V> next() {
				return prevEntry();
			}

			@Override
			public void remove() {
				removeDescending();
			}
		}

		final class SubMapKeyIterator extends SubMapIterator<K> implements Spliterator<K> {
			SubMapKeyIterator(MyTreeMap.Entry<K, V> first, MyTreeMap.Entry<K, V> fence) {
				super(first, fence);
			}

			@Override
			public K next() {
				return nextEntry().key;
			}

			@Override
			public void remove() {
				removeAscending();
			}

			@Override
			public Spliterator<K> trySplit() {
				return null;
			}

			@Override
			public void forEachRemaining(Consumer<? super K> action) {
				while (hasNext()) {
					action.accept(next());
				}
			}

			@Override
			public boolean tryAdvance(Consumer<? super K> action) {
				if (hasNext()) {
					action.accept(next());
					return true;
				}
				return false;
			}

			@Override
			public long estimateSize() {
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics() {
				return Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED;
			}

			@Override
			public final Comparator<? super K> getComparator() {
				return NavigableSubMap.this.comparator();
			}
		}

		final class DescendingSubMapKeyIterator extends SubMapIterator<K> implements Spliterator<K> {
			DescendingSubMapKeyIterator(MyTreeMap.Entry<K, V> last, MyTreeMap.Entry<K, V> fence) {
				super(last, fence);
			}

			@Override
			public K next() {
				return prevEntry().key;
			}

			@Override
			public void remove() {
				removeDescending();
			}

			@Override
			public Spliterator<K> trySplit() {
				return null;
			}

			@Override
			public void forEachRemaining(Consumer<? super K> action) {
				while (hasNext()) {
					action.accept(next());
				}
			}

			@Override
			public boolean tryAdvance(Consumer<? super K> action) {
				if (hasNext()) {
					action.accept(next());
					return true;
				}
				return false;
			}

			@Override
			public long estimateSize() {
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics() {
				return Spliterator.DISTINCT | Spliterator.ORDERED;
			}
		}
	}

	static final class AscendingSubMap<K, V> extends NavigableSubMap<K, V> {
		private static final long serialVersionUID = 912986545866124060L;

		AscendingSubMap(MyTreeMap<K, V> m, boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
			super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}

		@Override
		public Comparator<? super K> comparator() {
			return m.comparator();
		}

		@Override
		public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive)) {
				throw new IllegalArgumentException("fromKey out of range");
			}
			if (!inRange(toKey, toInclusive)) {
				throw new IllegalArgumentException("toKey out of range");
			}
			return new AscendingSubMap<>(m, false, fromKey, fromInclusive, false, toKey, toInclusive);
		}

		@Override
		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive)) {
				throw new IllegalArgumentException("toKey out of range");
			}
			return new AscendingSubMap<>(m, fromStart, lo, loInclusive, false, toKey, inclusive);
		}

		@Override
		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive)) {
				throw new IllegalArgumentException("fromKey out of range");
			}
			return new AscendingSubMap<>(m, false, fromKey, inclusive, toEnd, hi, hiInclusive);
		}

		@Override
		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv : (descendingMapView = new DescendingSubMap<>(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive));
		}

		@Override
		Iterator<K> keyIterator() {
			return new SubMapKeyIterator(absLowest(), absHighFence());
		}

		@Override
		Spliterator<K> keySpliterator() {
			return new SubMapKeyIterator(absLowest(), absHighFence());
		}

		@Override
		Iterator<K> descendingKeyIterator() {
			return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
		}

		final class AscendingEntrySetView extends EntrySetView {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new SubMapEntryIterator(absLowest(), absHighFence());
			}
		}

		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : (entrySetView = new AscendingEntrySetView());
		}

		@Override
		MyTreeMap.Entry<K, V> subLowest() {
			return absLowest();
		}

		@Override
		MyTreeMap.Entry<K, V> subHighest() {
			return absHighest();
		}

		@Override
		MyTreeMap.Entry<K, V> subCeiling(K key) {
			return absCeiling(key);
		}

		@Override
		MyTreeMap.Entry<K, V> subHigher(K key) {
			return absHigher(key);
		}

		@Override
		MyTreeMap.Entry<K, V> subFloor(K key) {
			return absFloor(key);
		}

		@Override
		MyTreeMap.Entry<K, V> subLower(K key) {
			return absLower(key);
		}
	}

	static final class DescendingSubMap<K, V> extends NavigableSubMap<K, V> {
		private static final long serialVersionUID = 912986545866120460L;

		DescendingSubMap(MyTreeMap<K, V> m, boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
			super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}

		private final Comparator<? super K> reverseComparator = Collections.reverseOrder(m.comparator);

		@Override
		public Comparator<? super K> comparator() {
			return reverseComparator;
		}

		@Override
		public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive)) {
				throw new IllegalArgumentException("fromKey out of range");
			}
			if (!inRange(toKey, toInclusive)) {
				throw new IllegalArgumentException("toKey out of range");
			}
			return new DescendingSubMap<>(m, false, toKey, toInclusive, false, fromKey, fromInclusive);
		}

		@Override
		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive)) {
				throw new IllegalArgumentException("toKey out of range");
			}
			return new DescendingSubMap<>(m, false, toKey, inclusive, toEnd, hi, hiInclusive);
		}

		@Override
		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive)) {
				throw new IllegalArgumentException("fromKey out of range");
			}
			return new DescendingSubMap<>(m, fromStart, lo, loInclusive, false, fromKey, inclusive);
		}

		@Override
		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv : (descendingMapView = new AscendingSubMap<>(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive));
		}

		@Override
		Iterator<K> keyIterator() {
			return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
		}

		@Override
		Spliterator<K> keySpliterator() {
			return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
		}

		@Override
		Iterator<K> descendingKeyIterator() {
			return new SubMapKeyIterator(absLowest(), absHighFence());
		}

		final class DescendingEntrySetView extends EntrySetView {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new DescendingSubMapEntryIterator(absHighest(), absLowFence());
			}
		}

		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : (entrySetView = new DescendingEntrySetView());
		}

		@Override
		MyTreeMap.Entry<K, V> subLowest() {
			return absHighest();
		}

		@Override
		MyTreeMap.Entry<K, V> subHighest() {
			return absLowest();
		}

		@Override
		MyTreeMap.Entry<K, V> subCeiling(K key) {
			return absFloor(key);
		}

		@Override
		MyTreeMap.Entry<K, V> subHigher(K key) {
			return absLower(key);
		}

		@Override
		MyTreeMap.Entry<K, V> subFloor(K key) {
			return absCeiling(key);
		}

		@Override
		MyTreeMap.Entry<K, V> subLower(K key) {
			return absHigher(key);
		}
	}

	@SuppressWarnings("unused")
	private class SubMap extends AbstractMap<K, V> implements SortedMap<K, V>, Serializable {
		private static final long serialVersionUID = -6520786458950516097L;
		private boolean fromStart = false, toEnd = false;
		private K fromKey, toKey;

		private Object readResolve() {
			return new AscendingSubMap<>(MyTreeMap.this, fromStart, fromKey, true, toEnd, toKey, false);
		}

		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			throw new InternalError();
		}

		@Override
		public K lastKey() {
			throw new InternalError();
		}

		@Override
		public K firstKey() {
			throw new InternalError();
		}

		@Override
		public SortedMap<K, V> subMap(K fromKey, K toKey) {
			throw new InternalError();
		}

		@Override
		public SortedMap<K, V> headMap(K toKey) {
			throw new InternalError();
		}

		@Override
		public SortedMap<K, V> tailMap(K fromKey) {
			throw new InternalError();
		}

		@Override
		public Comparator<? super K> comparator() {
			throw new InternalError();
		}
	}

	// Red-black mechanics

	private static final boolean RED = false;
	private static final boolean BLACK = true;

	static final class Entry<K, V> implements Map.Entry<K, V> {
		K key;
		V value;
		Entry<K, V> left = null;
		Entry<K, V> right = null;
		Entry<K, V> parent;
		boolean color = BLACK;

		Entry(K key, V value, Entry<K, V> parent) {
			this.key = key;
			this.value = value;
			this.parent = parent;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

			return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
		}

		@Override
		public int hashCode() {
			int keyHash = (key == null ? 0 : key.hashCode());
			int valueHash = (value == null ? 0 : value.hashCode());
			return keyHash ^ valueHash;
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}

	final Entry<K, V> getFirstEntry() {
		Entry<K, V> p = root;
		if (p != null) {
			while (p.left != null) {
				p = p.left;
			}
		}
		return p;
	}

	final Entry<K, V> getLastEntry() {
		Entry<K, V> p = root;
		if (p != null) {
			while (p.right != null) {
				p = p.right;
			}
		}
		return p;
	}

	static <K, V> MyTreeMap.Entry<K, V> successor(Entry<K, V> t) {
		if (t == null) {
			return null;
		} else if (t.right != null) {
			Entry<K, V> p = t.right;
			while (p.left != null) {
				p = p.left;
			}
			return p;
		} else {
			Entry<K, V> p = t.parent;
			Entry<K, V> ch = t;
			while (p != null && ch == p.right) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}

	/**
	 * Returns the predecessor of the specified Entry, or null if no such.
	 */
	static <K, V> Entry<K, V> predecessor(Entry<K, V> t) {
		if (t == null) {
			return null;
		} else if (t.left != null) {
			Entry<K, V> p = t.left;
			while (p.right != null) {
				p = p.right;
			}
			return p;
		} else {
			Entry<K, V> p = t.parent;
			Entry<K, V> ch = t;
			while (p != null && ch == p.left) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}

	private static <K, V> boolean colorOf(Entry<K, V> p) {
		return (p == null ? BLACK : p.color);
	}

	private static <K, V> Entry<K, V> parentOf(Entry<K, V> p) {
		return (p == null ? null : p.parent);
	}

	private static <K, V> void setColor(Entry<K, V> p, boolean c) {
		if (p != null) {
			p.color = c;
		}
	}

	private static <K, V> Entry<K, V> leftOf(Entry<K, V> p) {
		return (p == null) ? null : p.left;
	}

	private static <K, V> Entry<K, V> rightOf(Entry<K, V> p) {
		return (p == null) ? null : p.right;
	}

	/** From CLR */
	private void rotateLeft(Entry<K, V> p) {
		if (p != null) {
			Entry<K, V> r = p.right;
			p.right = r.left;
			if (r.left != null) {
				r.left.parent = p;
			}
			r.parent = p.parent;
			if (p.parent == null) {
				root = r;
			} else if (p.parent.left == p) {
				p.parent.left = r;
			} else {
				p.parent.right = r;
			}
			r.left = p;
			p.parent = r;
		}
	}

	/** From CLR */
	private void rotateRight(Entry<K, V> p) {
		if (p != null) {
			Entry<K, V> l = p.left;
			p.left = l.right;
			if (l.right != null) {
				l.right.parent = p;
			}
			l.parent = p.parent;
			if (p.parent == null) {
				root = l;
			} else if (p.parent.right == p) {
				p.parent.right = l;
			} else {
				p.parent.left = l;
			}
			l.right = p;
			p.parent = l;
		}
	}

	/** From CLR */
	private void fixAfterInsertion(Entry<K, V> x) {
		x.color = RED;

		while (x != null && x != root && x.parent.color == RED) {
			if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
				Entry<K, V> y = rightOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == rightOf(parentOf(x))) {
						x = parentOf(x);
						rotateLeft(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					rotateRight(parentOf(parentOf(x)));
				}
			} else {
				Entry<K, V> y = leftOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == leftOf(parentOf(x))) {
						x = parentOf(x);
						rotateRight(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					rotateLeft(parentOf(parentOf(x)));
				}
			}
		}
		root.color = BLACK;
	}

	private void deleteEntry(Entry<K, V> p) {
		modCount++;
		size--;

		if (p.left != null && p.right != null) {
			Entry<K, V> s = successor(p);
			p.key = s.key;
			p.value = s.value;
			p = s;
		} // p has 2 children

		// Start fixup at replacement node, if it exists.
		Entry<K, V> replacement = (p.left != null ? p.left : p.right);

		if (replacement != null) {
			// Link replacement to parent
			replacement.parent = p.parent;
			if (p.parent == null) {
				root = replacement;
			} else if (p == p.parent.left) {
				p.parent.left = replacement;
			} else {
				p.parent.right = replacement;
			}

			// Null out links so they are OK to use by fixAfterDeletion.
			p.left = p.right = p.parent = null;

			// Fix replacement
			if (p.color == BLACK) {
				fixAfterDeletion(replacement);
			}
		} else if (p.parent == null) { // return if we are the only node.
			root = null;
		} else { // No children. Use self as phantom replacement and unlink.
			if (p.color == BLACK) {
				fixAfterDeletion(p);
			}

			if (p.parent != null) {
				if (p == p.parent.left) {
					p.parent.left = null;
				} else if (p == p.parent.right) {
					p.parent.right = null;
				}
				p.parent = null;
			}
		}
	}

	/** From CLR */
	private void fixAfterDeletion(Entry<K, V> x) {
		while (x != root && colorOf(x) == BLACK) {
			if (x == leftOf(parentOf(x))) {
				Entry<K, V> sib = rightOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}

				if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(rightOf(sib)) == BLACK) {
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			} else { // symmetric
				Entry<K, V> sib = leftOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}

				if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(leftOf(sib)) == BLACK) {
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}

		setColor(x, BLACK);
	}

	private static final long serialVersionUID = 919286545866124006L;

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Write out the Comparator and any hidden stuff
		oos.defaultWriteObject();

		// Write out size (number of Mappings)
		oos.writeInt(size);

		// Write out keys and values (alternating)
		for (Iterator<Map.Entry<K, V>> i = entrySet().iterator(); i.hasNext();) {
			Map.Entry<K, V> e = i.next();
			oos.writeObject(e.getKey());
			oos.writeObject(e.getValue());
		}
	}

	private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Read in the Comparator and any hidden stuff
		ois.defaultReadObject();

		// Read in size
		int size = ois.readInt();

		buildFromSorted(size, null, ois, null);
	}

	void readTreeSet(int size, java.io.ObjectInputStream s, V defaultVal) throws IOException, ClassNotFoundException {
		buildFromSorted(size, null, s, defaultVal);
	}

	void addAllForTreeSet(SortedSet<? extends K> set, V defaultVal) {
		try {
			buildFromSorted(set.size(), set.iterator(), null, defaultVal);
		} catch (java.io.IOException cannotHappen) {

		} catch (ClassNotFoundException cannotHappen) {

		}
	}

	private void buildFromSorted(int size, Iterator<?> it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
		this.size = size;
		root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
	}

	@SuppressWarnings("unchecked")
	private final Entry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, 
		Iterator<?> it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
		if (hi < lo) {
			return null;
		}

		int mid = (lo + hi) >>> 1;

		Entry<K, V> left = null;
		if (lo < mid) {
			left = buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);
		}

		// extract key and/or value from iterator or stream
		K key;
		V value;
		if (it != null) {
			if (defaultVal == null) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
				key = (K) entry.getKey();
				value = (V) entry.getValue();
			} else {
				key = (K) it.next();
				value = defaultVal;
			}
		} else { // use stream
			key = (K) str.readObject();
			value = (defaultVal != null ? defaultVal : (V) str.readObject());
		}

		Entry<K, V> middle = new Entry<>(key, value, null);

		// color nodes in non-full bottommost level red
		if (level == redLevel) {
			middle.color = RED;
		}

		if (left != null) {
			middle.left = left;
			left.parent = middle;
		}

		if (mid < hi) {
			Entry<K, V> right = buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
			middle.right = right;
			right.parent = middle;
		}

		return middle;
	}

	private static int computeRedLevel(int sz) {
		int level = 0;
		for (int m = sz - 1; m >= 0; m = m / 2 - 1) {
			level++;
		}
		return level;
	}

	static <K> Spliterator<K> keySpliteratorFor(NavigableMap<K, ?> m) {
		if (m instanceof MyTreeMap) {
			@SuppressWarnings("unchecked")
			MyTreeMap<K, Object> t = (MyTreeMap<K, Object>) m;
			return t.keySpliterator();
		}
		if (m instanceof DescendingSubMap) {
			DescendingSubMap<K, ?> dm = (DescendingSubMap<K, ?>) m;
			MyTreeMap<K, ?> tm = dm.m;
			if (dm == tm.descendingMap) {
				@SuppressWarnings("unchecked")
				MyTreeMap<K, Object> t = (MyTreeMap<K, Object>) tm;
				return t.descendingKeySpliterator();
			}
		}

		NavigableSubMap<K, ?> sm = (NavigableSubMap<K, ?>) m;
		return sm.keySpliterator();
	}

	final Spliterator<K> keySpliterator() {
		return new KeySpliterator<K, V>(this, null, null, 0, -1, 0);
	}

	final Spliterator<K> descendingKeySpliterator() {
		return new DescendingKeySpliterator<K, V>(this, null, null, 0, -2, 0);
	}

	static class TreeMapSpliterator<K, V> {
		final MyTreeMap<K, V> tree;
		MyTreeMap.Entry<K, V> current; // traverser; initially first node in range
		MyTreeMap.Entry<K, V> fence; // one past last, or null
		int side; // 0: top, -1: is a left split, +1: right
		int est; // size estimate (exact only for top-level)
		int expectedModCount; // for CME checks

		/**
		 * 构造方法
		 */
		TreeMapSpliterator(MyTreeMap<K, V> tree, MyTreeMap.Entry<K, V> origin, MyTreeMap.Entry<K, V> fence, int side, int est, int expectedModCount) {
			this.tree = tree;
			this.current = origin;
			this.fence = fence;
			this.side = side;
			this.est = est;
			this.expectedModCount = expectedModCount;
		}

		final int getEstimate() { // force initialization
			int s;
			MyTreeMap<K, V> t;
			if ((s = est) < 0) {
				if ((t = tree) != null) {
					current = (s == -1) ? t.getFirstEntry() : t.getLastEntry();
					s = est = t.size;
					expectedModCount = t.modCount;
				} else {
					s = est = 0;
				}
			}
			return s;
		}

		public final long estimateSize() {
			return (long) getEstimate();
		}
	}

	static final class KeySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<K> {
		/**
		 * 构造方法
		 */
		KeySpliterator(MyTreeMap<K, V> tree, MyTreeMap.Entry<K, V> origin, MyTreeMap.Entry<K, V> fence, int side, int est, int expectedModCount) {
			super(tree, origin, fence, side, est, expectedModCount);
		}

		@Override
		public KeySpliterator<K, V> trySplit() {
			if (est < 0) {
				getEstimate(); // force initialization
			}

			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;
			MyTreeMap.Entry<K, V> s = getS();

			if (s != null && s != e && s != f && tree.compare(e.key, s.key) < 0) { // e not already past s
				side = 1;
				return new KeySpliterator<>(tree, e, current = s, -1, est >>>= 1, expectedModCount);
			}
			return null;
		}

		private MyTreeMap.Entry<K, V> getS() {
			int d = side;
			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;

			if (e == null || e == f) {
				return null;// empty
			}

			if (d == 0) {
				return tree.root;// was top
			}

			if (d > 0) {
				return e.right;// was right
			}

			if (d < 0 && f != null) {
				return f.left;// was left
			}

			return null;
		}

		@Override
		public void forEachRemaining(Consumer<? super K> action) {
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate(); // force initialization
			}
			MyTreeMap.Entry<K, V> f = fence, e, p, pl;
			if ((e = current) != null && e != f) {
				current = f; // exhaust
				do {
					action.accept(e.key);
					if ((p = e.right) != null) {
						while ((pl = p.left) != null) {
							p = pl;
						}
					} else {
						while ((p = e.parent) != null && e == p.right) {
							e = p;
						}
					}
				} while ((e = p) != null && e != f);
				if (tree.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
			}
		}

		@Override
		public boolean tryAdvance(Consumer<? super K> action) {
			MyTreeMap.Entry<K, V> e;
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate(); // force initialization
			}
			if ((e = current) == null || e == fence) {
				return false;
			}
			current = successor(e);
			action.accept(e.key);
			if (tree.modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			return true;
		}

		@Override
		public int characteristics() {
			return (side == 0 ? Spliterator.SIZED : 0) | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
		}

		@Override
		public final Comparator<? super K> getComparator() {
			return tree.comparator;
		}

	}

	static final class DescendingKeySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<K> {
		/**
		 * 构造方法
		 */
		DescendingKeySpliterator(MyTreeMap<K, V> tree, MyTreeMap.Entry<K, V> origin, MyTreeMap.Entry<K, V> fence, int side, int est, int expectedModCount) {
			super(tree, origin, fence, side, est, expectedModCount);
		}

		@Override
		public DescendingKeySpliterator<K, V> trySplit() {
			if (est < 0) {
				getEstimate(); // force initialization
			}

			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;
			MyTreeMap.Entry<K, V> s = getS();

			if (s != null && s != e && s != f && tree.compare(e.key, s.key) > 0) { // e not already past s
				side = 1;
				return new DescendingKeySpliterator<>(tree, e, current = s, -1, est >>>= 1, expectedModCount);
			}
			return null;
		}

		private MyTreeMap.Entry<K, V> getS() {
			int d = side;
			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;

			if (e == null || e == f) {
				return null;// empty
			}

			if (d == 0) {
				return tree.root;// was top
			}

			if (d < 0) {
				return e.left;// was left
			}

			if (d > 0 && f != null) {
				return f.right;// was right
			}

			return null;
		}

		@Override
		public void forEachRemaining(Consumer<? super K> action) {
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate(); // force initialization
			}
			MyTreeMap.Entry<K, V> f = fence, e, p, pr;
			if ((e = current) != null && e != f) {
				current = f; // exhaust
				do {
					action.accept(e.key);
					if ((p = e.left) != null) {
						while ((pr = p.right) != null) {
							p = pr;
						}
					} else {
						while ((p = e.parent) != null && e == p.left) {
							e = p;
						}
					}
				} while ((e = p) != null && e != f);
				if (tree.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
			}
		}

		@Override
		public boolean tryAdvance(Consumer<? super K> action) {
			MyTreeMap.Entry<K, V> e;
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate(); // force initialization
			}
			if ((e = current) == null || e == fence) {
				return false;
			}
			current = predecessor(e);
			action.accept(e.key);
			if (tree.modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			return true;
		}

		@Override
		public int characteristics() {
			return (side == 0 ? Spliterator.SIZED : 0) | Spliterator.DISTINCT | Spliterator.ORDERED;
		}
	}

	static final class ValueSpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<V> {
		/**
		 * 构造方法
		 */
		ValueSpliterator(MyTreeMap<K, V> tree, MyTreeMap.Entry<K, V> origin, MyTreeMap.Entry<K, V> fence, int side, int est, int expectedModCount) {
			super(tree, origin, fence, side, est, expectedModCount);
		}

		@Override
		public ValueSpliterator<K, V> trySplit() {
			if (est < 0) {
				getEstimate(); // force initialization
			}

			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;
			MyTreeMap.Entry<K, V> s = getS();

			if (s != null && s != e && s != f && tree.compare(e.key, s.key) < 0) { // e not already past s
				side = 1;
				return new ValueSpliterator<>(tree, e, current = s, -1, est >>>= 1, expectedModCount);
			}
			return null;
		}

		private MyTreeMap.Entry<K, V> getS() {
			int d = side;
			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;

			if (e == null || e == f) {
				return null;// empty
			}

			if (d == 0) {
				return tree.root;// was top
			}

			if (d > 0) {
				return e.right;// was right
			}

			if (d < 0 && f != null) {
				return f.left;// was left
			}

			return null;
		}

		@Override
		public void forEachRemaining(Consumer<? super V> action) {
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate(); // force initialization
			}
			MyTreeMap.Entry<K, V> f = fence, e, p, pl;
			if ((e = current) != null && e != f) {
				current = f; // exhaust
				do {
					action.accept(e.value);
					if ((p = e.right) != null) {
						while ((pl = p.left) != null) {
							p = pl;
						}
					} else {
						while ((p = e.parent) != null && e == p.right) {
							e = p;
						}
					}
				} while ((e = p) != null && e != f);
				if (tree.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
			}
		}

		@Override
		public boolean tryAdvance(Consumer<? super V> action) {
			MyTreeMap.Entry<K, V> e;
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate(); // force initialization
			}
			if ((e = current) == null || e == fence) {
				return false;
			}
			current = successor(e);
			action.accept(e.value);
			if (tree.modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			return true;
		}

		@Override
		public int characteristics() {
			return (side == 0 ? Spliterator.SIZED : 0) | Spliterator.ORDERED;
		}
	}

	static final class EntrySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
		/**
		 * 构造方法
		 */
		EntrySpliterator(MyTreeMap<K, V> tree, MyTreeMap.Entry<K, V> origin, MyTreeMap.Entry<K, V> fence, int side, int est, int expectedModCount) {
			super(tree, origin, fence, side, est, expectedModCount);
		}

		@Override
		public EntrySpliterator<K, V> trySplit() {
			if (est < 0) {
				getEstimate(); // force initialization
			}

			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;
			MyTreeMap.Entry<K, V> s = getS();

			if (s != null && s != e && s != f && tree.compare(e.key, s.key) < 0) { // e not already past s
				side = 1;
				return new EntrySpliterator<>(tree, e, current = s, -1, est >>>= 1, expectedModCount);
			}
			return null;
		}

		private MyTreeMap.Entry<K, V> getS() {
			int d = side;
			MyTreeMap.Entry<K, V> e = current;
			MyTreeMap.Entry<K, V> f = fence;

			if (e == null || e == f) { return null; 	 }// empty
			if (d == 0) 			 { return tree.root; }// top
			if (d > 0) 				 { return e.right; 	 }// right
			if (d < 0 && f != null)  { return f.left; 	 }// left

			return null;
		}

		@Override
		public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate(); // force initialization
			}
			MyTreeMap.Entry<K, V> f = fence, e, p, pl;
			if ((e = current) != null && e != f) {
				current = f; // exhaust
				do {
					action.accept(e);
					if ((p = e.right) != null) {
						while ((pl = p.left) != null) {
							p = pl;
						}
					} else {
						while ((p = e.parent) != null && e == p.right) {
							e = p;
						}
					}
				} while ((e = p) != null && e != f);

				if (tree.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
			}
		}

		@Override
		public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
			MyTreeMap.Entry<K, V> e;
			if (action == null) {
				throw new NullPointerException();
			}
			if (est < 0) {
				getEstimate();// force initialization
			}
			if ((e = current) == null || e == fence) {
				return false;
			}
			current = successor(e);
			action.accept(e);
			if (tree.modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			return true;
		}

		@Override
		public int characteristics() {
			return (side == 0 ? Spliterator.SIZED : 0) | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Comparator<Map.Entry<K, V>> getComparator() {
			// Adapt or create a key-based comparator
			if (tree.comparator != null) {
				return Map.Entry.comparingByKey(tree.comparator);
			}

			return (Comparator<Map.Entry<K, V>> & Serializable) (e1, e2) -> {
				Comparable<? super K> k1 = (Comparable<? super K>) e1.getKey();
				return k1.compareTo(e2.getKey());
			};
		}
	}

}