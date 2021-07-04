package org.gama.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.gama.lang.trace.ModifiableInt;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Guillaume Mary
 */
class NullableTest {
	
	private static final Supplier<String> NULL_SUPPLIER = () -> null;
	private static final Supplier<String> STRING_SUPPLIER = () -> "hello";
	
	@Test
	void testEmpty() {
		assertThat(Nullable.empty().isPresent()).isEqualTo(false);
		assertThat(Nullable.empty().get()).isEqualTo(null);
	}
	
	@Test
	void testConstructors_object() {
		String nullObject = null;
		assertThat(Nullable.nullable(nullObject).getOr("hello")).isEqualTo("hello");
		Function<String, String> appendingWorldFunction = o -> o + " world";
		Function<String, String> shallNotCallFunction = o -> {
			fail("this code should not even be invoked");
			return o;
		};
		assertThat(Nullable.nullable(nullObject, shallNotCallFunction).getOr("hello")).isEqualTo("hello");
		assertThat(Nullable.nullable(nullObject, shallNotCallFunction).getOr("hello")).isEqualTo("hello");
		assertThat(Nullable.nullable(nullObject, shallNotCallFunction, shallNotCallFunction).getOr("hello")).isEqualTo("hello");
		
		assertThat(Nullable.nullable("hello", appendingWorldFunction).getOr("john")).isEqualTo("hello world");
		assertThat(Nullable.nullable("hello", appendingWorldFunction).getOr("john")).isEqualTo("hello world");
		assertThat(Nullable.nullable("hello", appendingWorldFunction, o -> o + " !").getOr("john")).isEqualTo("hello world !");
	}
	
	@Test
	void testConstructors_supplier() {
		assertThat(Nullable.nullable(NULL_SUPPLIER).getOr("hello")).isEqualTo("hello");
		Function<String, String> appendingWorldFunction = o -> o + " world";
		Function<String, String> shallNotCallFunction = o -> {
			fail("this code should not even be invoked");
			return o;
		};
		assertThat(Nullable.nullable(NULL_SUPPLIER, shallNotCallFunction).getOr("hello")).isEqualTo("hello");
		assertThat(Nullable.nullable(NULL_SUPPLIER, shallNotCallFunction).getOr("hello")).isEqualTo("hello");
		assertThat(Nullable.nullable(NULL_SUPPLIER, shallNotCallFunction, shallNotCallFunction).getOr("hello")).isEqualTo("hello");
		
		assertThat(Nullable.nullable(STRING_SUPPLIER, appendingWorldFunction).getOr("john")).isEqualTo("hello world");
		assertThat(Nullable.nullable(STRING_SUPPLIER, appendingWorldFunction).getOr("john")).isEqualTo("hello world");
		assertThat(Nullable.nullable(STRING_SUPPLIER, appendingWorldFunction, o -> o + " !").getOr("john")).isEqualTo("hello world !");
	}
	
	@Test
	void testIsPresent() {
		assertThat(Nullable.nullable(new Object()).isPresent()).isTrue();
		assertThat(Nullable.nullable((Object) null).isPresent()).isFalse();
	}
	
	@Test
	void testGetOr_object() {
		Object value = new Object();
		assertThat(Nullable.nullable(value).getOr("hello")).isEqualTo(value);
		assertThat(Nullable.nullable((String) null).getOr("hello")).isEqualTo("hello");
	}
	
	@Test
	void testGetOr_supplier() {
		Object value = new Object();
		Supplier<Object> dummyFunction = () -> value;
		assertThat(Nullable.nullable(value).getOr(dummyFunction)).isEqualTo(value);
		assertThat(Nullable.nullable((Object) null).getOr(dummyFunction)).isEqualTo(value);
	}
	
	@Test
	void testElseSet_object() {
		Object value = new Object();
		assertThat(Nullable.nullable(value).elseSet("hello").get()).isEqualTo(value);
		assertThat(Nullable.nullable((String) null).elseSet("hello").get()).isEqualTo("hello");
	}
	
	@Test
	void testElseSet_supplier() {
		Object value = new Object();
		assertThat(Nullable.nullable(value).elseSet(() -> "hello").get()).isEqualTo(value);
		assertThat(Nullable.nullable((String) null).elseSet(() -> "hello").get()).isEqualTo("hello");
	}
	
	
	@Test
	void testMap() {
		// simple case
		assertThat(Nullable.nullable("Hello").map(o -> o + " World").get()).isEqualTo("Hello World");
		// with null value
		assertThat(Nullable.nullable((Object) null).map(o -> {
			fail("this code should not even be invoked");
			return o + " World";
		}).get()).isNull();
	}
	
	@Test
	void testTest() {
		// simple case
		assertThat(Nullable.nullable("Hello").test(o -> o.equals("Hello")).get()).isTrue();
		// with null value
		assertThat(Nullable.nullable((Object) null).test(o -> {
			fail("this code should not even be invoked");
			return false;
		}).isPresent()).isFalse();
	}
	
	@Test
	void testFilter() {
		// simple case
		assertThat(Nullable.nullable("Hello").filter(o -> o.contains("ll")).get()).isEqualTo("Hello");
		// with null value
		assertThat(Nullable.nullable((Object) null).filter(o -> {
			fail("this code should not even be invoked");
			return false;
		}).get()).isNull();
	}
	
	@Test
	void testInvoke() {
		String value = "hello";
		ModifiableInt isCalled = new ModifiableInt();
		Consumer<String> dummyFunction = s -> isCalled.increment();
		Nullable.nullable(value).invoke(dummyFunction);
		assertThat(isCalled.getValue()).isEqualTo(1);
		Nullable.nullable(value).invoke(dummyFunction);
		assertThat(isCalled.getValue()).isEqualTo(2);
		Nullable.nullable((String) null).invoke(dummyFunction);
		assertThat(isCalled.getValue()).isEqualTo(2);
	}
	
	@Test
	void testMapThrower() {
		assertThatExceptionOfType(IOException.class).isThrownBy(() -> Nullable.nullable(new ByteArrayOutputStream()).mapThrower(b -> {
			b.write(0);
			throw new IOException();
		}));
	}
	
	
	@Test
	void testInvokeThrower() {
		assertThatExceptionOfType(IOException.class).isThrownBy(() -> Nullable.nullable(new ByteArrayOutputStream()).invokeThrower(b -> {
			b.write(0);
			throw new IOException();
		}));
	}
	
	@Test
	void testElseThrow() throws IOException {
		Object value = new Object();
		assertThat(Nullable.nullable(value).elseThrow(new IOException()).get()).isEqualTo(value);
	}
	
	@Test
	void testElseThrow_nullValue_exceptionIsThrown() {
		assertThatExceptionOfType(IOException.class).isThrownBy(() -> Nullable.nullable((Object) null).elseThrow(new IOException()));
	}
	
	@Test
	void testElseThrow_supplier() throws IOException {
		Object value = new Object();
		assertThat(Nullable.nullable(value).<IOException>elseThrow(IOException::new).get()).isEqualTo(value);
	}
	
	@Test
	void testGetElseThrow() throws IOException {
		Object value = new Object();
		assertThat(Nullable.nullable(value).getOrThrow(new IOException())).isEqualTo(value);
	}
	
	@Test
	void testGetElseThrow_nullValue_exceptionIsThrown() {
		assertThatExceptionOfType(IOException.class).isThrownBy(() -> Nullable.nullable((Object) null).getOrThrow(new IOException()));
	}
	
}