package net.jqwik.time.api;

import java.time.*;

import net.jqwik.api.*;
import net.jqwik.time.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

@Group
public class ConstraintTests {

	@Group
	class DateConstraints {

		@Property
		void dateRangeBetween(@ForAll @DateRange(min = "2013-05-25", max = "2020-08-23") LocalDate date) {
			assertThat(date).isAfterOrEqualTo(LocalDate.of(2013, Month.MAY, 25));
			assertThat(date).isBeforeOrEqualTo(LocalDate.of(2020, Month.AUGUST, 23));
		}

		@Property
		void yearRangeBetween500And700(@ForAll @YearRange(min = 500, max = 700) LocalDate date) {
			assertThat(date.getYear()).isGreaterThanOrEqualTo(500);
			assertThat(date.getYear()).isLessThanOrEqualTo(700);
		}

		@Property
		void monthRangeBetweenMarchAndJuly(@ForAll @MonthRange(min = Month.MARCH, max = Month.JULY) LocalDate date) {
			assertThat(date.getMonth()).isGreaterThanOrEqualTo(Month.MARCH);
			assertThat(date.getMonth()).isLessThanOrEqualTo(Month.JULY);
		}

		@Property
		void dayOfMonthRangeBetween15And20Integer(@ForAll @DayOfMonthRange(min = 15, max = 20) LocalDate date) {
			assertThat(date.getDayOfMonth()).isGreaterThanOrEqualTo(15);
			assertThat(date.getDayOfMonth()).isLessThanOrEqualTo(20);
		}

		@Property
		void dayOfWeekRangeOnlyMonday(@ForAll @DayOfWeekRange(max = DayOfWeek.MONDAY) LocalDate date) {
			assertThat(date.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
		}

		@Property
		void dayOfWeekRangeOnlySunday(@ForAll @DayOfWeekRange(min = DayOfWeek.SUNDAY) LocalDate date) {
			assertThat(date.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
		}

		@Property
		void dayOfWeekRangeBetweenTuesdayAndFriday(@ForAll @DayOfWeekRange(min = DayOfWeek.TUESDAY, max = DayOfWeek.FRIDAY) LocalDate date) {
			assertThat(date.getDayOfWeek()).isGreaterThanOrEqualTo(DayOfWeek.TUESDAY);
			assertThat(date.getDayOfWeek()).isLessThanOrEqualTo(DayOfWeek.FRIDAY);
		}

	}

	@Group
	class YearMonthConstraints {

		@Property
		void yearMonthRangeBetween(@ForAll @YearMonthRange(min = "2013-05", max = "2020-08") YearMonth yearMonth) {
			assertThat(yearMonth).isGreaterThanOrEqualTo(YearMonth.of(2013, Month.MAY));
			assertThat(yearMonth).isLessThanOrEqualTo(YearMonth.of(2020, Month.AUGUST));
		}

		@Property
		void yearRangeBetween500And700(@ForAll @YearRange(min = 500, max = 700) YearMonth yearMonth) {
			assertThat(yearMonth.getYear()).isGreaterThanOrEqualTo(500);
			assertThat(yearMonth.getYear()).isLessThanOrEqualTo(700);
		}

		@Property
		void monthRangeBetweenMarchAndJuly(@ForAll @MonthRange(min = Month.MARCH, max = Month.JULY) YearMonth yearMonth) {
			assertThat(yearMonth.getMonth()).isGreaterThanOrEqualTo(Month.MARCH);
			assertThat(yearMonth.getMonth()).isLessThanOrEqualTo(Month.JULY);
		}

	}

	@Group
	class MonthDayConstraints {

		@Property
		void monthDayRangeBetween(@ForAll @MonthDayRange(min = "05-25", max = "08-23") MonthDay monthDay) {
			assertThat(monthDay).isGreaterThanOrEqualTo(MonthDay.of(Month.MAY, 25));
			assertThat(monthDay).isLessThanOrEqualTo(MonthDay.of(Month.AUGUST, 23));
		}

		@Property
		void monthRangeBetweenMarchAndJuly(@ForAll @MonthRange(min = Month.MARCH, max = Month.JULY) MonthDay monthDay) {
			assertThat(monthDay.getMonth()).isGreaterThanOrEqualTo(Month.MARCH);
			assertThat(monthDay.getMonth()).isLessThanOrEqualTo(Month.JULY);
		}

	}

	@Group
	class YearConstraints {

		@Property
		void yearRangeBetweenMinus100And100(@ForAll @YearRange(min = -100, max = 100) Year year) {
			assertThat(year.getValue()).isGreaterThanOrEqualTo(-100);
			assertThat(year.getValue()).isLessThanOrEqualTo(100);
			assertThat(year).isNotEqualTo(Year.of(0));
		}

		@Property
		void yearRangeBetween3000And3500(@ForAll @YearRange(min = 3000, max = 3500) Year year) {
			assertThat(year.getValue()).isGreaterThanOrEqualTo(3000);
			assertThat(year.getValue()).isLessThanOrEqualTo(3500);
		}

	}

	@Group
	class DayOfMonthConstraints {

		@Property
		void dayOfMonthRangeBetween15And20(@ForAll @DayOfMonth @DayOfMonthRange(min = 15, max = 20) int dayOfMonth) {
			assertThat(dayOfMonth).isGreaterThanOrEqualTo(15);
			assertThat(dayOfMonth).isLessThanOrEqualTo(20);
		}

		@Property
		void dayOfMonthRangeBetween15And20Integer(@ForAll @DayOfMonth @DayOfMonthRange(min = 15, max = 20) Integer dayOfMonth) {
			assertThat(dayOfMonth).isGreaterThanOrEqualTo(15);
			assertThat(dayOfMonth).isLessThanOrEqualTo(20);
		}

	}

}
