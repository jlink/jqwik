package net.jqwik.api;

import java.math.*;

import net.jqwik.api.Tuple.*;

import static org.assertj.core.api.Assertions.*;

class TupleTests {

	@Example
	void tupleOfOne() {
		Tuple1<String> tuple1 = Tuple.of("hallo");
		assertThat(tuple1.size()).isEqualTo(1);

		assertThat(tuple1.get1()).isEqualTo("hallo");

		assertThat(tuple1.equals(Tuple.of("hallo"))).isTrue();
		assertThat(tuple1.equals(Tuple.of("hello"))).isFalse();

		assertThat(tuple1.hashCode()).isEqualTo(Tuple.of("hallo").hashCode());

		assertThat(tuple1.toString()).isEqualTo("(hallo)");
	}

	@Example
	void tupleOfTwo() {
		Tuple2<String, Integer> tuple2 = Tuple.of("hallo", 42);
		assertThat(tuple2.size()).isEqualTo(2);

		assertThat(tuple2.get1()).isEqualTo("hallo");
		assertThat(tuple2.get2()).isEqualTo(42);

		assertThat(tuple2.equals(Tuple.of("hallo", 42))).isTrue();
		assertThat(tuple2.equals(Tuple.of("hello", 41))).isFalse();

		assertThat(tuple2.hashCode()).isEqualTo(Tuple.of("hallo", 42).hashCode());

		assertThat(tuple2.toString()).isEqualTo("(hallo,42)");
	}

	@Example
	void tupleOfThree() {
		Tuple3<String, Integer, Boolean> tuple3 = Tuple.of("hallo", 42, true);
		assertThat(tuple3.size()).isEqualTo(3);

		assertThat(tuple3.get1()).isEqualTo("hallo");
		assertThat(tuple3.get2()).isEqualTo(42);
		assertThat(tuple3.get3()).isEqualTo(true);

		assertThat(tuple3.equals(Tuple.of("hallo", 42, true))).isTrue();
		assertThat(tuple3.equals(Tuple.of("hallo", 42, false))).isFalse();

		assertThat(tuple3.hashCode()).isEqualTo(Tuple.of("hallo", 42, true).hashCode());

		assertThat(tuple3.toString()).isEqualTo("(hallo,42,true)");
	}

	@Example
	void tupleOfFour() {
		Tuple4<String, Integer, Boolean, RoundingMode> tuple4 = Tuple.of("hallo", 42, true, RoundingMode.CEILING);
		assertThat(tuple4.size()).isEqualTo(4);

		assertThat(tuple4.get1()).isEqualTo("hallo");
		assertThat(tuple4.get2()).isEqualTo(42);
		assertThat(tuple4.get3()).isEqualTo(true);
		assertThat(tuple4.get4()).isEqualTo(RoundingMode.CEILING);

		assertThat(tuple4.equals(Tuple.of("hallo", 42, true, RoundingMode.CEILING))).isTrue();
		assertThat(tuple4.equals(Tuple.of("hallo", 42, true, RoundingMode.FLOOR))).isFalse();

		assertThat(tuple4.hashCode()).isEqualTo(Tuple.of("hallo", 42, true, RoundingMode.CEILING).hashCode());

		assertThat(tuple4.toString()).isEqualTo("(hallo,42,true,CEILING)");
	}
}
