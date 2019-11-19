package com.simon.credit.toolkit.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class MySynchronousQueue<E> extends MyAbstractQueue<E> implements BlockingQueue<E>, Serializable {
	private static final long serialVersionUID = -3223113410248163686L;

	abstract static class Transferer {
		abstract Object transfer(Object e, boolean timed, long nanos);
	}

	/** The number of CPUs, for spin control */
	static final int NCPUS = Runtime.getRuntime().availableProcessors();

	static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32;

	static final int maxUntimedSpins = maxTimedSpins * 16;

	static final long spinForTimeoutThreshold = 1000L;

	@SuppressWarnings({ "restriction", "rawtypes" })
	static final class TransferStack extends Transferer {
		static final int REQUEST    = 0;
		static final int DATA       = 1;
		static final int FULFILLING = 2;

		static boolean isFulfilling(int m) {
			return (m & FULFILLING) != 0;
		}

		static final class SNode {
			volatile SNode next; // next node in stack
			volatile SNode match; // the node matched to this
			volatile Thread waiter; // to control park/unpark
			Object item; // data; or null for REQUESTs
			int mode;

			SNode(Object item) {
				this.item = item;
			}

			boolean casNext(SNode cmp, SNode val) {
				return cmp == next && UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
			}

			boolean tryMatch(SNode s) {
				if (match == null && UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) {
					Thread w = waiter;
					if (w != null) { // waiters need at most one unpark
						waiter = null;
						LockSupport.unpark(w);
					}
					return true;
				}
				return match == s;
			}

			void tryCancel() {
				UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
			}

			boolean isCancelled() {
				return match == this;
			}

			// Unsafe mechanics
			private static final sun.misc.Unsafe UNSAFE;
			private static final long matchOffset;
			private static final long nextOffset;

			static {
				try {
					UNSAFE = sun.misc.Unsafe.getUnsafe();
					Class k = SNode.class;
					matchOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("match"));
					nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		}

		volatile SNode head;

		boolean casHead(SNode h, SNode nh) {
			return h == head && UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
		}

		static SNode snode(SNode s, Object e, SNode next, int mode) {
			if (s == null) {
				s = new SNode(e);
			}
			s.mode = mode;
			s.next = next;
			return s;
		}

		Object transfer(Object e, boolean timed, long nanos) {
			SNode s = null; // constructed/reused as needed
			int mode = (e == null) ? REQUEST : DATA;

			for (;;) {
				SNode h = head;
				if (h == null || h.mode == mode) { // empty or same-mode
					if (timed && nanos <= 0) { // can't wait
						if (h != null && h.isCancelled()) {
							casHead(h, h.next); // pop cancelled node
						} else {
							return null;
						}
					} else if (casHead(h, s = snode(s, e, h, mode))) {
						SNode m = awaitFulfill(s, timed, nanos);
						if (m == s) { // wait was cancelled
							clean(s);
							return null;
						}
						if ((h = head) != null && h.next == s) {
							casHead(h, s.next); // help s's fulfiller
						}
						return (mode == REQUEST) ? m.item : s.item;
					}
				} else if (!isFulfilling(h.mode)) { // try to fulfill
					if (h.isCancelled()) {// already cancelled
						casHead(h, h.next); // pop and retry
					}
					else if (casHead(h, s = snode(s, e, h, FULFILLING | mode))) {
						for (;;) { // loop until matched or waiters disappear
							SNode m = s.next; // m is s's match
							if (m == null) { // all waiters are gone
								casHead(s, null); // pop fulfill node
								s = null; // use new node next time
								break; // restart main loop
							}
							SNode mn = m.next;
							if (m.tryMatch(s)) {
								casHead(s, mn); // pop both s and m
								return (mode == REQUEST) ? m.item : s.item;
							} else {// lost match
								s.casNext(m, mn); // help unlink
							}
						}
					}
				} else { // help a fulfiller
					SNode m = h.next; // m is h's match
					if (m == null) {// waiter is gone
						casHead(h, null); // pop fulfilling node
					} else {
						SNode mn = m.next;
						if (m.tryMatch(h)) {// help match
							casHead(h, mn); // pop both h and m
						} else {// lost match
							h.casNext(m, mn); // help unlink
						}
					}
				}
			}
		}

		SNode awaitFulfill(SNode s, boolean timed, long nanos) {
			long lastTime = timed ? System.nanoTime() : 0;
			Thread w = Thread.currentThread();
			@SuppressWarnings("unused")
			SNode h = head;
			int spins = (shouldSpin(s) ? (timed ? maxTimedSpins : maxUntimedSpins) : 0);
			for (;;) {
				if (w.isInterrupted()) {
					s.tryCancel();
				}
				SNode m = s.match;
				if (m != null) {
					return m;
				}
				if (timed) {
					long now = System.nanoTime();
					nanos -= now - lastTime;
					lastTime = now;
					if (nanos <= 0) {
						s.tryCancel();
						continue;
					}
				}
				if (spins > 0) {
					spins = shouldSpin(s) ? (spins - 1) : 0;
				} else if (s.waiter == null) {
					s.waiter = w; // establish waiter so can park next iter
				} else if (!timed) {
					LockSupport.park(this);
				} else if (nanos > spinForTimeoutThreshold) {
					LockSupport.parkNanos(this, nanos);
				}
			}
		}

		boolean shouldSpin(SNode s) {
			SNode h = head;
			return (h == s || h == null || isFulfilling(h.mode));
		}

		void clean(SNode s) {
			s.item = null; // forget item
			s.waiter = null; // forget thread

			SNode past = s.next;
			if (past != null && past.isCancelled()) {
				past = past.next;
			}

			// Absorb cancelled nodes at head
			SNode p;
			while ((p = head) != null && p != past && p.isCancelled()) {
				casHead(p, p.next);
			}

			// Unsplice embedded nodes
			while (p != null && p != past) {
				SNode n = p.next;
				if (n != null && n.isCancelled()) {
					p.casNext(n, n.next);
				} else {
					p = n;
				}
			}
		}

		// Unsafe mechanics
		private static final sun.misc.Unsafe UNSAFE;
		private static final long headOffset;
		static {
			try {
				UNSAFE = sun.misc.Unsafe.getUnsafe();
				Class k = TransferStack.class;
				headOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("head"));
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	/** Dual Queue */
	@SuppressWarnings({ "restriction", "rawtypes" })
	static final class TransferQueue extends Transferer {
		/** Node class for TransferQueue. */
		static final class QNode {
			volatile QNode next; // next node in queue
			volatile Object item; // CAS'ed to or from null
			volatile Thread waiter; // to control park/unpark
			final boolean isData;

			QNode(Object item, boolean isData) {
				this.item = item;
				this.isData = isData;
			}

			boolean casNext(QNode cmp, QNode val) {
				return next == cmp && UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
			}

			boolean casItem(Object cmp, Object val) {
				return item == cmp && UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
			}

			void tryCancel(Object cmp) {
				UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
			}

			boolean isCancelled() {
				return item == this;
			}

			boolean isOffList() {
				return next == this;
			}

			// Unsafe mechanics
			private static final sun.misc.Unsafe UNSAFE;
			private static final long itemOffset;
			private static final long nextOffset;

			static {
				try {
					UNSAFE = sun.misc.Unsafe.getUnsafe();
					Class k = QNode.class;
					itemOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("item"));
					nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		}

		/** Head of queue */
		transient volatile QNode head;
		/** Tail of queue */
		transient volatile QNode tail;

		transient volatile QNode cleanMe;

		TransferQueue() {
			QNode h = new QNode(null, false); // initialize to dummy node.
			head = h;
			tail = h;
		}

		void advanceHead(QNode h, QNode nh) {
			if (h == head && UNSAFE.compareAndSwapObject(this, headOffset, h, nh)) {
				h.next = h; // forget old next
			}
		}

		void advanceTail(QNode t, QNode nt) {
			if (tail == t) {
				UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
			}
		}

		boolean casCleanMe(QNode cmp, QNode val) {
			return cleanMe == cmp && UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
		}

		Object transfer(Object e, boolean timed, long nanos) {
			QNode s = null; // constructed/reused as needed
			boolean isData = (e != null);

			for (;;) {
				QNode t = tail;
				QNode h = head;
				if (t == null || h == null) {// saw uninitialized value
					continue; // spin
				}

				if (h == t || t.isData == isData) { // empty or same-mode
					QNode tn = t.next;
					if (t != tail) { // inconsistent read
						continue;
					}
					if (tn != null) { // lagging tail
						advanceTail(t, tn);
						continue;
					}
					if (timed && nanos <= 0) { // can't wait
						return null;
					}
					if (s == null) {
						s = new QNode(e, isData);
					}
					if (!t.casNext(null, s)) { // failed to link in
						continue;
					}

					advanceTail(t, s); // swing tail and wait
					Object x = awaitFulfill(s, e, timed, nanos);
					if (x == s) { // wait was cancelled
						clean(t, s);
						return null;
					}

					if (!s.isOffList()) { // not already unlinked
						advanceHead(t, s); // unlink if head
						if (x != null) {// and forget fields
							s.item = s;
						}
						s.waiter = null;
					}
					return (x != null) ? x : e;

				} else { // complementary-mode
					QNode m = h.next; // node to fulfill
					if (t != tail || m == null || h != head) {
						continue; // inconsistent read
					}

					Object x = m.item;
					if (isData == (x != null) || /*m already fulfilled*/x == m || /*m cancelled*/!m.casItem(x, e)) {// lost CAS
						advanceHead(h, m); // dequeue and retry
						continue;
					}

					advanceHead(h, m); // successfully fulfilled
					LockSupport.unpark(m.waiter);
					return (x != null) ? x : e;
				}
			}
		}

		Object awaitFulfill(QNode s, Object e, boolean timed, long nanos) {
			/* Same idea as TransferStack.awaitFulfill */
			long lastTime = timed ? System.nanoTime() : 0;
			Thread w = Thread.currentThread();
			int spins = ((head.next == s) ? (timed ? maxTimedSpins : maxUntimedSpins) : 0);
			for (;;) {
				if (w.isInterrupted()) {
					s.tryCancel(e);
				}
				Object x = s.item;
				if (x != e) {
					return x;
				}
				if (timed) {
					long now = System.nanoTime();
					nanos -= now - lastTime;
					lastTime = now;
					if (nanos <= 0) {
						s.tryCancel(e);
						continue;
					}
				}

				if (spins > 0) {
					--spins;
				} else if (s.waiter == null) {
					s.waiter = w;
				} else if (!timed) {
					LockSupport.park(this);
				} else if (nanos > spinForTimeoutThreshold) {
					LockSupport.parkNanos(this, nanos);
				}
			}
		}

		void clean(QNode pred, QNode s) {
			s.waiter = null; // forget thread

			while (pred.next == s) { // Return early if already unlinked
				QNode h = head;
				QNode hn = h.next; // Absorb cancelled first node as head
				if (hn != null && hn.isCancelled()) {
					advanceHead(h, hn);
					continue;
				}
				QNode t = tail; // Ensure consistent read for tail
				if (t == h) {
					return;
				}
				QNode tn = t.next;
				if (t != tail) {
					continue;
				}
				if (tn != null) {
					advanceTail(t, tn);
					continue;
				}
				if (s != t) { // If not tail, try to unsplice
					QNode sn = s.next;
					if (sn == s || pred.casNext(s, sn)) {
						return;
					}
				}
				QNode dp = cleanMe;
				if (dp != null) { // Try unlinking previous cancelled node
					QNode d = dp.next;
					QNode dn;
					if (d == null || /* d is gone or */ d == dp || /* d is off list or */!d.isCancelled() ||
					/* d not cancelled or */(d != t && /* d not tail and */(dn = d.next) != null &&
					/* has successor */dn != d && /* that is on list */dp.casNext(d, dn))) {// d unspliced
						casCleanMe(dp, null);
					}
					if (dp == pred) {
						return; // s is already saved node
					}
				} else if (casCleanMe(null, pred)) {
					return; // Postpone cleaning s
				}
			}
		}

		private static final sun.misc.Unsafe UNSAFE;
		private static final long headOffset;
		private static final long tailOffset;
		private static final long cleanMeOffset;
		static {
			try {
				UNSAFE = sun.misc.Unsafe.getUnsafe();
				Class k = TransferQueue.class;
				headOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("head"));
				tailOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("tail"));
				cleanMeOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("cleanMe"));
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	private transient volatile Transferer transferer;

	public MySynchronousQueue() {
		this(false);
	}

	public MySynchronousQueue(boolean fair) {
		transferer = fair ? new TransferQueue() : new TransferStack();
	}

	public void put(E o) throws InterruptedException {
		if (o == null) {
			throw new NullPointerException();
		}
		if (transferer.transfer(o, false, 0) == null) {
			Thread.interrupted();
			throw new InterruptedException();
		}
	}

	public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
		if (o == null) {
			throw new NullPointerException();
		}
		if (transferer.transfer(o, true, unit.toNanos(timeout)) != null) {
			return true;
		}
		if (!Thread.interrupted()) {
			return false;
		}
		throw new InterruptedException();
	}

	public boolean offer(E e) {
		if (e == null) {
			throw new NullPointerException();
		}
		return transferer.transfer(e, true, 0) != null;
	}

	@SuppressWarnings("unchecked")
	public E take() throws InterruptedException {
		Object e = transferer.transfer(null, false, 0);
		if (e != null) {
			return (E) e;
		}
		Thread.interrupted();
		throw new InterruptedException();
	}

	@SuppressWarnings("unchecked")
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		Object e = transferer.transfer(null, true, unit.toNanos(timeout));
		if (e != null || !Thread.interrupted()) {
			return (E) e;
		}
		throw new InterruptedException();
	}

	@SuppressWarnings("unchecked")
	public E poll() {
		return (E) transferer.transfer(null, true, 0);
	}

	public boolean isEmpty() {
		return true;
	}

	public int size() {
		return 0;
	}

	public int remainingCapacity() {
		return 0;
	}

	public void clear() {
	}

	public boolean contains(Object o) {
		return false;
	}

	public boolean remove(Object o) {
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		return c.isEmpty();
	}

	public boolean removeAll(Collection<?> c) {
		return false;
	}

	public boolean retainAll(Collection<?> c) {
		return false;
	}

	public E peek() {
		return null;
	}

	public Iterator<E> iterator() {
		return Collections.emptyIterator();
	}

	public Object[] toArray() {
		return new Object[0];
	}

	public <T> T[] toArray(T[] a) {
		if (a.length > 0) {
			a[0] = null;
		}
		return a;
	}

	public int drainTo(Collection<? super E> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		if (c == this) {
			throw new IllegalArgumentException();
		}
		int n = 0;
		E e;
		while ((e = poll()) != null) {
			c.add(e);
			++n;
		}
		return n;
	}

	public int drainTo(Collection<? super E> c, int maxElements) {
		if (c == null) {
			throw new NullPointerException();
		}
		if (c == this) {
			throw new IllegalArgumentException();
		}
		int n = 0;
		E e;
		while (n < maxElements && (e = poll()) != null) {
			c.add(e);
			++n;
		}
		return n;
	}

	static class WaitQueue implements java.io.Serializable {
		private static final long serialVersionUID = -6627013309402660810L;
	}

	static class LifoWaitQueue extends WaitQueue {
		private static final long serialVersionUID = -3633113410248163686L;
	}

	static class FifoWaitQueue extends WaitQueue {
		private static final long serialVersionUID = -3623113410248163686L;
	}

	@SuppressWarnings("unused")
	private ReentrantLock qlock;
	private WaitQueue waitingProducers;
	@SuppressWarnings("unused")
	private WaitQueue waitingConsumers;

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		boolean fair = transferer instanceof TransferQueue;
		if (fair) {
			qlock = new ReentrantLock(true);
			waitingProducers = new FifoWaitQueue();
			waitingConsumers = new FifoWaitQueue();
		} else {
			qlock = new ReentrantLock();
			waitingProducers = new LifoWaitQueue();
			waitingConsumers = new LifoWaitQueue();
		}
		s.defaultWriteObject();
	}

	private void readObject(final java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		if (waitingProducers instanceof FifoWaitQueue) {
			transferer = new TransferQueue();
		} else {
			transferer = new TransferStack();
		}
	}

	// Unsafe mechanics
	@SuppressWarnings("restriction")
	static long objectFieldOffset(sun.misc.Unsafe UNSAFE, String field, Class<?> klazz) {
		try {
			return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
		} catch (NoSuchFieldException e) {
			// Convert Exception to corresponding Error
			NoSuchFieldError error = new NoSuchFieldError(field);
			error.initCause(e);
			throw error;
		}
	}

}
