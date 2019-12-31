package com.simon.credit.toolkit.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 * 一个基于优先级堆的无界优先级队列
 * <pre>
 * 优先级队列的元素按照其自然顺序进行排序，或者根据构造队列时提供的Comparator进行排序，
 * 具体取决于所使用的构造方法。该队列不允许使用 null 元素也不允许插入不可比较的对象(没有实现Comparable接口的对象)。
 * 
 * PriorityQueue队列的头指排序规则最小那个元素。如果多个元素都是最小值则随机选一个。
 * PriorityQueue是一个无界队列，但是初始的容量(实际是一个Object[])，随着不断向优先级队列添加元素，其容量会自动扩容，无需指定容量增加策略的细节。
 * PriorityQueue使用跟普通队列一样，唯一区别是PriorityQueue会根据排序规则决定谁在队头，谁在队尾。
 * PriorityQueue并不是线程安全队列，因为offer/poll都没有对队列进行锁定，故若要拥有线程安全的优先级队列，需要额外进行加锁操作。
 * 
 * 【总结】
 * 1> PriorityQueue是一种无界的，线程不安全的队列。
 * 2> PriorityQueue是一种通过数组实现的，并拥有优先级的队列。
 * 3> PriorityQueue存储的元素要求必须是可比较的对象， 如果不是就必须明确指定比较器。
 * </pre>
 */
public class MyPriorityQueue<E> extends MyAbstractQueue<E> implements Serializable {
	private static final long serialVersionUID = -7720805057305804111L;

	private static final int DEFAULT_INITIAL_CAPACITY = 11;

	/** 队列容器， 默认是11 */
	private transient Object[] queue;

	/** 队列长度 */
	private int size = 0;

	/** 队列比较器，(默认)为null使用自然排序 */
	private final Comparator<? super E> comparator;

	private transient int modCount = 0;

	public MyPriorityQueue() {
		this(DEFAULT_INITIAL_CAPACITY, null);
	}

	public MyPriorityQueue(int initialCapacity) {
		this(initialCapacity, null);
	}

	public MyPriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
		// Note: This restriction of at least one is not actually needed, but continues for 1.5 compatibility
		if (initialCapacity < 1) {
			throw new IllegalArgumentException();
		}
		this.queue = new Object[initialCapacity];
		this.comparator = comparator;
	}

	@SuppressWarnings("unchecked")
	public MyPriorityQueue(Collection<? extends E> c) {
		if (c instanceof SortedSet<?>) {
			SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
			this.comparator = (Comparator<? super E>) ss.comparator();
			initElementsFromCollection(ss);
		} else if (c instanceof MyPriorityQueue<?>) {
			MyPriorityQueue<? extends E> pq = (MyPriorityQueue<? extends E>) c;
			this.comparator = (Comparator<? super E>) pq.comparator();
			initFromPriorityQueue(pq);
		} else {
			this.comparator = null;
			initFromCollection(c);
		}
	}

	@SuppressWarnings("unchecked")
	public MyPriorityQueue(MyPriorityQueue<? extends E> c) {
		this.comparator = (Comparator<? super E>) c.comparator();
		initFromPriorityQueue(c);
	}

	@SuppressWarnings("unchecked")
	public MyPriorityQueue(SortedSet<? extends E> c) {
		this.comparator = (Comparator<? super E>) c.comparator();
		initElementsFromCollection(c);
	}

	private void initFromPriorityQueue(MyPriorityQueue<? extends E> c) {
		if (c.getClass() == MyPriorityQueue.class) {
			this.queue = c.toArray();
			this.size = c.size();
		} else {
			initFromCollection(c);
		}
	}

	private void initElementsFromCollection(Collection<? extends E> c) {
		Object[] a = c.toArray();
		// If c.toArray incorrectly doesn't return Object[], copy it.
		if (a.getClass() != Object[].class) {
			a = Arrays.copyOf(a, a.length, Object[].class);
		}
		int len = a.length;
		if (len == 1 || this.comparator != null) {
			for (int i = 0; i < len; i++) {
				if (a[i] == null) {
					throw new NullPointerException();
				}
			}
		}
		this.queue = a;
		this.size = a.length;
	}

	private void initFromCollection(Collection<? extends E> c) {
		initElementsFromCollection(c);
		heapify();
	}

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	private void grow(int minCapacity) {
		int oldCapacity = queue.length;
		// Double size if small; else grow by 50%
		int newCapacity = oldCapacity + ((oldCapacity < 64) ? (oldCapacity + 2) : (oldCapacity >> 1));
		// overflow-conscious code
		if (newCapacity - MAX_ARRAY_SIZE > 0) {
			newCapacity = hugeCapacity(minCapacity);
		}
		queue = Arrays.copyOf(queue, newCapacity);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) {// overflow
			throw new OutOfMemoryError();
		}
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	public boolean add(E e) {
		return offer(e);
	}

	/**
	 * 入列
	 */
	public boolean offer(E e) {
		if (e == null) {
			throw new NullPointerException();
		}
		modCount++;
		int i = size;
		if (i >= queue.length) {
			grow(i + 1);// 当队列长度大于等于容量值时，自动扩容
		}
		size = i + 1;
		if (i == 0) {
			queue[0] = e;
		} else {
			siftUp(i, e);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public E peek() {
		if (size == 0) {
			return null;
		}
		return (E) queue[0];
	}

	private int indexOf(Object obj) {
		if (obj != null) {
			for (int i = 0; i < size; i++) {
				if (obj.equals(queue[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	public boolean remove(Object obj) {
		int i = indexOf(obj);
		if (i == -1) {
			return false;
		} else {
			removeAt(i);
			return true;
		}
	}

	boolean removeEq(Object obj) {
		for (int i = 0; i < size; i++) {
			if (obj == queue[i]) {
				removeAt(i);
				return true;
			}
		}
		return false;
	}

	public boolean contains(Object obj) {
		return indexOf(obj) != -1;
	}

	public Object[] toArray() {
		return Arrays.copyOf(queue, size);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] array) {
		if (array.length < size) {
			// Make a new array of a's runtime type, but my contents:
			return (T[]) Arrays.copyOf(queue, size, array.getClass());
		}
		System.arraycopy(queue, 0, array, 0, size);
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	public Iterator<E> iterator() {
		return new Itr();
	}

	private final class Itr implements Iterator<E> {
		private int cursor = 0;

		private int lastRet = -1;

		private ArrayDeque<E> forgetMeNot = null;

		private E lastRetElt = null;

		private int expectedModCount = modCount;

		public boolean hasNext() {
			return cursor < size || (forgetMeNot != null && !forgetMeNot.isEmpty());
		}

		@SuppressWarnings("unchecked")
		public E next() {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			if (cursor < size) {
				return (E) queue[lastRet = cursor++];
			}
			if (forgetMeNot != null) {
				lastRet = -1;
				lastRetElt = forgetMeNot.poll();
				if (lastRetElt != null) {
					return lastRetElt;
				}
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			if (lastRet != -1) {
				E moved = MyPriorityQueue.this.removeAt(lastRet);
				lastRet = -1;
				if (moved == null) {
					cursor--;
				} else {
					if (forgetMeNot == null) {
						forgetMeNot = new ArrayDeque<>();
					}
					forgetMeNot.add(moved);
				}
			} else if (lastRetElt != null) {
				MyPriorityQueue.this.removeEq(lastRetElt);
				lastRetElt = null;
			} else {
				throw new IllegalStateException();
			}
			expectedModCount = modCount;
		}
	}

	public int size() {
		return size;
	}

	public void clear() {
		modCount++;
		for (int i = 0; i < size; i++) {
			queue[i] = null;
		}
		size = 0;
	}

	public E poll() {
		if (size == 0) {
			return null;
		}
		int s = --size;
		modCount++;
		@SuppressWarnings("unchecked")
		E result = (E) queue[0];
		@SuppressWarnings("unchecked")
		E x = (E) queue[s];
		queue[s] = null;
		if (s != 0) {
			siftDown(0, x);
		}
		return result;
	}

	private E removeAt(int i) {
		assert i >= 0 && i < size;
		modCount++;
		int s = --size;
		if (s == i) {// removed last element
			queue[i] = null;
		} else {
			@SuppressWarnings("unchecked")
			E moved = (E) queue[s];
			queue[s] = null;
			siftDown(i, moved);
			if (queue[i] == moved) {
				siftUp(i, moved);
				if (queue[i] != moved) {
					return moved;
				}
			}
		}
		return null;
	}

	private void siftUp(int k, E x) {
		if (comparator != null) {
			siftUpUsingComparator(k, x);// 指定比较器
		} else {
			siftUpComparable(k, x);// 没有指定比较器，使用默认的自然比较器
		}
	}

	/**
	 * 将插入的小数往前上升
	 * @param k
	 * @param x
	 */
	@SuppressWarnings("unchecked")
	private void siftUpComparable(int k, E x) {
		Comparable<? super E> key = (Comparable<? super E>) x;
		while (k > 0) {
			int parent = (k - 1) >>> 1;
			Object e = queue[parent];
			if (key.compareTo((E) e) >= 0) {
				break;
			}
			queue[k] = e;
			k = parent;
		}
		queue[k] = key;
	}

	@SuppressWarnings("unchecked")
	private void siftUpUsingComparator(int k, E x) {
		while (k > 0) {
			int parent = (k - 1) >>> 1;
			Object e = queue[parent];
			if (comparator.compare(x, (E) e) >= 0) {
				break;
			}
			queue[k] = e;
			k = parent;
		}
		queue[k] = x;
	}

	private void siftDown(int k, E x) {
		if (comparator != null) {
			siftDownUsingComparator(k, x);// 指定比较器
		} else {
			siftDownComparable(k, x);// 没有指定比较器，使用默认的自然比较器
		}
	}

	@SuppressWarnings("unchecked")
	private void siftDownComparable(int k, E x) {
		Comparable<? super E> key = (Comparable<? super E>) x;
		int half = size >>> 1; // loop while a non-leaf
		while (k < half) {
			int child = (k << 1) + 1; // assume left child is least
			Object c = queue[child];
			int right = child + 1;
			if (right < size && ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0) {
				c = queue[child = right];
			}
			if (key.compareTo((E) c) <= 0) {
				break;
			}
			queue[k] = c;
			k = child;
		}
		queue[k] = key;
	}

	@SuppressWarnings("unchecked")
	private void siftDownUsingComparator(int k, E x) {
		int half = size >>> 1;
		while (k < half) {
			int child = (k << 1) + 1;
			Object c = queue[child];
			int right = child + 1;
			if (right < size && comparator.compare((E) c, (E) queue[right]) > 0) {
				c = queue[child = right];
			}
			if (comparator.compare(x, (E) c) <= 0) {
				break;
			}
			queue[k] = c;
			k = child;
		}
		queue[k] = x;
	}

	@SuppressWarnings("unchecked")
	private void heapify() {
		for (int i = (size >>> 1) - 1; i >= 0; i--) {
			siftDown(i, (E) queue[i]);
		}
	}

	public Comparator<? super E> comparator() {
		return comparator;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Write out element count, and any hidden stuff
		oos.defaultWriteObject();

		// Write out array length, for compatibility with 1.5 version
		oos.writeInt(Math.max(2, size + 1));

		// Write out all elements in the "proper order".
		for (int i = 0; i < size; i++)
			oos.writeObject(queue[i]);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Read in size, and any hidden stuff
		ois.defaultReadObject();

		// Read in (and discard) array length
		ois.readInt();

		queue = new Object[size];

		// Read in all elements.
		for (int i = 0; i < size; i++) {
			queue[i] = ois.readObject();
		}

		// Elements are guaranteed to be in "proper order", but the
		// spec has never explained what that might be.
		heapify();
	}

}