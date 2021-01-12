package net.jqwik.time.providers;

import java.time.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.providers.*;
import net.jqwik.time.api.*;
import net.jqwik.time.api.arbitraries.*;

public class DatesArbitraryProvider implements ArbitraryProvider {
	@Override
	public boolean canProvideFor(TypeUsage targetType) {
		return targetType.isAssignableFrom(LocalDate.class) || targetType.isAssignableFrom(Calendar.class);
	}

	@Override
	public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
		DateArbitrary dateArbitrary = Dates.dates();
		if (targetType.isAssignableFrom(Calendar.class)) {
			return Collections.singleton(dateArbitrary.asCalendar());
		}
		return Collections.singleton(dateArbitrary);
	}
}
