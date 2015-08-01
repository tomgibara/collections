package com.tomgibara.collect;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.tomgibara.hashing.Hasher;

public final class TokenMap<V> extends AbstractMap<String, V> {

	private final String[] strings;
	private final Hasher<String> hasher;
	private final Store<V> store;
	
	private Entries entries = null;
	private Keys keys = null;
	private Values values = null;
	
	TokenMap(String[] strings, Hasher<String> hasher, Store<V> store) {
		this.strings = strings;
		this.hasher = hasher;
		this.store = store;
	}
	
	@Override
	public int size() {
		return store.size();
	}
	
	@Override
	public void clear() {
		store.clear();
	}
	
	@Override
	public boolean containsKey(Object key) {
		int i = indexOf(key);
		return i == -1 ? false : store.get(i) != null;
	}
	
	@Override
	public boolean containsValue(Object value) {
		return indexOfValue(value) != -1;
	}
	
	@Override
	public boolean isEmpty() {
		return store.size() == 0;
	}
	
	@Override
	public V get(Object key) {
		int i = indexOf(key);
		return i == -1 ? null : store.get(i);
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		int i = indexOf(key);
		if (i == -1) return defaultValue;
		V value = store.get(i);
		return value == null ? defaultValue : value;
	}
	
	@Override
	public V remove(Object key) {
		int i = indexOf(key);
		if (i == -1) return null;
		V value = store.get(i);
		if (value != null) store.set(i, null);
		return value;
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		if (value == null) return false;
		int i = indexOf(key);
		if (i == -1) return false;
		V previous = store.get(i);
		if (previous == null || !previous.equals(value)) return false;
		store.set(i, null);
		return true;
	}
	
	@Override
	public V put(String key, V value) {
		if (value == null) throw new IllegalArgumentException("null value");
		int i = checkedIndexOf(key);
		V previous = store.get(i);
		store.set(i, value);
		return previous;
	}

	@Override
	public V putIfAbsent(String key, V value) {
		if (value == null) throw new IllegalArgumentException("null value");
		int i = checkedIndexOf(key);
		V previous = store.get(i);
		if (previous != null) return previous;
		store.set(i, value);
		return null;
	}

	@Override
	public V replace(String key, V value) {
		if (value == null) throw new IllegalArgumentException("null value");
		int i = checkedIndexOf(key);
		V previous = store.get(i);
		if (previous == null) return null;
		store.set(i, value);
		return previous;
	}
	
	@Override
	public boolean replace(String key, V oldValue, V newValue) {
		if (newValue == null) throw new IllegalArgumentException("null newValue");
		int i = checkedIndexOf(key);
		V previous = store.get(i);
		if (previous == null || !previous.equals(oldValue)) return false;
		store.set(i, newValue);
		return true;
	}

	@Override
	public Set<Map.Entry<String, V>> entrySet() {
		return entries == null ? entries = new Entries() : entries;
	}
	
	@Override
	public Set<String> keySet() {
		return keys == null ? keys = new Keys() : keys;
	}
	
	@Override
	public Collection<V> values() {
		return values == null ? values = new Values() : values;
	}

	private int indexOf(Object o) {
		if (!(o instanceof String)) return -1;
		String s = (String) o;
		//TODO no way to make this more efficient yet
		int i;
		try {
			i = hasher.intHashValue(s);
		} catch (IllegalArgumentException e) {
			return -1;
		}
		return strings[i].equals(s) ? i : -1;
	}
	
	private int checkedIndexOf(String s) {
		int i = hasher.intHashValue(s);
		if (!strings[i].equals(s)) throw new IllegalArgumentException("invalid token");
		return i;
	}
	
	private int indexOfValue(Object value) {
		if (value == null) return -1;
		for (int i = 0; i < strings.length; i++) {
			V candidate = store.get(i);
			if (candidate != null && candidate.equals(value)) return i;
		}
		return -1;
	}

	private class Keys extends AbstractSet<String> {
		
		@Override
		public int size() {
			return store.size();
		}
		
		@Override
		public boolean isEmpty() {
			return store.size() != 0;
		}
		
		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}
		
		@Override
		public void clear() {
			store.clear();
		}
		
		@Override
		public boolean remove(Object o) {
			int i = indexOf(o);
			if (i == -1) return false;
			V value = store.get(i);
			if (value == null) return false;
			store.set(i, null);
			return true;
		}

		@Override
		public Iterator<String> iterator() {
			return new TokenIterator<String>() {
				@Override
				String get(int index) {
					return strings[index];
				}
			};
		}
	}
	
	final private class Values extends AbstractCollection<V> {

		@Override
		public int size() {
			return store.size();
		}
		
		@Override
		public boolean isEmpty() {
			return store.size() == 0;
		}
		
		@Override
		public void clear() {
			store.clear();
		}
		
		@Override
		public boolean contains(Object o) {
			return containsValue(o);
		}
		
		@Override
		public boolean remove(Object o) {
			int i = indexOfValue(o);
			if (i == -1) return false;
			store.set(i, null);
			return true;
		}

		@Override
		public Iterator<V> iterator() {
			return new TokenIterator<V>() {
				@Override
				V get(int index) {
					return store.get(index);
				}
			};
		}

	}
	
	final private class Entries extends AbstractSet<Entry<String, V>> {
		
		@Override
		public int size() {
			return store.size();
		}
		
		@Override
		public boolean isEmpty() {
			return store.size() == 0;
		}
		
		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Entry)) return false;
			Entry<?,?> e = (Entry<?,?>) o;
			Object k = e.getKey();
			int i = indexOf(k);
			if (i == -1) return false;
			V v = store.get(i);
			if (v == null) return false;
			return v.equals(e.getValue());
		}
		
		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Entry)) return false;
			Entry<?,?> e = (Entry<?,?>) o;
			Object k = e.getKey();
			int i = indexOf(k);
			if (i == -1) return false;
			V v = store.get(i);
			if (v == null) return false;
			if (!v.equals(e.getValue())) return false;
			store.set(i, null);
			return true;
		}

		@Override
		public Iterator<Entry<String, V>> iterator() {
			return new TokenIterator<Entry<String, V>>() {
				@Override
				TokenEntry get(int index) {
					return new TokenEntry(index);
				}
			};
		}
		
		@Override
		public void clear() {
			store.clear();
		}
		
	}
	
	abstract private class TokenIterator<X> implements Iterator<X> {
		
		int previous = -1;
		int next = subsequent(previous + 1);

		@Override
		public X next() {
			if (next == strings.length) throw new NoSuchElementException();
			X x = get(next);
			int tmp = next;
			next = subsequent(previous + 1);
			previous = tmp;
			return x;
		}

		@Override
		public boolean hasNext() {
			return next != strings.length;
		}

		@Override
		public void remove() {
			if (previous == -1) throw new NoSuchElementException();
			V value = store.get(previous);
			if (value == null) throw new IllegalStateException();
			store.set(previous, null);
		}
		
		abstract X get(int index);

		private int subsequent(int i) {
			int length = strings.length;
			for (; i < length; i++) {
				if (store.get(i) != null) return i;
			}
			return length;
		}

	}

	final private class TokenEntry extends AbstractMapEntry<String, V> {
		
		private final int index;
		
		TokenEntry(int index) {
			this.index = index;
		}
		
		@Override
		public String getKey() {
			return strings[index];
		}
		
		@Override
		public V getValue() {
			return store.get(index);
		}
		
		@Override
		public V setValue(V value) {
			if (value == null) throw new IllegalArgumentException("null value");
			V previous = store.get(index);
			store.set(index, value);
			return previous;
		}

	}

}
