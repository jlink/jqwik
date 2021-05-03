package net.jqwik.time.api.times.localTime.timeMethods;

import java.time.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.time.api.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.testing.TestingSupport.*;

public class HourTests {

	@Provide
	Arbitrary<Integer> hours() {
		return Arbitraries.integers().between(0, 23);
	}

	@Property
	void hourBetween(@ForAll("hours") int startHour, @ForAll("hours") int endHour, @ForAll Random random) {

		Assume.that(startHour <= endHour);

		Arbitrary<LocalTime> times = Times.times().hourBetween(startHour, endHour);

		assertAllGenerated(times.generator(1000), random, time -> {
			assertThat(time.getHour()).isGreaterThanOrEqualTo(startHour);
			assertThat(time.getHour()).isLessThanOrEqualTo(endHour);
			return true;
		});

	}

	@Property
	void hourBetweenSame(@ForAll("hours") int hour, @ForAll Random random) {

		Arbitrary<LocalTime> times = Times.times().hourBetween(hour, hour);

		assertAllGenerated(times.generator(1000), random, time -> {
			assertThat(time.getHour()).isEqualTo(hour);
			return true;
		});

	}

}
