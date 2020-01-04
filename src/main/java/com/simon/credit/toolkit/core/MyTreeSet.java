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

	private transient NavigableMap<E, Object> m;

	private static final Object PRESENT = new Object();

	MyTreeSet(NavigableMap<E, Object> m) {
		this.m = m;
	}

	public MyTreeSet() {
		this(new TreeMap<E, Object>());
	}

	public MyTreeSet(Comparator<? super E> comparator) {
		this(new TreeMap<>(comparator));
	}

	public MyTreeSet(Collection<? extends E> c) {
		this();
		addAll(c);
	}

	public MyTreeSet(SortedSet<E> s) {
		this(s.comparator());
		addAll(s);
	}

	public Iterator<E> iterator() {
		return m.navigableKeySet().iterator();
	}

	public Iterator<E> descendingIterator() {
		return m.descendingKeySet().iterator();
	}

	public NavigableSet<E> descendingSet() {
		return new MyTreeSet<>(m.descendingMap());
	}

	public int size() {
		return m.size();
	}

	public boolean isEmpty() {
		return m.isEmpty();
	}

	public boolean contains(Object o) {
		return m.containsKey(o);
	}

	public boolean add(E e) {
		return m.put(e, PRESENT) == null;
	}

	public boolean remove(Object o) {
		return m.remove(o) == PRESENT;
	}

	public void clear() {
		m.clear();
	}

	public boolean addAll(Collection<? extends E> coll) {
		// Use linear-time version if applicable
		if (m.size() == 0 && coll.size() > 0 && coll instanceof SortedSet && m instanceof TreeMap) {
			SortedSet<? extends E> set = (SortedSet<? extends E>) coll;
			MyTreeMap<E, Object> map = (MyTreeMap<E, Object>) m;
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

	public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
		return new MyTreeSet<>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
	}

	public NavigableSet<E> headSet(E toElement, boolean inclusive) {
		return new MyTreeSet<>(m.headMap(toElement, inclusive));
	}

	public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
		return new MyTreeSet<>(m.tailMap(fromElement, inclusive));
	}

	public SortedSet<E> subSet(E fromElement, E toElement) {
		return subSet(fromElement, true, toElement, false);
	}

	public SortedSet<E> headSet(E toElement) {
		return headSet(toElement, false);
	}

	public SortedSet<E> tailSet(E fromElement) {
		return tailSet(fromElement, true);
	}

	public Comparator<? super E> comparator() {
		return m.comparator();
	}

	public E first() {
		return m.firstKey();
	}

	public E last() {
		return m.lastKey();
	}

	// NavigableSet API methods

	public E lower(E e) {
		return m.lowerKey(e);
	}

	public E floor(E e) {
		return m.floorKey(e);
	}

	public E ceiling(E e) {
		return m.ceilingKey(e);
	}

	public E higher(E e) {
		return m.higherKey(e);
	}

	public E pollFirst() {
		Map.Entry<E, ?> e = m.pollFirstEntry();
		return (e == null) ? null : e.getKey();
	}

	public E pollLast() {
		Map.Entry<E, ?> e = m.pollLastEntry();
		return (e == null) ? null : e.getKey();
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		MyTreeSet<E> clone = null;
		try {
			clone = (MyTreeSet<E>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}

		clone.m = new TreeMap<>(m);
		return clone;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Write out any hidden stuff
		oos.defaultWriteObject();

		// Write out Comparator
		oos.writeObject(m.comparator());

		// Write out size
		oos.writeInt(m.size());

		// Write out all elements in the proper order.
		for (E e : m.keySet()) {
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
		m = treeMap;

		// Read in size
		int size = ois.readInt();

		treeMap.readTreeSet(size, ois, PRESENT);
	}

}