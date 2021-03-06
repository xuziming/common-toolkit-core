package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("restriction")
public class MyExchanger<V> {

	private static final int ASHIFT = 7;

	private static final int MMASK = 0xff;

	private static final int SEQ = MMASK + 1;

	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	static final int FULL = (NCPU >= (MMASK << 1)) ? MMASK : NCPU >>> 1;

	private static final int SPINS = 1 << 10;

	private static final Object NULL_ITEM = new Object();

	private static final Object TIMED_OUT = new Object();

	static final class Node {
		int index; // Arena index
		int bound; // Last recorded value of Exchanger.bound
		int collides; // Number of CAS failures at current bound
		int hash; // Pseudo-random for spins
		Object item; // This thread's current item
		volatile Object match; // Item provided by releasing thread
		volatile Thread parked; // Set to this thread when parked, else null
	}

	static final class Participant extends ThreadLocal<Node> {
		public Node initialValue() {
			return new Node();
		}
	}

	private final Participant participant;

	private volatile Node[] arena;

	private volatile Node slot;

	private volatile int bound;

	private final Object arenaExchange(Object item, boolean timed, long ns) {
		Node[] a = arena;
		Node p = participant.get();
		for (int i = p.index;;) { // access slot at i
			int b, m, c;
			long j; // j is raw array offset
			Node q = (Node) unsafe.getObjectVolatile(a, j = (i << ASHIFT) + ABASE);
			if (q != null && unsafe.compareAndSwapObject(a, j, q, null)) {
				Object v = q.item; // release
				q.match = item;
				Thread w = q.parked;
				if (w != null) {
					unsafe.unpark(w);
				}
				return v;
			} else if (i <= (m = (b = bound) & MMASK) && q == null) {
				p.item = item; // offer
				if (unsafe.compareAndSwapObject(a, j, null, p)) {
					long end = (timed && m == 0) ? System.nanoTime() + ns : 0L;
					Thread t = Thread.currentThread(); // wait
					for (int h = p.hash, spins = SPINS;;) {
						Object v = p.match;
						if (v != null) {
							unsafe.putOrderedObject(p, MATCH, null);
							p.item = null; // clear for next use
							p.hash = h;
							return v;
						} else if (spins > 0) {
							h ^= h << 1;
							h ^= h >>> 3;
							h ^= h << 10; // xorshift
							if (h == 0) {// initialize hash
								h = SPINS | (int) t.getId();
							} else if (h < 0 && /* approx 50% true */(--spins & ((SPINS >>> 1) - 1)) == 0) {
								Thread.yield(); // two yields per wait
							}
						} else if (unsafe.getObjectVolatile(a, j) != p) {
							spins = SPINS; // releaser hasn't set match yet
						} else if (!t.isInterrupted() && m == 0 && (!timed || (ns = end - System.nanoTime()) > 0L)) {
							unsafe.putObject(t, BLOCKER, this); // emulate LockSupport
							p.parked = t; // minimize window
							if (unsafe.getObjectVolatile(a, j) == p) {
								unsafe.park(false, ns);
							}
							p.parked = null;
							unsafe.putObject(t, BLOCKER, null);
						} else if (unsafe.getObjectVolatile(a, j) == p && unsafe.compareAndSwapObject(a, j, p, null)) {
							if (m != 0) {// try to shrink
								unsafe.compareAndSwapInt(this, BOUND, b, b + SEQ - 1);
							}
							p.item = null;
							p.hash = h;
							i = p.index >>>= 1; // descend
							if (Thread.interrupted()) {
								return null;
							}
							if (timed && m == 0 && ns <= 0L) {
								return TIMED_OUT;
							}
							break; // expired; restart
						}
					}
				} else {
					p.item = null; // clear offer
				}
			} else {
				if (p.bound != b) { // stale; reset
					p.bound = b;
					p.collides = 0;
					i = (i != m || m == 0) ? m : m - 1;
				} else if ((c = p.collides) < m || m == FULL || !unsafe.compareAndSwapInt(this, BOUND, b, b + SEQ + 1)) {
					p.collides = c + 1;
					i = (i == 0) ? m : i - 1; // cyclically traverse
				} else {
					i = m + 1; // grow
				}
				p.index = i;
			}
		}
	}

	private final Object slotExchange(Object item, boolean timed, long ns) {
		Node p = participant.get();
		Thread t = Thread.currentThread();
		if (t.isInterrupted()) {// preserve interrupt status so caller can recheck
			return null;
		}

		for (Node q;;) {
			if ((q = slot) != null) {
				if (unsafe.compareAndSwapObject(this, SLOT, q, null)) {
					Object v = q.item;
					q.match = item;
					Thread w = q.parked;
					if (w != null) {
						unsafe.unpark(w);
					}
					return v;
				}
				// create arena on contention, but continue until slot null
				if (NCPU > 1 && bound == 0 && unsafe.compareAndSwapInt(this, BOUND, 0, SEQ)) {
					arena = new Node[(FULL + 2) << ASHIFT];
				}
			} else if (arena != null) {
				return null; // caller must reroute to arenaExchange
			} else {
				p.item = item;
				if (unsafe.compareAndSwapObject(this, SLOT, null, p)) {
					break;
				}
				p.item = null;
			}
		}

		// await release
		int h = p.hash;
		long end = timed ? System.nanoTime() + ns : 0L;
		int spins = (NCPU > 1) ? SPINS : 1;
		Object v;
		while ((v = p.match) == null) {
			if (spins > 0) {
				h ^= h << 1;
				h ^= h >>> 3;
				h ^= h << 10;
				if (h == 0) {
					h = SPINS | (int) t.getId();
				} else if (h < 0 && (--spins & ((SPINS >>> 1) - 1)) == 0) {
					Thread.yield();
				}
			} else if (slot != p) {
				spins = SPINS;
			} else if (!t.isInterrupted() && arena == null && (!timed || (ns = end - System.nanoTime()) > 0L)) {
				unsafe.putObject(t, BLOCKER, this);
				p.parked = t;
				if (slot == p) {
					unsafe.park(false, ns);
				}
				p.parked = null;
				unsafe.putObject(t, BLOCKER, null);
			} else if (unsafe.compareAndSwapObject(this, SLOT, p, null)) {
				v = timed && ns <= 0L && !t.isInterrupted() ? TIMED_OUT : null;
				break;
			}
		}
		unsafe.putOrderedObject(p, MATCH, null);
		p.item = null;
		p.hash = h;
		return v;
	}

	public MyExchanger() {
		participant = new Participant();
	}

	@SuppressWarnings("unchecked")
	public V exchange(V x) throws InterruptedException {
		Object v;
		Object item = (x == null) ? NULL_ITEM : x; // translate null args
		if ((arena != null || (v = slotExchange(item, false, 0L)) == null) 
			&& ((Thread.interrupted() || (v = arenaExchange(item, false, 0L)) == null))) {
			throw new InterruptedException();
		}
		return (v == NULL_ITEM) ? null : (V) v;
	}

	@SuppressWarnings("unchecked")
	public V exchange(V x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Object v;
		Object item = (x == null) ? NULL_ITEM : x;
		long ns = unit.toNanos(timeout);
		if ((arena != null || (v = slotExchange(item, true, ns)) == null)
			&& ((Thread.interrupted() || (v = arenaExchange(item, true, ns)) == null))) {
			throw new InterruptedException();
		}
		if (v == TIMED_OUT) {
			throw new TimeoutException();
		}
		return (v == NULL_ITEM) ? null : (V) v;
	}

	// Unsafe mechanics
	private static final sun.misc.Unsafe unsafe;
	private static final long BOUND;
	private static final long SLOT;
	private static final long MATCH;
	private static final long BLOCKER;
	private static final int ABASE;

	static {
		int s;
		try {
			unsafe = UnsafeToolkits.getUnsafe();
			Class<?> ek = MyExchanger.class;
			Class<?> nk = Node.class;
			Class<?> ak = Node[].class;
			Class<?> tk = Thread.class;

			BOUND 	= unsafe.objectFieldOffset(ek.getDeclaredField("bound"));
			SLOT 	= unsafe.objectFieldOffset(ek.getDeclaredField("slot"));
			MATCH 	= unsafe.objectFieldOffset(nk.getDeclaredField("match"));
			BLOCKER = unsafe.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
			s = unsafe.arrayIndexScale(ak);
			// ABASE absorbs padding in front of element 0
			ABASE = unsafe.arrayBaseOffset(ak) + (1 << ASHIFT);
		} catch (Exception e) {
			throw new Error(e);
		}
		if ((s & (s - 1)) != 0 || s > (1 << ASHIFT)) {
			throw new Error("Unsupported array scale");
		}
	}

}