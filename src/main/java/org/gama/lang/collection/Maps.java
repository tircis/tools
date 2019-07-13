package org.gama.lang.collection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Guillaume Mary
 */
public final class Maps {
	
	/**
	 * Allows chaining of the {@link ChainingMap#add(Object, Object)} method and then easily create a Map
	 * @return a new Map instance
	 */
	public static <K, V> ChainingMap<K, V> asMap(K key, V value) {
		return new ChainingMap<K, V>().add(key, value);
	}
	
	/**
	 * Allows chaining of the {@link ChainingHashMap#add(Object, Object)} method and then easily create a Map
	 * @return a new HashMap instance
	 */
	public static <K, V> ChainingHashMap<K, V> asHashMap(K key, V value) {
		return new ChainingHashMap<K, V>().add(key, value);
	}
	
	/**
	 * Allows chaining of the {@link ChainingMap#add(Object, Object)} method and then easily create a TreeMap
	 * @param comparator the {@link Comparator} for the keys of the {@link TreeMap}
	 * @return a new TreeMap instance
	 */
	public static <K, V> ChainingComparingMap<K, V> asComparingMap(Comparator<K> comparator, K key, V value) {
		return new ChainingComparingMap<K, V>(comparator).add(key, value);
	}
	
	/**
	 * Returns a {@link Map} of given {@link Map}s values that are mapped on the same keys in both {@link Map}s.
	 * 
	 * @param map1 a {@link Map}
	 * @param map2 a {@link Map}
	 * @param <K> {@link Map}s key type
	 * @param <V1> first {@link Map} values type
	 * @param <V2> second {@link Map} values type
	 * @return a {@link Map} of values that are in both {@link Map}s and mapped under the same keys
	 */
	public static <K, V1, V2> Map<V1, V2> innerJoin(Map<K, V1> map1, Map<K, V2> map2) {
		Map<V1, V2> result = new HashMap<>();
		map1.forEach((k, v) -> {
			V2 v2 = map2.get(k);
			if (v2 != null) {
				result.put(v, v2);
			}
		});
		return result;
	}
	
	/**
	 * Simple {@link LinkedHashMap} that allows to chain calls to {@link #add(Object, Object)} (same as put) and so quickly create a Map.
	 * 
	 * @param <K>
	 * @param <V>
	 */
	public static class ChainingMap<K, V> extends LinkedHashMap<K, V> {
		
		public ChainingMap() {
			super();
		}
		
		public ChainingMap<K, V> add(K key, V value) {
			put(key, value);
			return this;
		}
	}
	
	/**
	 * Simple {@link HashMap} that allows to chain calls to {@link #add(Object, Object)} (same as put) and so quickly create a Map.
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static class ChainingHashMap<K, V> extends HashMap<K, V> {
		
		public ChainingHashMap() {
			super();
		}
		
		public ChainingHashMap<K, V> add(K key, V value) {
			put(key, value);
			return this;
		}
	}
	
	
	/**
	 * Simple {@link TreeMap} that allows to chain calls to {@link #add(Object, Object)} (same as put) and so quickly create a Map.
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static class ChainingComparingMap<K, V> extends TreeMap<K, V> {
		
		public ChainingComparingMap(Comparator<K> comparator) {
			super(comparator);
		}
		
		public ChainingComparingMap<K, V> add(K key, V value) {
			put(key, value);
			return this;
		}
	}
}
