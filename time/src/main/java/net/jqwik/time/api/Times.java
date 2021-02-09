package net.jqwik.time.api;

import java.time.*;
import java.util.*;

import org.apiguardian.api.*;

import net.jqwik.api.*;
import net.jqwik.time.api.arbitraries.*;
import net.jqwik.time.internal.properties.arbitraries.*;

import static org.apiguardian.api.API.Status.*;

@API(status = EXPERIMENTAL, since = "1.4.1")
public class Times {

	private Times() {
		// Must never be called
	}

	/**
	 * Create an arbitrary that generates instances of {@linkplain java.time.LocalTime}.
	 *
	 * @return a new arbitrary instance
	 */
	public static LocalTimeArbitrary times() {
		return new DefaultLocalTimeArbitrary();
	}

	/**
	 * Create an arbitrary that generates instances of {@linkplain java.time.OffsetTime}.
	 *
	 * @return a new arbitrary instance
	 */
	public static OffsetTimeArbitrary offsetTimes() {
		return new DefaultOffsetTimeArbitrary();
	}

	/**
	 * Create an arbitrary that generates instances of {@linkplain java.util.TimeZone}.
	 *
	 * @return a new arbitrary instance
	 */
	public static Arbitrary<TimeZone> timeZones() {
		return Arbitraries.of(TimeZone.getAvailableIDs()).map(TimeZone::getTimeZone);
	}

	/**
	 * Create an arbitrary that generates instances of {@linkplain java.time.ZoneId}.
	 *
	 * @return a new arbitrary instance
	 */
	public static Arbitrary<ZoneId> zoneIds() {
		return Arbitraries.of(ZoneId.getAvailableZoneIds()).map(ZoneId::of);
	}

}
