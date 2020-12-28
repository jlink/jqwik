package net.jqwik.api.time;

import org.apiguardian.api.*;

import net.jqwik.time.*;

import static org.apiguardian.api.API.Status.*;

@API(status = EXPERIMENTAL, since = "1.4.0")
public class Dates {

	private Dates() {
		// Must never be called
	}

	/**
	 * Create an arbitrary that generates local dates.
	 *
	 * @return a new arbitrary instance
	 */
	public static DateArbitrary dates() {
		return new DefaultDateArbitrary();
	}

	/**
	 * Create an arbitrary that generates years.
	 *
	 * @return a new arbitrary instance
	 */
	public static YearArbitrary years() {
		return new DefaultYearArbitrary();
	}

	/**
	 * Create an arbitrary that generates months.
	 *
	 * @return a new arbitrary instance
	 */
	public static MonthArbitrary months() {
		return new DefaultMonthArbitrary();
	}

	/**
	 * Create an arbitrary that generates days of month.
	 *
	 * @return a new arbitrary instance
	 */
	public static DaysOfMonthArbitrary daysOfMonth() {
		return new DefaultDaysOfMonthArbitrary();
	}

	/**
	 * Create an arbitrary that generates year and months.
	 *
	 * @return a new arbitrary instance
	 */
	public static YearMonthArbitrary yearMonths() {
		return new DefaultYearMonthArbitrary();
	}

	/**
	 * Create an arbitrary that generates month and days.
	 *
	 * @return a new arbitrary instance
	 */
	public static MonthDayArbitrary monthDays() {
		return new DefaultMonthDayArbitrary();
	}

}
