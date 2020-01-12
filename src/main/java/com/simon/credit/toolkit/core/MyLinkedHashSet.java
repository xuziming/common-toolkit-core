package com.simon.credit.toolkit.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class MyLinkedHashSet<E> extends MyHashSet<E> implements Set<E>, Cloneable, Serializable {
	private static final long serialVersionUID = -2851667679971038690L;

	public MyLinkedHashSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor, true);
	}

	public MyLinkedHashSet(int initialCapacity) {
		super(initialCapacity, 0.75f, true);
	}

	public MyLinkedHashSet() {
		super(16, 0.75f, true);
	}

	public MyLinkedHashSet(Collection<? extends E> coll) {
		super(Math.max(2 * coll.size(), 11), 0.75f, true);
		addAll(coll);
	}

}