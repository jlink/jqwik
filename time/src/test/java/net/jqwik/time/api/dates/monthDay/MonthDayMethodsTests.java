package net.jqwik.time.api.dates.monthDay;

import java.time.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.time.api.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.testing.TestingSupport.*;

@Group
public class MonthDayMethodsTests {

	@Provide
	Arbitrary<MonthDay> monthDays() {
		return Dates.monthDays();
	}

	@Group
	class MonthDayMethods {

		@Property
		void atTheEarliest(@ForAll("monthDays") MonthDay monthDay, @ForAll Random random) {

			Arbitrary<MonthDay> dates = Dates.monthDays().atTheEarliest(monthDay);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md).isGreaterThanOrEqualTo(monthDay);
				return true;
			});

		}

		@Property
		void atTheLatest(@ForAll("monthDays") MonthDay monthDay, @ForAll Random random) {

			Arbitrary<MonthDay> dates = Dates.monthDays().atTheLatest(monthDay);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md).isLessThanOrEqualTo(monthDay);
				return true;
			});

		}

		@Property
		void between(@ForAll("monthDays") MonthDay startMonthDay, @ForAll("monthDays") MonthDay endMonthDay, @ForAll Random random) {

			Assume.that(!startMonthDay.isAfter(endMonthDay));

			Arbitrary<MonthDay> dates = Dates.monthDays().between(startMonthDay, endMonthDay);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md).isGreaterThanOrEqualTo(startMonthDay);
				assertThat(md).isLessThanOrEqualTo(endMonthDay);
				return true;
			});
		}

		@Property
		void betweenSame(@ForAll("monthDays") MonthDay monthDay, @ForAll Random random) {

			Arbitrary<MonthDay> dates = Dates.monthDays().between(monthDay, monthDay);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md).isEqualTo(monthDay);
				return true;
			});

		}

	}

	@Group
	class MonthMethods {

		@Property
		void monthBetween(@ForAll("months") int startMonth, @ForAll("months") int endMonth, @ForAll Random random) {

			Assume.that(startMonth <= endMonth);

			Arbitrary<MonthDay> dates = Dates.monthDays().monthBetween(startMonth, endMonth);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md.getMonth()).isGreaterThanOrEqualTo(Month.of(startMonth));
				assertThat(md.getMonth()).isLessThanOrEqualTo(Month.of(endMonth));
				return true;
			});

		}

		@Property
		void monthBetweenSame(@ForAll("months") int month, @ForAll Random random) {

			Arbitrary<MonthDay> dates = Dates.monthDays().monthBetween(month, month);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md.getMonth()).isEqualTo(Month.of(month));
				return true;
			});

		}

		@Property
		void monthOnlyMonths(@ForAll @Size(min = 1) Set<Month> months, @ForAll Random random) {

			Arbitrary<MonthDay> dates = Dates.monthDays().onlyMonths(months.toArray(new Month[]{}));

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md.getMonth()).isIn(months);
				return true;
			});

		}

		@Provide
		Arbitrary<Integer> months() {
			return Arbitraries.integers().between(1, 12);
		}

	}

	@Group
	class DayOfMonthMethods {

		@Property
		void dayOfMonthBetween(
			@ForAll("dayOfMonths") int startDayOfMonth,
			@ForAll("dayOfMonths") int endDayOfMonth,
			@ForAll Random random
		) {

			Assume.that(startDayOfMonth <= endDayOfMonth);

			Arbitrary<MonthDay> dates = Dates.monthDays().dayOfMonthBetween(startDayOfMonth, endDayOfMonth);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md.getDayOfMonth()).isGreaterThanOrEqualTo(startDayOfMonth);
				assertThat(md.getDayOfMonth()).isLessThanOrEqualTo(endDayOfMonth);
				return true;
			});

		}

		@Property
		void dayOfMonthBetweenSame(@ForAll("dayOfMonths") int dayOfMonth, @ForAll Random random) {

			Arbitrary<MonthDay> dates = Dates.monthDays().dayOfMonthBetween(dayOfMonth, dayOfMonth);

			assertAllGenerated(dates.generator(1000, true), random, md -> {
				assertThat(md.getDayOfMonth()).isEqualTo(dayOfMonth);
				return true;
			});

		}

		@Provide
		Arbitrary<Integer> dayOfMonths() {
			return Arbitraries.integers().between(1, 31);
		}

	}

}
