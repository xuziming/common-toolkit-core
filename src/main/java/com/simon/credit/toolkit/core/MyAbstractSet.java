package com.simon.credit.toolkit.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class MyAbstractSet<E> extends MyAbstractCollection<E> implements Set<E> {

	protected MyAbstractSet() {}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof Set)) {
			return false;
		}

		@SuppressWarnings("rawtypes")
		Collection c = (Collection) o;
		if (c.size() != size()) {
			return false;
		}

		try {
			return containsAll(c);
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int h = 0;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			if (obj != null) {
				h += obj.hashCode();
			}
		}
		return h;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;

		if (size() > c.size()) {
			for (Iterator<?> i = c.iterator(); i.hasNext();) {
				modified |= remove(i.next());
			}
		} else {
			for (Iterator<?> i = iterator(); i.hasNext();) {
				if (c.contains(i.next())) {
					i.remove();
					modified = true;
				}
			}
		}

		return modified;
	}

}