package net.jqwik.api.time;

import java.time.*;

import org.apiguardian.api.*;

import net.jqwik.api.*;

import static org.apiguardian.api.API.Status.*;

/**
 * Fluent interface to configure the generation of year and month values.
 */
@API(status = EXPERIMENTAL, since = "1.4.0")
public interface YearMonthArbitrary extends Arbitrary<YearMonth> {

	/**
	 * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of generated year and month values.
	 */
	default YearMonthArbitrary between(YearMonth min, YearMonth max) {
		return atTheEarliest(min).atTheLatest(max);
	}

	/**
	 * Set the allowed lower {@code min} (included) bounder of generated year and month values.
	 */
	YearMonthArbitrary atTheEarliest(YearMonth min);

	/**
	 * Set the allowed upper {@code max} (included) bounder of generated year and month values.
	 */
	YearMonthArbitrary atTheLatest(YearMonth max);

	/**
	 * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of generated year values.
	 */
	YearMonthArbitrary yearBetween(Year min, Year max);

	/**
	 * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of generated year values.
	 * The {@code int} values can be between {@code Year.MIN_VALUE} and {@code Year.MAX_VALUE}.
	 */
	default YearMonthArbitrary yearBetween(int min, int max) {
		return yearBetween(Year.of(min), Year.of(max));
	}

	/**
	 * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of generated month values.
	 */
	YearMonthArbitrary monthBetween(Month min, Month max);

	/**
	 * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of generated month values.
	 * The {@code int} values can be between 1 and 12.
	 */
	default YearMonthArbitrary monthBetween(int min, int max) {
		return monthBetween(Month.of(min), Month.of(max));
	}

	/**
	 * Set an array of allowed {@code months}.
	 */
	YearMonthArbitrary onlyMonths(Month... months);

}
