package com.simon.credit.toolkit.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;

public class MyTreeSet<E> extends MyAbstractSet<E> implements NavigableSet<E>, Cloneable, Serializable {
	private static final long serialVersionUID = -7970188436966143797L;

	private transient NavigableMap<E, Object> map;

	private static final Object PRESENT = new Object();

	MyTreeSet(NavigableMap<E, Object> map) {
		this.map = map;
	}

	public MyTreeSet() {
		this(new MyTreeMap<E, Object>());
	}

	public MyTreeSet(Comparator<? super E> comparator) {
		this(new MyTreeMap<>(comparator));
	}

	public MyTreeSet(Collection<? extends E> collection) {
		this();
		addAll(collection);
	}

	public MyTreeSet(SortedSet<E> sortedSet) {
		this(sortedSet.comparator());
		addAll(sortedSet);
	}

	@Override
	public Iterator<E> iterator() {
		return map.navigableKeySet().iterator();
	}

	@Override
	public Iterator<E> descendingIterator() {
		return map.descendingKeySet().iterator();
	}

	@Override
	public NavigableSet<E> descendingSet() {
		return new MyTreeSet<>(map.descendingMap());
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean add(E e) {
		return map.put(e, PRESENT) == null;
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) == PRESENT;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean addAll(Collection<? extends E> coll) {
		// Use linear-time version if applicable
		if (map.size() == 0 && coll.size() > 0 && coll instanceof SortedSet && map instanceof TreeMap) {
			SortedSet<? extends E> set = (SortedSet<? extends E>) coll;
			MyTreeMap<E, Object> map = (MyTreeMap<E, Object>) this.map;
			@SuppressWarnings("unchecked")
			Comparator<? super E> cc = (Comparator<? super E>) set.comparator();
			Comparator<? super E> mc = map.comparator();
			if (cc == mc || (cc != null && cc.equals(mc))) {
				map.addAllForTreeSet(set, PRESENT);
				return true;
			}
		}
		return super.addAll(coll);
	}

	@Override
	public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
		return new MyTreeSet<>(map.subMap(fromElement, fromInclusive, toElement, toInclusive));
	}

	@Override
	public NavigableSet<E> headSet(E toElement, boolean inclusive) {
		return new MyTreeSet<>(map.headMap(toElement, inclusive));
	}

	@Override
	public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
		return new MyTreeSet<>(map.tailMap(fromElement, inclusive));
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
	public Comparator<? super E> comparator() {
		return map.comparator();
	}

	@Override
	public E first() {
		return map.firstKey();
	}

	@Override
	public E last() {
		return map.lastKey();
	}

	// NavigableSet API methods

	@Override
	public E lower(E e) {
		return map.lowerKey(e);
	}

	@Override
	public E floor(E e) {
		return map.floorKey(e);
	}

	@Override
	public E ceiling(E e) {
		return map.ceilingKey(e);
	}

	@Override
	public E higher(E e) {
		return map.higherKey(e);
	}

	@Override
	public E pollFirst() {
		Map.Entry<E, ?> e = map.pollFirstEntry();
		return (e == null) ? null : e.getKey();
	}

	@Override
	public E pollLast() {
		Map.Entry<E, ?> e = map.pollLastEntry();
		return (e == null) ? null : e.getKey();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		MyTreeSet<E> clone = null;
		try {
			clone = (MyTreeSet<E>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}

		clone.map = new TreeMap<>(map);
		return clone;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Write out any hidden stuff
		oos.defaultWriteObject();

		// Write out Comparator
		oos.writeObject(map.comparator());

		// Write out size
		oos.writeInt(map.size());

		// Write out all elements in the proper order.
		for (E e : map.keySet()) {
			oos.writeObject(e);
		}
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Read in any hidden stuff
		ois.defaultReadObject();

		// Read in Comparator
		@SuppressWarnings("unchecked")
		Comparator<? super E> comparator = (Comparator<? super E>) ois.readObject();

		// Create backing TreeMap
		MyTreeMap<E, Object> treeMap;
		if (comparator == null) {
			treeMap = new MyTreeMap<>();
		} else {
			treeMap = new MyTreeMap<>(comparator);
		}
		map = treeMap;

		// Read in size
		int size = ois.readInt();

		treeMap.readTreeSet(size, ois, PRESENT);
	}

}