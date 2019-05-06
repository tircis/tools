package org.gama.lang.function;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Guillaume Mary
 */
public class Predicates {
	
	/**
	 * Static method to negate the given predicate so one can write {@code not(String::contains)}.
	 *
	 * @param predicate any {@link Predicate}, a method reference is prefered else this method as no purpose and can be replaced by {@link Predicate#negate}
	 * @param <E> input type of tested elements
	 * @return a negated {@link Predicate} of the given one
	 */
	public static <E> Predicate<E> not(Predicate<E> predicate) {
		return predicate.negate();
	}
	
	/**
	 * Converts a {@link Function} returning a boolean to {@link Predicates}
	 *
	 * @param booleanFunction the one to be converted
	 * @param <E> input type
	 * @return a new (lambda) {@link Predicate} plugged onto the given {@link Function}
	 */
	public static <E> Predicate<E> predicate(Function<E, Boolean> booleanFunction) {
		return booleanFunction::apply;
	}
	
	/**
	 * Creates a {@link Predicate} from a mapping function and a predicate applied to the result of the function
	 *
	 * @param mapper the {@link Function} that gives the value to be tested
	 * @param predicate the {@link Predicate} to apply onto the result of the mapping function 
	 * @param <I> input type
	 * @param <O> output type
	 * @return a new (lambda) {@link Predicate} plugged onto the given {@link Function} and {@link Predicate}
	 */
	public static <I, O> Predicate<I> predicate(Function<I, O> mapper, Predicate<O> predicate) {
		return predicate(mapper.andThen(Functions.toFunction(predicate)));
	}
	
	/**
	 * @return a {@link Predicate} that always matches
	 */
	public static <C> Predicate<C> acceptAll() {
		return new AlwaysTrue<>();
	}
	
	/**
	 * @return a {@link Predicate} that never matches
	 */
	public static <C> Predicate<C> rejectAll() {
		return new AlwaysFalse<>();
	}
	
	/**
	 * A {@link Predicate} that always returns true
	 * @param <C>
	 */
	private static class AlwaysTrue<C> implements Predicate<C> {
		@Override
		public boolean test(C o) {
			return true;
		}
	}
	
	/**
	 * A {@link Predicate} that always returns false
	 * @param <C>
	 */
	private static class AlwaysFalse<C> implements Predicate<C> {
		@Override
		public boolean test(C o) {
			return false;
		}
	}
}
