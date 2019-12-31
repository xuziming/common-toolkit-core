package com.simon.credit.toolkit.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class MyHashSet<E> extends MyAbstractSet<E> implements Set<E>, Cloneable, Serializable {
	static final long serialVersionUID = -5024744406713321676L;

	private transient MyHashMap<E, Object> map;

	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();

	public MyHashSet() {
		map = new MyHashMap<E, Object>();
	}

	public MyHashSet(Collection<? extends E> c) {
		map = new MyHashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
		addAll(c);
	}

	public MyHashSet(int initialCapacity, float loadFactor) {
		map = new MyHashMap<>(initialCapacity, loadFactor);
	}

	public MyHashSet(int initialCapacity) {
		map = new MyHashMap<>(initialCapacity);
	}

	MyHashSet(int initialCapacity, float loadFactor, boolean dummy) {
		map = new MyLinkedHashMap<>(initialCapacity, loadFactor);
	}

	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	public boolean add(E e) {
		return map.put(e, PRESENT) == null;
	}

	public boolean remove(Object o) {
		return map.remove(o) == PRESENT;
	}

	public void clear() {
		map.clear();
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		try {
			MyHashSet<E> newSet = (MyHashSet<E>) super.clone();
			newSet.map = (MyHashMap<E, Object>) map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Write out any hidden serialization magic
		oos.defaultWriteObject();

		// Write out HashMap capacity and load factor
		oos.writeInt(map.capacity());
		oos.writeFloat(map.loadFactor());

		// Write out size
		oos.writeInt(map.size());

		// Write out all elements in the proper order.
		for (E e : map.keySet())
			oos.writeObject(e);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Read in any hidden serialization magic
		ois.defaultReadObject();

		// Read in HashMap capacity and load factor and create backing HashMap
		int capacity = ois.readInt();
		float loadFactor = ois.readFloat();
		map = (((MyHashSet) this) instanceof MyLinkedHashSet ? 
			new MyLinkedHashMap<E, Object>(capacity, loadFactor) : new MyHashMap<E, Object>(capacity, loadFactor));

		// Read in size
		int size = ois.readInt();

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++) {
			E e = (E) ois.readObject();
			map.put(e, PRESENT);
		}
	}

}