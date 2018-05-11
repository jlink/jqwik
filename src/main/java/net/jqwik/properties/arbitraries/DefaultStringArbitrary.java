package net.jqwik.properties.arbitraries;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;

import java.util.*;
import java.util.stream.*;

public class DefaultStringArbitrary extends AbstractArbitraryBase implements StringArbitrary {

	private static final int DEFAULT_MAX_LENGTH = 255;

	private List<Arbitrary<Character>> characterArbitraries = new ArrayList<>();

	private int minLength = 0;
	private int maxLength = DEFAULT_MAX_LENGTH;

	private CharacterArbitrary defaultCharacterArbitrary() {
		return Arbitraries.chars().all();
	}

	@Override
	public RandomGenerator<String> generator(int genSize) {
		final int cutoffLength = RandomGenerators.defaultCutoffSize(minLength, maxLength, genSize);
		List<NShrinkable<String>> samples = Arrays.stream(new String[] { "" })
				.filter(s -> s.length() >= minLength && s.length() <= maxLength).map(NShrinkable::unshrinkable)
				.collect(Collectors.toList());
		return RandomGenerators.strings(createCharacterGenerator(), minLength, maxLength, cutoffLength).withEdgeCases(genSize, samples);
	}

	@Override
	public StringArbitrary ofMinLength(int minLength) {
		DefaultStringArbitrary clone = typedClone();
		clone.minLength = minLength;
		return clone;
	}

	@Override
	public StringArbitrary ofMaxLength(int maxLength) {
		DefaultStringArbitrary clone = typedClone();
		clone.maxLength = maxLength;
		return clone;
	}

	@Override
	public StringArbitrary withChars(char[] chars) {
		DefaultStringArbitrary clone = typedClone();
		clone.addChars(chars);
		return clone;
	}

	@Override
	public StringArbitrary withCharRange(char from, char to) {
		if (from == 0 && to == 0) {
			return this;
		}
		DefaultStringArbitrary clone = typedClone();
		clone.addCharRange(from, to);
		return clone;
	}

	@Override
	public StringArbitrary ascii() {
		DefaultStringArbitrary clone = typedClone();
		clone.characterArbitraries.add(Arbitraries.chars().ascii());
		return clone;
	}

	@Override
	public StringArbitrary alpha() {
		DefaultStringArbitrary clone = typedClone();
		clone.addCharRange('a', 'z');
		clone.addCharRange('A', 'Z');
		return clone;
	}

	@Override
	public StringArbitrary numeric() {
		DefaultStringArbitrary clone = typedClone();
		clone.addCharRange('0', '9');
		return clone;
	}

	private void addCharRange(char from, char to) {
		characterArbitraries.add(Arbitraries.chars().between(from, to));
	}

	private void addChars(char[] chars) {
		characterArbitraries.add(Arbitraries.of(chars));
	}

	private RandomGenerator<Character> createCharacterGenerator() {
		if (characterArbitraries.isEmpty()) {
			return defaultCharacterArbitrary().generator(1);
		}
		Arbitrary<Character> first = characterArbitraries.get(0);
		@SuppressWarnings("unchecked")
		Arbitrary<Character>[] rest = characterArbitraries.subList(1, characterArbitraries.size()).toArray(new Arbitrary[characterArbitraries.size() - 1]);

		Arbitrary<Character> allValidCharacters = Arbitraries.oneOf(first, rest);
		return allValidCharacters.generator(1);
	}

}
