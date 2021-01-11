package net.jqwik.time.internal.properties.configurators;

import java.time.*;

import net.jqwik.api.*;
import net.jqwik.api.configurators.*;
import net.jqwik.api.providers.*;
import net.jqwik.time.api.arbitraries.*;
import net.jqwik.time.api.constraints.*;

public class MonthRangeConfigurator extends ArbitraryConfiguratorBase {

	@Override
	protected boolean acceptTargetType(TypeUsage targetType) {
		return targetType.isAssignableFrom(LocalDate.class) || targetType.isAssignableFrom(YearMonth.class) || targetType
																													   .isAssignableFrom(MonthDay.class);
	}

	public Arbitrary<?> configure(Arbitrary<?> arbitrary, MonthRange range) {
		if (arbitrary instanceof DateArbitrary) {
			DateArbitrary dateArbitrary = (DateArbitrary) arbitrary;
			return dateArbitrary.monthBetween(range.min(), range.max());
		} else if (arbitrary instanceof YearMonthArbitrary) {
			YearMonthArbitrary yearMonthArbitrary = (YearMonthArbitrary) arbitrary;
			return yearMonthArbitrary.monthBetween(range.min(), range.max());
		} else if (arbitrary instanceof MonthDayArbitrary) {
			MonthDayArbitrary monthDayArbitrary = (MonthDayArbitrary) arbitrary;
			return monthDayArbitrary.monthBetween(range.min(), range.max());
		} else {
			return arbitrary;
		}
	}
}