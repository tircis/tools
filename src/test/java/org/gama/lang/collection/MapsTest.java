package org.gama.lang.collection;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Mary
 */
class MapsTest {
	
	@Test
	void innerJoin() {
		Map<Integer, Integer> result = Maps.innerJoin(
				Maps.forHashMap(String.class, Integer.class)
				.add("a", 1).add("b", 2).add("c", 3),
				Maps.forHashMap(String.class, Integer.class)
				.add("b", 4).add("c", 5).add("d", 6));
		assertThat(result).isEqualTo(Maps.forHashMap(Integer.class, Integer.class).add(2, 4).add(3, 5));
	}
	
	@Test
	void innerJoinOnValuesAndKeys() {
		Map<String, String> result = Maps.innerJoinOnValuesAndKeys(
				Maps.forHashMap(String.class, Integer.class)
						.add("a", 1).add("b", 2).add("c", 3),
				Maps.forHashMap(Integer.class, String.class)
						.add(1, "A").add(2, "B").add(3, "C"));
		assertThat(result).isEqualTo(Maps.forHashMap(String.class, String.class).add("a", "A").add("b", "B").add("c", "C"));
	}
	
	@Test
	void putAll() {
		Map<String, Integer> result = Maps.putAll(
				Maps.forHashMap(String.class, Integer.class).add("a", 1).add("b", 2).add("c", 3),
				Maps.forHashMap(String.class, Integer.class).add("b", 4).add("c", 5).add("d", 6));
		assertThat(result).isEqualTo(Maps.forHashMap(String.class, Integer.class).add("a", 1).add("b", 4).add("c", 5).add("d", 6));
	}
}