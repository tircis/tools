package org.gama.lang;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.gama.lang.bean.FieldIterator;
import org.gama.lang.bean.MethodIterator;
import org.gama.lang.collection.Iterables;
import org.gama.lang.exception.Exceptions;
import org.gama.lang.reflect.MemberPrinter;

import static org.gama.lang.reflect.MemberPrinter.FLATTEN_PACKAGE_PRINTER;
import static org.gama.lang.reflect.MemberPrinter.FULL_PACKAGE_PRINTER;

/**
 * @author Guillaume Mary
 */
public final class Reflections {
	
	/** The system property name to manage how classes are printed when using {@link #toString(Class)} */
	public static final String FLAT_PACKAGES_OPTION_KEY = "reflections.flatPackages";
	
	/** Possible values of {@link #FLAT_PACKAGES_OPTION_KEY} : disable, false, off */
	public static final Set<String> DISABLE_FLAT_PACKAGES_OPTIONS = org.gama.lang.collection.Arrays.asTreeSet(String.CASE_INSENSITIVE_ORDER, "disable, false, off");
	
	/**
	 * Printer for {@link #toString(Class)} and {@link #toString(Method)}.
	 * Depends on {@link #FLAT_PACKAGES_OPTION_KEY} system property
	 */
	private static final MemberPrinter classPrinter = ((Supplier<MemberPrinter>) () -> {
		Optional<String> flattenPackageOption = Optional.ofNullable(System.getProperty(FLAT_PACKAGES_OPTION_KEY));
		return flattenPackageOption.filter(DISABLE_FLAT_PACKAGES_OPTIONS::contains).isPresent()
				? FULL_PACKAGE_PRINTER
				: FLATTEN_PACKAGE_PRINTER;
	}).get();
	
	/**
	 * Shortcut for {@link AccessibleObject#setAccessible(boolean)}
	 * @param accessibleObject the object to be set accessible
	 */
	public static void ensureAccessible(AccessibleObject accessibleObject) {
		accessibleObject.setAccessible(true);
	}
	
	/**
	 * Looks for the "default constructor" (no argument) of a class
	 * @param clazz a class, not null
	 * @param <T> type of the class instances
	 * @return the default constructor of the given class (never null)
	 * @throws UnsupportedOperationException if the class doesn't have a default constructor
	 */
	public static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException("Class " + toString(clazz) + " doesn't have a default constructor");
		}
	}
	
	public static Map<String, Field> mapFieldsOnName(Class clazz) {
		return Iterables.mapIdentity(() -> new FieldIterator(clazz), Field::getName);
	}
	
	/**
	 * Returns the field with the given signature elements. Class hierarchy is checked also until Object class. 
	 *
	 * @param clazz the class of the field
	 * @param name the name of the field
	 * @return the found field, null possible
	 */
	public static Field findField(Class clazz, String name) {
		return Iterables.stream(new FieldIterator(clazz)).filter(field -> field.getName().equals(name)).findAny().orElse(null);
	}
	
	/**
	 * Same as {@link #findField(Class, String)} but throws a {@link org.gama.lang.Reflections.MemberNotFoundException}
	 * if the field is not found.
	 *
	 * @param clazz the class of the method
	 * @param name the name of the method
	 * @return the found method, never null
	 */
	public static Field getField(Class clazz, String name) {
		Field field = findField(clazz, name);
		if (field == null) {
			throw new MemberNotFoundException("Field " + name + " on " + toString(clazz) + " was not found");
		}
		return field;
	}
	
	/**
	 * Returns the method with the given signature elements. Class hierarchy is checked also until Object class. 
	 * 
	 * @param clazz the class of the method
	 * @param name the name of the method
	 * @param argTypes the argument types of the method
	 * @return the found method, null possible
	 */
	public static Method findMethod(Class clazz, String name, Class... argTypes) {
		return Iterables.stream(new MethodIterator(clazz))
				.filter(method -> method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), argTypes))
				.findAny().orElse(null);
	}
	
	/**
	 * Same as {@link #findMethod(Class, String, Class[])} but throws a {@link org.gama.lang.Reflections.MemberNotFoundException}
	 * if the method is not found.
	 * 
	 * @param clazz the class of the method
	 * @param name the name of the method
	 * @param argTypes the argument types of the method
	 * @return the found method, never null
	 */
	public static Method getMethod(Class clazz, String name, Class... argTypes) {
		Method method = findMethod(clazz, name, argTypes);
		if (method == null) {
			throw new MemberNotFoundException("Method " + name + "(" + new StringAppender().ccat(argTypes, ", ").toString()
					+ ") on " + toString(clazz) + " was not found");
		}
		return method;
	}
	
	/**
	 * Instanciates a class from its default contructor
	 * 
	 * @param clazz the target intance class
	 * @param <E> the target instance type
	 * @return a new instance of type E, never null
	 */
	public static <E> E newInstance(Class<E> clazz) {
		try {
			Constructor<E> defaultConstructor = getDefaultConstructor(clazz);
			// safeguard for non-accessible accessor
			defaultConstructor.setAccessible(true);
			return defaultConstructor.newInstance();
		} catch (Throwable t) {
			throw Exceptions.asRuntimeException(t);
		}
	}
	
	public static class MemberNotFoundException extends RuntimeException {
		public MemberNotFoundException(String message) {
			super(message);
		}
	}
	
	/**
	 * Gives the type of eventualy-wrapped property by a method (works even if the field doesn't exists), which means:
	 * - the input if the method is a setter
	 * - the return type if it's a getter
	 * @param method a method matching the Java Bean Convention naming
	 * @return the eventualy-wrapped field type, not null
	 */
	public static Class propertyType(Method method) {
		return onJavaBeanPropertyWrapper(method, Method::getReturnType, m -> m.getParameterTypes()[0], m -> boolean.class);
	}
	
	/**
	 * Calls a {@link Supplier} according to the detected kind of getter or setter a method is. This implementation only tests on method name
	 * (or method return type for boolean getter). So it does not ensure that a real field matches the wrapped method.
	 *
	 * @param fieldWrapper the method to test against getter, setter
	 * @param getterAction the action run in case of given method is a getter
	 * @param setterAction the action run in case of given method is a setter
	 * @param booleanGetterAction the action run in case of given method is a getter of a boolean
	 * @param <E> the returned type
	 * @return the result of the called action
	 */
	public static <E> E onJavaBeanPropertyWrapper(Method fieldWrapper, Function<Method, E> getterAction, Function<Method, E> setterAction, Function<Method, E> booleanGetterAction) {
		int parameterCount = fieldWrapper.getParameterCount();
		Class<?> returnType = fieldWrapper.getReturnType();
		IllegalArgumentException exception = newEncapsulationException(fieldWrapper);
		return onJavaBeanPropertyWrapperName(fieldWrapper, new getOrThrow<>(getterAction, () -> parameterCount == 0 && returnType != Void.class, () -> exception),
				new getOrThrow<>(setterAction, () -> parameterCount == 1 && returnType == void.class, () -> exception),
				new getOrThrow<>(booleanGetterAction, () -> parameterCount == 0 && returnType == boolean.class, () -> exception));
	}
	
	/**
	 * Calls a {@link Supplier} according to the detected kind of getter or setter a method is. This implementation only tests on method name
	 * (or method return type for boolean getter). So it does not ensure that a real field matches the wrapped method.
	 * 
	 * @param fieldWrapper the method to test against getter, setter
	 * @param getterAction the action run in case of given method is a getter
	 * @param setterAction the action run in case of given method is a setter
	 * @param booleanGetterAction the action run in case of given method is a getter of a boolean
	 * @param <E> the returned type
	 * @return the result of the called action
	 */
	public static <E> E onJavaBeanPropertyWrapperName(Method fieldWrapper, Function<Method, E> getterAction, Function<Method, E> setterAction, Function<Method, E> booleanGetterAction) {
		String methodName = fieldWrapper.getName();
		if (methodName.startsWith("get")) {
			return getterAction.apply(fieldWrapper);
		} else if (methodName.startsWith("set")) {
			return setterAction.apply(fieldWrapper);
		} else if (methodName.startsWith("is")) {
			return booleanGetterAction.apply(fieldWrapper);
		} else {
			throw newEncapsulationException(fieldWrapper);
		}
	}
	
	private static IllegalArgumentException newEncapsulationException(Method method) {
		return new IllegalArgumentException("Field wrapper "
				+ toString(method)
				+ " doesn't feet encapsulation naming convention");
	}
	
	/**
	 * Gives the "target" type of some method. Target type is the returned type for getter, and first arg type for setter. boolean if getter startint with "is".
	 * The method must follow the Java Bean Convention : starts by "get", "set", or "is", else it will throw an {@link IllegalArgumentException}
	 * 
	 * @param method not null, expected to be a getter or setter
	 * @return the target type of the getter/setter
	 */
	public static Class javaBeanTargetType(Method method) {
		return Reflections.onJavaBeanPropertyWrapper(method, Method::getReturnType, m -> m.getParameterTypes()[0], m -> boolean.class);
	}
	
	public static String toString(Method method) {
		return classPrinter.toString(method);
	}
	
	public static String toString(Class clazz) {
		return classPrinter.toString(clazz);
	}
	
	/**
	 * Almost the same as {@link Class#forName(String)} but accepts serialized form of type names.
	 * @param typeName a type name, not null
	 * @return the {@link Class} found behind typeName
	 * @throws ClassNotFoundException as thrown by {@link Class#forName(String)}
	 * @see Class#getName()
	 */
	public static Class forName(String typeName) throws ClassNotFoundException {
		switch (typeName) {
			case "Z":
				return boolean.class;
			case "B":
				return byte.class;
			case "C":
				return char.class;
			case "D":
				return double.class;
			case "F":
				return float.class;
			case "I":
				return int.class;
			case "J":
				return long.class;
			case "S":
				return short.class;
			case "V":
				return void.class;
			default:
				if (typeName.startsWith("L")) {
					typeName = typeName.substring(1, typeName.length());
					typeName = typeName.replace("/", ".");
				}
				return Class.forName(typeName);
		}
	}
	
	@FunctionalInterface
	private interface Checker {
		boolean check();
	}
	
	private static class getOrThrow<E> implements Function<Method, E> {
		
		private final Function<Method, E> surrogate;
		private final Checker predicate;
		private final Supplier<RuntimeException> throwableSupplier;
		
		private getOrThrow(Function<Method, E> surrogate, Checker predicate, Supplier<RuntimeException> s) {
			this.surrogate = surrogate;
			this.predicate = predicate;
			this.throwableSupplier = s;
		}
		
		@Override
		public E apply(Method method) {
			if (predicate.check()) {
				return surrogate.apply(method);
			} else {
				throw throwableSupplier.get();
			}
		}
	}
}
