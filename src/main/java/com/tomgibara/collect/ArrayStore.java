package com.tomgibara.collect;

import java.lang.reflect.Array;
import java.util.Arrays;

class ArrayStore<V> implements Store<V> {

	final V[] values;
	int size;
	
	ArrayStore(Class<V> type, int size) {
		values = (V[]) Array.newInstance(type, size);
		size = 0;
	}
	
	ArrayStore(V[] values) {
		this.values = values;
		size = Stores.countNonNulls(values);
	}

	ArrayStore(V[] values, int size) {
		this.values = values;
		this.size = size;
	}
	
	@Override
	public Class<? extends V> valueType() {
		return (Class<? extends V>) values.getClass().getComponentType();
	}

	@Override
	public int size() {
		return size;
	}
	
	@Override
	public V get(int index) {
		return values[index];
	}

	@Override
	public V set(int index, V value) {
		V old = values[index];
		values[index] = value;
		if (old != null) size --;
		if (value != null) size ++;
		return old;
	}

	@Override
	public void clear() {
		Arrays.fill(values, null);
		size = 0;
	}

	@Override
	public boolean isMutable() {
		return true;
	}
	
	@Override
	public Store<V> immutable() {
		return new ImmutableArrayStore<V>(this);
	}
	
	@Override
	public Store<V> mutableCopy() {
		return new ArrayStore<V>(values.clone(), size);
	}

}
