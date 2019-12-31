package com.simon.credit.toolkit.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class MyArrayList<E> extends MyAbstractList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    private static final long serialVersionUID = 8683452581122892189L;

    private transient Object[] elementData;

    private int size;

    public MyArrayList(int initialCapacity) {
        super();
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: "+ initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
    }

    public MyArrayList() {
        this(10);// ArrayList默认初始容量为10
    }

    public MyArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        size = elementData.length;
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class) {
            elementData = Arrays.copyOf(elementData, size, Object[].class);
        }
    }

    public void trimToSize() {
        modCount++;
        int oldCapacity = elementData.length;
        if (size < oldCapacity) {
            elementData = Arrays.copyOf(elementData, size);
        }
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity > 0) {
            ensureCapacityInternal(minCapacity);
        }
    }

	/**
	 * 确保内部数组容量(注意：ArrayList没有负载因子loadFactor)
	 * @param minCapacity
	 */
	private void ensureCapacityInternal(int minCapacity) {
		modCount++;

		// 当minCapacity(值为size+1)大于数组长度时，需要进行扩容
		if (minCapacity - elementData.length > 0) {
			grow(minCapacity);
		}
	}

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
		int newCapacity = oldCapacity + (oldCapacity >> 1);// oldCapacity + oldCapacity/2 = oldCapacity * 1.5
        if (newCapacity - minCapacity < 0) {// 如果newCapacity小于minCapacity
            newCapacity = minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE > 0) {// 如果newCapacity大于MAX_ARRAY_SIZE
            newCapacity = hugeCapacity(minCapacity);// 返回最大整数：Integer.MAX_VALUE
        }

        // 扩容：将之前的老数组元素通通拷贝到新数组
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

	/**
	 * 超大容量
	 * @param minCapacity
	 * @return
	 */
	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) { // overflow
			throw new OutOfMemoryError();
		}
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size; i++) {
				if (elementData[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				if (o.equals(elementData[i])) {
					return i;
				}
			}
		}

		return -1;
	}

	public int lastIndexOf(Object obj) {
		if (obj == null) {
			for (int i = size - 1; i >= 0; i--) {
				if (elementData[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = size - 1; i >= 0; i--) {
				if (obj.equals(elementData[i])) {
					return i;
				}
			}
		}

		return -1;
	}

	public Object clone() {
		try {
			@SuppressWarnings("unchecked")
			MyArrayList<E> v = (MyArrayList<E>) super.clone();
			v.elementData = Arrays.copyOf(elementData, size);
			v.modCount = 0;
			return v;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}

    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (a.length < size) {
			// Make a new array of a's runtime type, but my contents:
			return (T[]) Arrays.copyOf(elementData, size, a.getClass());
		}
		System.arraycopy(elementData, 0, a, 0, size);
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

    // Positional Access Operations

	@SuppressWarnings("unchecked")
	E elementData(int index) {
		return (E) elementData[index];
	}

    public E get(int index) {
        rangeCheck(index);
        return elementData(index);
    }

    public E set(int index, E element) {
        rangeCheck(index);
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    public boolean add(E e) {
        ensureCapacityInternal(size + 1);
        elementData[size++] = e;
        return true;
    }

	public void add(int index, E element) {
		rangeCheckForAdd(index);
		ensureCapacityInternal(size + 1);
		// index位置之后的元素通通往后移动一位
		System.arraycopy(elementData, index, elementData, index + 1, size - index);
		elementData[index] = element;
		size++;
	}

	public E remove(int index) {
		rangeCheck(index);

		modCount++;
		E oldValue = elementData(index);

		// 计算总共需要移动的元素个数(等于maxIndex减去index)
		int numMoved = (size - 1) - index;
		if (numMoved > 0) {
			// 数组元素往前移动numMoved位
			System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		}
		elementData[--size] = null; // Let gc do its work

		return oldValue;
	}

    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++) {
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
            }
        } else {
            for (int index = 0; index < size; index++) {
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
            }
        }

        return false;
    }

	private void fastRemove(int index) {
		modCount++;
		int numMoved = (size - 1) - index;
		if (numMoved > 0) {// 即：index < (size-1)，表示index在合理范围
			System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		}
		elementData[--size] = null; // Let gc do its work
	}

    public void clear() {
        modCount++;

        // Let gc do its work
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }

        size = 0;
    }

	public boolean addAll(Collection<? extends E> coll) {
		Object[] array = coll.toArray();
		int numNew = array.length;
		ensureCapacityInternal(size + numNew);// 确保容量
		// 将待插入的数组元素从size位置开始拷贝numNew个到新数组(如已扩容)
		System.arraycopy(array, 0, elementData, size, numNew);
		size += numNew;// 数组元素数量增加numNew
		return numNew != 0;
	}

	public boolean addAll(int index, Collection<? extends E> coll) {
		rangeCheckForAdd(index);

		Object[] array = coll.toArray();
		int numNew = array.length;
		ensureCapacityInternal(size + numNew);// 确保容量

		int numMoved = size - index;
		if (numMoved > 0) {// step1：如果index小余size，表示在中间插入多个元素，则后面的元素需要往后移
			System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
		}

		// 将待插入的数组元素放入到step1执行完成后空置出来的位置
		System.arraycopy(array, 0, elementData, index, numNew);

		size += numNew;// 数组元素增加numNew
		return numNew != 0;
	}

	protected void removeRange(int fromIndex, int toIndex) {
		modCount++;
		int numMoved = size - toIndex;// 计算toIndex之后的剩余的元素个数

		// 把toIndex位置之后的numMoved个元素往前移动至fromIndex位置
		System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

		// Let gc do its work
		int newSize = size - (toIndex - fromIndex);
		while (size != newSize) {
			elementData[--size] = null;
		}
	}

    private void rangeCheck(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

	private void rangeCheckForAdd(int index) {
		if (index > size || index < 0) {// index的值必须位于0到size之间(即：0<=index<size)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
		}
	}

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    public boolean removeAll(Collection<?> c) {
        return batchRemove(c, false);
    }

    public boolean retainAll(Collection<?> c) {
        return batchRemove(c, true);
    }

	private boolean batchRemove(Collection<?> c, boolean complement) {
		final Object[] elementData = this.elementData;
		int r = 0, w = 0;
		boolean modified = false;
		try {
			for (; r < size; r++) {
				if (c.contains(elementData[r]) == complement) {
					elementData[w++] = elementData[r];
				}
			}
		} finally {
			// Preserve behavioral compatibility with AbstractCollection,
			// even if c.contains() throws.
			if (r != size) {
				System.arraycopy(elementData, r, elementData, w, size - r);
				w += size - r;
			}
			if (w != size) {
				for (int i = w; i < size; i++) {
					elementData[i] = null;
				}
				modCount += size - w;
				size = w;
				modified = true;
			}
		}
		return modified;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Write out element count, and any hidden stuff
		int expectedModCount = modCount;
		oos.defaultWriteObject();

		// Write out array length
		oos.writeInt(elementData.length);

		// Write out all elements in the proper order.
		for (int i = 0; i < size; i++)
			oos.writeObject(elementData[i]);

		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}

	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Read in size, and any hidden stuff
		ois.defaultReadObject();

		// Read in array length and allocate array
		int arrayLength = ois.readInt();
		Object[] a = elementData = new Object[arrayLength];

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++) {
			a[i] = ois.readObject();
		}
	}

    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
        return new ListItr(index);
    }

    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

	private class Itr implements Iterator<E> {
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		int expectedModCount = modCount;

		public boolean hasNext() {
			return cursor != size;
		}

		@SuppressWarnings("unchecked")
		public E next() {
			checkForComodification();
			int i = cursor;
			if (i >= size) {
				throw new NoSuchElementException();
			}
			Object[] elementData = MyArrayList.this.elementData;
			if (i >= elementData.length) {
				throw new ConcurrentModificationException();
			}
			cursor = i + 1;
			return (E) elementData[lastRet = i];
		}

		public void remove() {
			if (lastRet < 0) {
				throw new IllegalStateException();
			}
			checkForComodification();

			try {
				MyArrayList.this.remove(lastRet);
				cursor = lastRet;
				lastRet = -1;
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		final void checkForComodification() {
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
		}
	}

    /** An optimized version of AbstractList.ListItr */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0) {
                throw new NoSuchElementException();
            }
            Object[] elementData = MyArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();

            try {
                MyArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                MyArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > size) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
    }

    private class SubList extends MyAbstractList<E> implements RandomAccess {
        private final MyAbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(MyAbstractList<E> parent, int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = MyArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = MyArrayList.this.elementData(offset + index);
            MyArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return MyArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex, parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0) return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = MyArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size) {
                        throw new NoSuchElementException();
                    }
                    Object[] elementData = MyArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0) {
                        throw new NoSuchElementException();
                    }
                    Object[] elementData = MyArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0) {
                        throw new IllegalStateException();
                    }
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = MyArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0) {
                        throw new IllegalStateException();
                    }
                    checkForComodification();

                    try {
                        MyArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = MyArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != MyArrayList.this.modCount) {
                        throw new ConcurrentModificationException();
                    }
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            }
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            }
        }

        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        private void checkForComodification() {
            if (MyArrayList.this.modCount != this.modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

}