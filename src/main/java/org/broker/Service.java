package org.broker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Service<T> {
	
	private ConcurrentMap<Long, T> map;

	public Service() {
		map = new ConcurrentHashMap<>();
	}
	
	public boolean create(long key, T value) {
		return this.map.putIfAbsent(key, value) == null;
	}
	
	public T get(long key) {
		return this.map.get(key);
	}
	
	public boolean exist(long key) {
		return this.map.containsKey(key);
	}
	
	public List<T> values() {
		return new ArrayList<>(map.values());
	}

}
