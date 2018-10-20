package net.jqwik.properties;

import java.math.*;
import java.util.*;
import java.util.stream.*;

import net.jqwik.api.*;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@Group
@Label("Exhaustive Generation")
class ExhaustiveGenerationTests {

	enum MyEnum {
		Yes,
		No,
		Maybe
	}

	@Example
	@Label("Arbitrary.map()")
	void mapping() {
		Optional<ExhaustiveGenerator<String>> optionalGenerator = Arbitraries.integers().between(-5, 5).map(i -> Integer.toString(i)).exhaustive();
		assertThat(optionalGenerator).isPresent();

		ExhaustiveGenerator<String> generator = optionalGenerator.get();
		assertThat(generator.maxCount()).isEqualTo(11);
		assertThat(generator).containsExactly("-5", "-4", "-3", "-2", "-1", "0", "1", "2", "3", "4", "5");
	}

	@Example
	@Label("Arbitrary.filter()")
	void filtering() {
		Optional<ExhaustiveGenerator<Integer>> optionalGenerator = Arbitraries.integers().between(-5, 5)
																			  .filter(i -> i % 2 == 0)
																			  .exhaustive();
		assertThat(optionalGenerator).isPresent();

		ExhaustiveGenerator<Integer> generator = optionalGenerator.get();
		assertThat(generator.maxCount()).isEqualTo(11); // Cannot know the number of filtered elements in advance
		assertThat(generator).containsExactly(-4, -2, 0, 2, 4);
	}

	@Example
	@Label("Arbitrary.injectNull(): null is prepended")
	void withNull() {
		double doesNotMatter = 0.5;
		Optional<ExhaustiveGenerator<String>> optionalGenerator = Arbitraries.of("abc", "def").injectNull(doesNotMatter).exhaustive();
		assertThat(optionalGenerator).isPresent();

		ExhaustiveGenerator<String> generator = optionalGenerator.get();
		assertThat(generator.maxCount()).isEqualTo(3);
		assertThat(generator).containsExactly(null, "abc", "def");
	}

	@Example
	@Label("Arbitrary.withSamples(): samples are prepended")
	void withSamples() {
		Optional<ExhaustiveGenerator<String>> optionalGenerator =
			Arbitraries.of("abc", "def")
					   .withSamples("s1", "s2").exhaustive();
		assertThat(optionalGenerator).isPresent();

		ExhaustiveGenerator<String> generator = optionalGenerator.get();
		assertThat(generator.maxCount()).isEqualTo(4);
		assertThat(generator).containsExactly("s1", "s2", "abc", "def");
	}

	@Example
	@Label("Arbitrary.fixGenSize() has no influence on exhaustive generation")
	void fixGenSize() {
		int doesNotMatter = 42;
		Optional<ExhaustiveGenerator<String>> optionalGenerator = Arbitraries.of("abc", "def").fixGenSize(doesNotMatter).exhaustive();
		assertThat(optionalGenerator).isPresent();

		ExhaustiveGenerator<String> generator = optionalGenerator.get();
		assertThat(generator.maxCount()).isEqualTo(2);
		assertThat(generator).containsExactly("abc", "def");
	}

// TODO: Uniqueness for exhaustive generation probably requires majo refactoring
//	@Group
//	@Label("Arbitrary.unique()")
//	class Unique {
//		@Example
//		@Label("filter out duplicates")
//		void unique() {
//			Optional<ExhaustiveGenerator<Integer>> optionalGenerator = Arbitraries.of(1, 2, 1, 3, 1, 2).unique().exhaustive();
//			assertThat(optionalGenerator).isPresent();
//
//			ExhaustiveGenerator<Integer> generator = optionalGenerator.get();
//			assertThat(generator.maxCount()).isEqualTo(6); // Cannot know the number of unique elements in advance
//			assertThat(generator).containsExactly(1, 2, 3);
//		}
//
//		@Example
//		@Label("uniqueness within list")
//		void uniqueWithinList() {
//			Optional<ExhaustiveGenerator<List<Integer>>> optionalGenerator = Arbitraries.of(1, 2, 3).unique().list().ofSize(3).exhaustive();
//			assertThat(optionalGenerator).isPresent();
//
//			ExhaustiveGenerator<List<Integer>> generator = optionalGenerator.get();
//			assertThat(generator.maxCount()).isEqualTo(27); // Cannot know the number of unique elements in advance
//			assertThat(generator).containsExactly(
//				asList(1, 2, 3),
//				asList(2, 3, 1),
//				asList(3, 1, 2),
//				asList(1, 3, 2),
//				asList(2, 1, 3),
//				asList(3, 2, 1)
//			);
//		}
//
//		@Property
//		@Label("reset of uniqueness for embedded arbitraries")
//		void uniquenessIsResetForEmbeddedArbitraries(@ForAll("listOfUniqueIntegers") List<Integer> aList) {
//			Assertions.assertThat(aList.size()).isEqualTo(new HashSet<>(aList).size());
//		}
//
//		@Provide
//		Arbitrary<List<Integer>> listOfUniqueIntegers() {
//			return Arbitraries.integers().between(1, 10).unique().list().ofSize(3);
//		}
//	}

	@Group
	class OfValues {

		@Example
		void booleans() {
			Optional<ExhaustiveGenerator<Boolean>> optionalGenerator = Arbitraries.of(true, false).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Boolean> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(2);
			assertThat(generator).containsExactly(true, false);
		}

		@Example
		void samples() {
			Optional<ExhaustiveGenerator<String>> optionalGenerator = Arbitraries.of("a", "b", "c", "d").exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<String> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(4);
			assertThat(generator).containsExactly("a", "b", "c", "d");
		}

		@Example
		void enums() {
			Optional<ExhaustiveGenerator<MyEnum>> optionalGenerator = Arbitraries.of(MyEnum.class).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<MyEnum> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(3);
			assertThat(generator).containsExactly(MyEnum.Yes, MyEnum.No, MyEnum.Maybe);
		}

	}

	@Group
	class Integers {
		@Example
		void fromMinToMax() {
			Optional<ExhaustiveGenerator<Integer>> optionalGenerator = Arbitraries.integers().between(-10, 10).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Integer> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(21);
			assertThat(generator).containsExactly(-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		}

		@Example
		void rangeTooBig() {
			Optional<ExhaustiveGenerator<Integer>> optionalGenerator = Arbitraries.integers().between(-1, Integer.MAX_VALUE).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	class Longs {
		@Example
		void fromMinToMax() {
			Optional<ExhaustiveGenerator<Long>> optionalGenerator = Arbitraries.longs().between(-10, 10).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Long> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(21);
			assertThat(generator).containsExactly(-10L, -9L, -8L, -7L, -6L, -5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
		}

		@Example
		void rangeTooBig() {
			Optional<ExhaustiveGenerator<Long>> optionalGenerator = Arbitraries.longs().between(-1, Long.MAX_VALUE).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	class BigIntegers {
		@Example
		void fromMinToMax() {
			Optional<ExhaustiveGenerator<BigInteger>> optionalGenerator = Arbitraries.bigIntegers().between(BigInteger.valueOf(-2), BigInteger.valueOf(2)).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<BigInteger> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(5);
			assertThat(generator).containsExactly(BigInteger.valueOf(-2), BigInteger.valueOf(-1), BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2));
		}

		@Example
		void rangeTooBig() {
			Optional<ExhaustiveGenerator<BigInteger>> optionalGenerator = Arbitraries.bigIntegers().between(BigInteger.valueOf(Long.MIN_VALUE), BigInteger.ZERO).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	class Shorts {
		@Example
		void fromMinToMax() {
			Optional<ExhaustiveGenerator<Short>> optionalGenerator = Arbitraries.shorts().between((short) -5, (short) 5).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Short> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(11);
			assertThat(generator).containsExactly((short) -5, (short) -4, (short) -3, (short) -2, (short) -1, (short) 0, (short) 1, (short) 2, (short) 3, (short) 4, (short) 5);
		}

		@Example
		void rangeCannotBeTooBig() {
			Optional<ExhaustiveGenerator<Short>> optionalGenerator = Arbitraries.shorts().between(Short.MIN_VALUE, Short.MAX_VALUE).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Short> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(65536);
		}
	}

	@Group
	class Bytes {
		@Example
		void fromMinToMax() {
			Optional<ExhaustiveGenerator<Byte>> optionalGenerator = Arbitraries.bytes().between((byte) -5, (byte) 5).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Byte> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(11);
			assertThat(generator).containsExactly((byte) -5, (byte) -4, (byte) -3, (byte) -2, (byte) -1, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5);
		}

		@Example
		void rangeCannotBeTooBig() {
			Optional<ExhaustiveGenerator<Byte>> optionalGenerator = Arbitraries.bytes().between(Byte.MIN_VALUE, Byte.MAX_VALUE).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Byte> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(256);
		}
	}

	@Group
	class Chars {
		@Example
		void fromMinToMax() {
			Optional<ExhaustiveGenerator<Character>> optionalGenerator = Arbitraries.chars().between('a', 'f').exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Character> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(6);
			assertThat(generator).containsExactly('a', 'b', 'c', 'd', 'e', 'f');
		}

		@Example
		void rangeCannotBeTooBig() {
			Optional<ExhaustiveGenerator<Character>> optionalGenerator = Arbitraries.chars().between(Character.MIN_VALUE, Character.MAX_VALUE).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Character> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(65536);
		}

		@Example
		@Label("Arbitraries.of(char[])")
		void arbitrariesOf() {
			Optional<ExhaustiveGenerator<Character>> optionalGenerator = Arbitraries.of(new char[] {'a', 'c', 'e', 'X'}).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Character> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(4);
			assertThat(generator).containsExactly('a', 'c', 'e', 'X');
		}
	}

	@Group
	class Lists {
		@Example
		void listsAreCombinationsOfElementsUpToMaxLength() {
			Optional<ExhaustiveGenerator<List<Integer>>> optionalGenerator =
				Arbitraries.integers().between(1, 3).list().ofMaxSize(2).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<List<Integer>> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(13);
			assertThat(generator).containsExactly(
				asList(),
				asList(1),
				asList(2),
				asList(3),
				asList(1, 1),
				asList(1, 2),
				asList(1, 3),
				asList(2, 1),
				asList(2, 2),
				asList(2, 3),
				asList(3, 1),
				asList(3, 2),
				asList(3, 3)
			);
		}

		@Example
		void elementArbitraryNotExhaustive() {
			Optional<ExhaustiveGenerator<List<Double>>> optionalGenerator =
				Arbitraries.doubles().between(1, 10).list().ofMaxSize(1).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}

		@Example
		void tooManyCombinations() {
			Optional<ExhaustiveGenerator<List<Integer>>> optionalGenerator =
				Arbitraries.integers().between(1, 10).list().ofMaxSize(10).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	class Streams {
		@Example
		void streamsAreCombinationsOfElementsUpToMaxLength() {
			Optional<ExhaustiveGenerator<Stream<Integer>>> optionalGenerator =
				Arbitraries.integers().between(1, 2).stream().ofMaxSize(2).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Stream<Integer>> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(7);
			assertThat(generator.map(s -> s.collect(Collectors.toList()))).containsExactly(
				asList(),
				asList(1),
				asList(2),
				asList(1, 1),
				asList(1, 2),
				asList(2, 1),
				asList(2, 2)
			);
		}

		@Example
		void elementArbitraryNotExhaustive() {
			Optional<ExhaustiveGenerator<Stream<Double>>> optionalGenerator =
				Arbitraries.doubles().between(1, 10).stream().ofMaxSize(1).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}

		@Example
		void tooManyCombinations() {
			Optional<ExhaustiveGenerator<Stream<Integer>>> optionalGenerator =
				Arbitraries.integers().between(1, 10).stream().ofMaxSize(10).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}
	
	@Group
	class Arrays {
		@Example
		void arraysAreCombinationsOfElementsUpToMaxLength() {
			Optional<ExhaustiveGenerator<Integer[]>> optionalGenerator =
				Arbitraries.integers().between(1, 2).array(Integer[].class)
						   .ofMaxSize(2).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Integer[]> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(7);
			assertThat(generator).containsExactly(
				new Integer[] {},
				new Integer[] {1},
				new Integer[] {2},
				new Integer[] {1, 1},
				new Integer[] {1, 2},
				new Integer[] {2, 1},
				new Integer[] {2, 2}
			);
		}

		@Example
		void elementArbitraryNotExhaustive() {
			Optional<ExhaustiveGenerator<Double[]>> optionalGenerator =
				Arbitraries.doubles().between(1, 10).array(Double[].class).ofMaxSize(1).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}

		@Example
		void tooManyCombinations() {
			Optional<ExhaustiveGenerator<Integer[]>> optionalGenerator =
				Arbitraries.integers().between(1, 10).array(Integer[].class).ofMaxSize(10).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	class Sets {
		@Example
		void setsAreCombinationsOfElementsUpToMaxLength() {
			Optional<ExhaustiveGenerator<Set<Integer>>> optionalGenerator =
				Arbitraries.integers().between(1, 3).set().ofMaxSize(2).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Set<Integer>> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(7);
			assertThat(generator).containsExactly(
				asSet(),
				asSet(1),
				asSet(2),
				asSet(3),
				asSet(1, 2),
				asSet(1, 3),
				asSet(2, 3)
			);
		}

		private Set<Integer> asSet(Integer...ints) {
			return new HashSet<>(asList(ints));
		}

		@Example
		void elementArbitraryNotExhaustive() {
			Optional<ExhaustiveGenerator<Set<Double>>> optionalGenerator =
				Arbitraries.doubles().between(1, 10).set().ofMaxSize(1).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}

		@Example
		void tooManyCombinations() {
			Optional<ExhaustiveGenerator<Set<Integer>>> optionalGenerator =
				Arbitraries.integers().between(1, 25).set().ofMaxSize(10).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	@Label("Optional")
	class OptionalTests {
		@Example
		void prependsOptionalEmpty() {
			Optional<ExhaustiveGenerator<java.util.Optional<Integer>>> optionalGenerator =
				Arbitraries.integers().between(1, 5).optional().exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<Optional<Integer>> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(6);
			assertThat(generator).containsExactly(
				Optional.empty(),
				Optional.of(1),
				Optional.of(2),
				Optional.of(3),
				Optional.of(4),
				Optional.of(5)
			);
		}

		@Example
		void elementArbitraryNotExhaustive() {
			Optional<ExhaustiveGenerator<Optional<Double>>> optionalGenerator =
				Arbitraries.doubles().between(1, 10).optional().exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	@Label("Combinators")
	class CombinatorsTests {

		@Example
		void combine2() {
			Arbitrary<Integer> a1020 = Arbitraries.of(10, 20);
			Arbitrary<Integer> a12 = Arbitraries.of(1, 2);
			Arbitrary<Integer> plus = Combinators.combine(a1020, a12).as((i1, i2) -> i1 + i2);

			assertThat(plus.exhaustive()).isPresent();

			ExhaustiveGenerator<Integer> generator = plus.exhaustive().get();
			assertThat(generator).containsExactly(11, 12, 21, 22);
		}

	}

}
