package com.simon.credit.toolkit.core;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class MyAbstractMap<K, V> implements Map<K, V> {

	protected MyAbstractMap() {}

	@Override
	public int size() {
		return entrySet().size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsValue(Object value) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (value == null) {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getValue() == null) {
					return true;
				}
			}
		} else {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (value.equals(e.getValue())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		Iterator<Map.Entry<K, V>> i = entrySet().iterator();
		if (key == null) {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getKey() == null) {
					return true;
				}
			}
		} else {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (key.equals(e.getKey())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public V get(Object key) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (key == null) {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getKey() == null) {
					return e.getValue();
				}
			}
		} else {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (key.equals(e.getKey())) {
					return e.getValue();
				}
			}
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		Entry<K, V> correctEntry = null;
		if (key == null) {
			while (correctEntry == null && i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getKey() == null) {
					correctEntry = e;
				}
			}
		} else {
			while (correctEntry == null && i.hasNext()) {
				Entry<K, V> e = i.next();
				if (key.equals(e.getKey())) {
					correctEntry = e;
				}
			}
		}

		V oldValue = null;
		if (correctEntry != null) {
			oldValue = correctEntry.getValue();
			i.remove();
		}
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		entrySet().clear();
	}

	transient volatile Set<K> keySet = null;
	transient volatile Collection<V> values = null;

	@Override
	public Set<K> keySet() {
		if (keySet == null) {
			keySet = new AbstractSet<K>() {
				public Iterator<K> iterator() {
					return new Iterator<K>() {
						private Iterator<Entry<K, V>> i = entrySet().iterator();

						public boolean hasNext() {
							return i.hasNext();
						}

						public K next() {
							return i.next().getKey();
						}

						public void remove() {
							i.remove();
						}
					};
				}

				public int size() {
					return MyAbstractMap.this.size();
				}

				public boolean isEmpty() {
					return MyAbstractMap.this.isEmpty();
				}

				public void clear() {
					MyAbstractMap.this.clear();
				}

				public boolean contains(Object k) {
					return MyAbstractMap.this.containsKey(k);
				}
			};
		}
		return keySet;
	}

	@Override
	public Collection<V> values() {
		if (values == null) {
			values = new AbstractCollection<V>() {
				public Iterator<V> iterator() {
					return new Iterator<V>() {
						private Iterator<Entry<K, V>> i = entrySet().iterator();

						public boolean hasNext() {
							return i.hasNext();
						}

						public V next() {
							return i.next().getValue();
						}

						public void remove() {
							i.remove();
						}
					};
				}

				public int size() {
					return MyAbstractMap.this.size();
				}

				public boolean isEmpty() {
					return MyAbstractMap.this.isEmpty();
				}

				public void clear() {
					MyAbstractMap.this.clear();
				}

				public boolean contains(Object v) {
					return MyAbstractMap.this.containsValue(v);
				}
			};
		}
		return values;
	}

	public abstract Set<Entry<K, V>> entrySet();

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof Map)) {
			return false;
		}

		Map<?, ?> m = (Map<?, ?>) o;
		if (m.size() != size()) {
			return false;
		}

		try {
			Iterator<Entry<K, V>> i = entrySet().iterator();
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				K key = e.getKey();
				V value = e.getValue();
				if (value == null) {
					if (!(m.get(key) == null && m.containsKey(key))) {
						return false;
					}
				} else {
					if (!value.equals(m.get(key))) {
						return false;
					}
				}
			}
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int h = 0;
		Iterator<Entry<K, V>> i = entrySet().iterator();
		while (i.hasNext()) {
			h += i.next().hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (!i.hasNext()) {
			return "{}";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (;;) {
			Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == this ? "(this Map)" : key);
			sb.append('=');
			sb.append(value == this ? "(this Map)" : value);
			if (!i.hasNext()) {
				return sb.append('}').toString();
			}
			sb.append(',').append(' ');
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		MyAbstractMap<?, ?> result = (MyAbstractMap<?, ?>) super.clone();
		result.keySet = null;
		result.values = null;
		return result;
	}

	private static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public static class SimpleEntry<K, V> implements Entry<K, V>, java.io.Serializable {
		private static final long serialVersionUID = -8499721149061103585L;

		private final K key;
		private V value;

		public SimpleEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public SimpleEntry(Entry<? extends K, ? extends V> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			return eq(key, e.getKey()) && eq(value, e.getValue());
		}

		@Override
		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}

	public static class SimpleImmutableEntry<K, V> implements Entry<K, V>, java.io.Serializable {
		private static final long serialVersionUID = 7138329143949025153L;

		private final K key;
		private final V value;

		public SimpleImmutableEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public SimpleImmutableEntry(Entry<? extends K, ? extends V> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			return eq(key, e.getKey()) && eq(value, e.getValue());
		}

		@Override
		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}

}
