package net.jqwik.api.constraints;

import java.util.*;

import net.jqwik.api.*;

class ArraysConstraintsProperties {

	@Property
	boolean sizeAppliesToArrays(@ForAll @Size(min = 2, max = 7) String[] aValue) {
		return aValue.length >= 2 && aValue.length <= 7;
	}

	@Property
	boolean constraintAnnotationIsHandedDownToComponentType(@ForAll @Size(1) @StringLength(2) String[] aValue) {
		return aValue.length == 1 && aValue[0].length() == 2;
	}

	@Property
	boolean handingDownAlsoWorksInNestedArrays(@ForAll Optional<@Size(1) @StringLength(2) String[]> optional) {
		return optional.map(aValue -> aValue[0].length() == 2).orElse(true);
	}

	@Property
	boolean sizeAppliesToVarargs(@ForAll @Size(min = 2, max = 7) String... aValue) {
		return aValue.length >= 2 && aValue.length <= 7;
	}

	@Property
	boolean constraintAnnotationIsHandedDownToVarargsComponentType(@ForAll @Size(1) @StringLength(2) String... aValue) {
		return aValue[0].length() == 2;
	}

}
