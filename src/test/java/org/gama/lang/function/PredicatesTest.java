package org.gama.lang.function;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.gama.lang.collection.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Guillaume Mary
 */
class PredicatesTest {
	
	@Test
	void not() {
		Predicate<Object> testInstance = Predicates.not(Objects::isNull);
		assertTrue(testInstance.test(""));
		assertFalse(testInstance.test(null));
	}
	
	@Test
	void predicate() {
		Set<String> data = Arrays.asHashSet("a", "b", "c");
		Predicate<Object> testInstance = Predicates.predicate(data::contains);
		assertTrue(testInstance.test("a"));
		assertFalse(testInstance.test("d"));
	}
	
	@Test
	void predicate_withMapper() {
		Set<String> data = Arrays.asHashSet("1", "2", "3");
		Predicate<Integer> testInstance = Predicates.predicate(Object::toString, data::contains);
		assertTrue(testInstance.test(1));
		assertFalse(testInstance.test(4));
	}
	
	@Test
	void acceptAll() {
		assertTrue(Predicates.acceptAll().test(""));
		assertTrue(Predicates.acceptAll().test(null));
		assertTrue(Predicates.acceptAll().test("a"));
		assertTrue(Predicates.acceptAll().test(42));
	}
	
	@Test
	void rejectAll() {
		assertFalse(Predicates.rejectAll().test(""));
		assertFalse(Predicates.rejectAll().test(null));
		assertFalse(Predicates.rejectAll().test("a"));
		assertFalse(Predicates.rejectAll().test(42));
	}
	
	@Test
	void equalsWithNull() {
		assertTrue(Predicates.equalOrNull(null, null));
		assertTrue(Predicates.equalOrNull("a", "a"));
		assertFalse(Predicates.equalOrNull("a", "b"));
		assertFalse(Predicates.equalOrNull("b", "a"));
		assertFalse(Predicates.equalOrNull(null, "b"));
		assertFalse(Predicates.equalOrNull("a", null));
	}
	
	@Test
	void equalsWithNull_predicate() {
		BiPredicate predicateMock = Mockito.mock(BiPredicate.class);
		when(predicateMock.test(any(), any())).thenReturn(true);
		
		assertTrue(Predicates.equalOrNull(null, null, predicateMock));
		Mockito.verifyZeroInteractions(predicateMock);

		Predicates.equalOrNull("a", "a", predicateMock);
		Mockito.verify(predicateMock, Mockito.times(1)).test(eq("a"), eq("a"));

		Predicates.equalOrNull("a", "b", predicateMock);
		Mockito.verify(predicateMock, Mockito.times(1)).test(eq("a"), eq("b"));

		Predicates.equalOrNull("b", "a", predicateMock);
		Mockito.verify(predicateMock, Mockito.times(1)).test(eq("a"), eq("b"));
		
		Mockito.clearInvocations(predicateMock);
		
		Predicates.equalOrNull(null, "b", predicateMock);
		Mockito.verifyZeroInteractions(predicateMock);
		
		Predicates.equalOrNull("a", null, predicateMock);
		Mockito.verifyZeroInteractions(predicateMock);
	}
}