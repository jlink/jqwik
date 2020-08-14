package net.jqwik.api;

import java.math.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.api.constraints.*;
import net.jqwik.engine.properties.*;

import static java.math.BigInteger.*;
import static org.assertj.core.api.Assertions.*;

import static net.jqwik.api.ArbitraryTestHelper.*;

@Label("Arbitraries")
class ArbitrariesTests {

	enum MyEnum {
		Yes,
		No,
		Maybe
	}

	@Example
	void randomValues() {
		Arbitrary<String> stringArbitrary = Arbitraries.randomValue(random -> Integer.toString(random.nextInt(10)));
		RandomGenerator<String> generator = stringArbitrary.generator(1);
		assertAllGenerated(generator, value -> Integer.parseInt(value) < 10);
		assertAtLeastOneGeneratedOf(generator, "1", "2", "3", "4", "5", "6", "7", "8", "9");
	}

	@Example
	void fromGenerator() {
		Arbitrary<String> stringArbitrary =
			Arbitraries.fromGenerator(random -> Shrinkable.unshrinkable(Integer.toString(random.nextInt(10))));
		RandomGenerator<String> generator = stringArbitrary.generator(1);
		assertAllGenerated(generator, value -> Integer.parseInt(value) < 10);
	}

	@Example
	void ofValues() {
		Arbitrary<String> stringArbitrary = Arbitraries.of("1", "hallo", "test");
		RandomGenerator<String> generator = stringArbitrary.generator(1);
		assertAllGenerated(generator, (String value) -> Arrays.asList("1", "hallo", "test").contains(value));
		assertAtLeastOneGeneratedOf(generator, "1", "hallo", "test");
	}

	@Example
	void ofValueList() {
		List<String> valueList = Arrays.asList("1", "hallo", "test");
		Arbitrary<String> stringArbitrary = Arbitraries.of(valueList);
		RandomGenerator<String> generator = stringArbitrary.generator(1);
		assertAllGenerated(generator, (String value) -> Arrays.asList("1", "hallo", "test").contains(value));
		assertAtLeastOneGeneratedOf(generator, "1", "hallo", "test");
	}

	@Example
	void ofNonNullableValueList() {
		// TODO: Replace with List.of("a", "b") when moving to JDK >= 11
		List<String> valueList = new ArrayList<String>() {
			@Override
			public boolean contains(final Object o) {
				if (o == null) {
					throw new NullPointerException();
				}
				return super.contains(o);
			}
		};
		valueList.add("a");
		valueList.add("b");

		Arbitrary<String> stringArbitrary = Arbitraries.of(valueList);
		RandomGenerator<String> generator = stringArbitrary.generator(1);
		assertAllGenerated(generator, (String value) -> Arrays.asList("a", "b").contains(value));
	}

	@Example
	void ofValueSet() {
		Set<String> valueSet = new HashSet<>(Arrays.asList("1", "hallo", "test"));
		Arbitrary<String> stringArbitrary = Arbitraries.of(valueSet);
		RandomGenerator<String> generator = stringArbitrary.generator(1);
		assertAllGenerated(generator, (String value) -> Arrays.asList("1", "hallo", "test").contains(value));
		assertAtLeastOneGeneratedOf(generator, "1", "hallo", "test");
	}

	@Example
	void ofSuppliers() {
		Arbitrary<List<String>> listArbitrary = Arbitraries.ofSuppliers(ArrayList::new, ArrayList::new);
		RandomGenerator<List<String>> generator = listArbitrary.generator(1);
		assertAllGenerated(generator, (List<String> value) -> {
			assertThat(value).isEmpty();
			value.add("aString");
		});
	}

	@Example
	void ofSupplierList() {
		@SuppressWarnings("unchecked")
		Supplier<List<String>>[] suppliers = new Supplier[]{ArrayList::new, ArrayList::new};
		List<Supplier<List<String>>> supplierList = Arrays.asList(suppliers);
		Arbitrary<List<String>> listArbitrary = Arbitraries.ofSuppliers(supplierList);
		RandomGenerator<List<String>> generator = listArbitrary.generator(1);
		assertAllGenerated(generator, (List<String> value) -> {
			assertThat(value).isEmpty();
			value.add("aString");
		});
	}

	@Example
	void ofSupplierSet() {
		@SuppressWarnings("unchecked")
		Supplier<List<String>>[] suppliers = new Supplier[]{ArrayList::new, ArrayList::new};
		Set<Supplier<List<String>>> supplierList = new HashSet<>(Arrays.asList(suppliers));
		Arbitrary<List<String>> listArbitrary = Arbitraries.ofSuppliers(supplierList);
		RandomGenerator<List<String>> generator = listArbitrary.generator(1);
		assertAllGenerated(generator, (List<String> value) -> {
			assertThat(value).isEmpty();
			value.add("aString");
		});
	}

	@Example
	void ofEnum() {
		Arbitrary<MyEnum> enumArbitrary = Arbitraries.of(MyEnum.class);
		RandomGenerator<MyEnum> generator = enumArbitrary.generator(1);
		assertAllGenerated(generator, (MyEnum value) -> Arrays.asList(MyEnum.class.getEnumConstants()).contains(value));
		assertAtLeastOneGeneratedOf(generator, MyEnum.values());
	}

	@Example
	@Deprecated
	void samplesAreGeneratedDeterministicallyInRoundRobin() {
		Arbitrary<Integer> integerArbitrary = Arbitraries.samples(-5, 0, 3);
		RandomGenerator<Integer> generator = integerArbitrary.generator(1);
		ArbitraryTestHelper.assertGeneratedExactly(generator, -5, 0, 3, -5, 0, 3);
	}

	@Example
	void randoms() {
		Arbitrary<Random> randomArbitrary = Arbitraries.randoms();
		RandomGenerator<Random> generator = randomArbitrary.generator(1);
		assertAllGenerated(generator, (Random value) -> value.nextInt(100) < 100);
	}

	/**
	 * Remove this test as soon as Arbitraries.constant(value) is removed
	 */
	@Example
	@SuppressWarnings("deprecation")
	void constant() {
		Arbitrary<String> constant = Arbitraries.constant("hello");
		assertAllGenerated(constant.generator(1000), value -> {
			assertThat(value).isEqualTo("hello");
		});
	}

	@Example
	void just() {
		Arbitrary<String> constant = Arbitraries.just("hello");
		assertAllGenerated(constant.generator(1000), value -> {
			assertThat(value).isEqualTo("hello");
		});
	}

	@Example
	void create() {
		Arbitrary<String> constant = Arbitraries.create(() -> "hello");
		assertAllGenerated(constant.generator(1000), value -> {
			assertThat(value).isEqualTo("hello");
		});
	}

	@Example
	void forType() {
		TypeArbitrary<Person> constant = Arbitraries.forType(Person.class);
		assertAllGenerated(constant.generator(1000), value -> {
			assertThat(value).isInstanceOf(Person.class);
		});
	}

	private static class Person {
		public Person(String firstName, String lastName) {
		}

		public static Person create(String firstName) {
			return new Person(firstName, "Stranger");
		}
	}

	@Group
	@Label("shuffle(..)")
	class Shuffle {
		@Example
		void varArgsValues() {
			Arbitrary<List<Integer>> shuffled = Arbitraries.shuffle(1, 2, 3);
			assertPermutations(shuffled);
		}

		@Example
		void noValues() {
			Arbitrary<List<Integer>> shuffled = Arbitraries.shuffle();
			assertAllGenerated(
				shuffled.generator(1000),
				list -> { assertThat(list).isEmpty();}
			);
		}

		@Example
		void listOfValues() {
			Arbitrary<List<Integer>> shuffled = Arbitraries.shuffle(Arrays.asList(1, 2, 3));
			assertPermutations(shuffled);
		}

		private void assertPermutations(Arbitrary<List<Integer>> shuffled) {
			assertAtLeastOneGeneratedOf(
				shuffled.generator(1000),
				Arrays.asList(1, 2, 3),
				Arrays.asList(1, 3, 2),
				Arrays.asList(2, 3, 1),
				Arrays.asList(2, 1, 3),
				Arrays.asList(3, 1, 2),
				Arrays.asList(3, 2, 1)
			);
		}
	}

	@Group
	@Label("oneOf(..)")
	class OneOf {

		@Example
		void choosesOneOfManyArbitraries() {
			Arbitrary<Integer> one = Arbitraries.of(1);
			Arbitrary<Integer> two = Arbitraries.of(2);
			Arbitrary<Integer> threeToFive = Arbitraries.of(3, 4, 5);

			Arbitrary<Integer> oneOfArbitrary = Arbitraries.oneOf(one, two, threeToFive);
			assertAllGenerated(oneOfArbitrary.generator(1000), value -> {
				assertThat(value).isIn(1, 2, 3, 4, 5);
			});

			RandomGenerator<Integer> generator = oneOfArbitrary.generator(1000);
			assertAtLeastOneGeneratedOf(generator, 1, 2, 3, 4, 5);
		}

		@Example
		void choosesOneOfDifferentCovariantTypes() {
			Arbitrary<Integer> ones = Arbitraries.of(1);
			Arbitrary<String> twos = Arbitraries.of("2");

			Arbitrary<?> anyOfArbitrary = Arbitraries.oneOf(ones, twos);

			RandomGenerator<?> generator = anyOfArbitrary.generator(1000);

			assertAllGenerated(generator, value -> {
				assertThat(value).isIn(1, "2");
			});

			assertAtLeastOneGeneratedOf(generator, 1, "2");
		}

		@Property
		void willHandDownConfigurations(@ForAll("stringLists") @Size(10) Collection<?> stringList) {
			assertThat(stringList).hasSize(10);
			assertThat(stringList).allMatch(element -> element instanceof String);
		}

		@Provide
		Arbitrary<List<String>> stringLists() {
			return Arbitraries.oneOf(
				Arbitraries.strings().ofLength(2).list(),
				Arbitraries.strings().ofLength(3).list()
			);
		}
	}

	@Group
	@Label("frequencyOf(..)")
	class FrequencyOf {

		@Example
		void choosesOneOfManyAccordingToFrequency(@ForAll Random random) {
			Arbitrary<Integer> one = Arbitraries.of(1);
			Arbitrary<Integer> two = Arbitraries.of(2);

			Arbitrary<Integer> frequencyOfArbitrary = Arbitraries.frequencyOf(Tuple.of(10, one), Tuple.of(1, two));
			assertAllGenerated(frequencyOfArbitrary.generator(1000), value -> {
				assertThat(value).isIn(1, 2);
			});

			RandomGenerator<Integer> generator = frequencyOfArbitrary.generator(1000);
			assertAtLeastOneGeneratedOf(generator, 1, 2);

			List<Integer> elements = generator.stream(random).map(Shrinkable::value).limit(100).collect(Collectors.toList());
			int countOnes = Collections.frequency(elements, 1);
			int countTwos = Collections.frequency(elements, 2);

			assertThat(countOnes).isGreaterThan(countTwos * 2);
		}

		@Property
		void willHandDownConfigurations(@ForAll("stringLists") @Size(10) Collection<?> stringList) {
			assertThat(stringList).hasSize(10);
			assertThat(stringList).allMatch(element -> element instanceof String);
		}

		@Provide
		Arbitrary<List<String>> stringLists() {
			return Arbitraries.frequencyOf(
				Tuple.of(1, Arbitraries.strings().ofLength(2).list()),
				Tuple.of(2, Arbitraries.strings().ofLength(3).list())
			);
		}
	}

	@Example
	void recursive() {
		Arbitrary<Integer> base = Arbitraries.integers().between(0, 5);
		Arbitrary<Integer> integer = Arbitraries.recursive(
			() -> base,
			list -> list.map(i -> i + 1),
			10
		);

		ArbitraryTestHelper.assertAllGenerated(integer.generator(1000), result -> {
			assertThat(result).isBetween(10, 15);
		});
	}

	@Group
	class Lazy {

		@Example
		void lazy() {
			Arbitrary<Integer> samples = Arbitraries.lazy(() -> new OrderedArbitraryForTesting<>(1, 2, 3));

			ArbitraryTestHelper.assertGeneratedExactly(samples.generator(1000), 1, 2, 3, 1);
			ArbitraryTestHelper.assertGeneratedExactly(samples.generator(1000), 1, 2, 3, 1);
		}

		@Example
		void recursiveLazy() {
			Arbitrary<Tree> trees = trees();
			ArbitraryTestHelper.assertAllGenerated(
				trees.generator(1000),
				tree -> {
					//System.out.println(tree);
					return tree != null;
				}
			);
		}

		private Arbitrary<Tree> trees() {
			return Combinators.combine(aName(), aBranch(), aBranch()).as(Tree::new);
		}

		private Arbitrary<String> aName() {
			return Arbitraries.strings().alpha().ofLength(3);
		}

		private Arbitrary<Tree> aBranch() {
			return Arbitraries.lazy(() -> Arbitraries.frequencyOf(
				Tuple.of(2, Arbitraries.just(null)),
				Tuple.of(1, trees())
			));
		}

	}

	@Group
	class LazyOf {
		@Example
		void lazyOf() {
			ArbitraryTestHelper.assertAllGenerated(
				trees().generator(1000),
				tree -> {
					//System.out.println(tree);
					return tree != null;
				}
			);
		}

		private Arbitrary<Tree> trees() {
			return Combinators.combine(aName(), aBranch(), aBranch()).as(Tree::new);
		}

		private Arbitrary<String> aName() {
			return Arbitraries.strings().alpha().ofLength(3);
		}

		private Arbitrary<Tree> aBranch() {
			return Arbitraries.lazyOf(
				() -> Arbitraries.just(null),
				() -> Arbitraries.just(null),
				this::trees
			);
		}

	}

	class Tree {
		final String name;
		final Tree left;
		final Tree right;

		public Tree(final String name, final Tree left, final Tree right) {
			this.name = name;
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return String.format("%s[%s]", name, depth());
		}

		private int depth() {
			if (left == null && right == null) {
				return 0;
			}
			return Math.max(
				left == null ? 0 : left.depth() + 1,
				right == null ? 0 : right.depth() + 1
			);
		}
	}

	@Group
	@Label("frequency(..)")
	class Frequency {

		@Example
		void onePair() {
			Arbitrary<String> one = Arbitraries.frequency(Tuple.of(1, "a"));
			assertAllGenerated(one.generator(1000), value -> {return value.equals("a");});
		}

		@Property(tries = 10)
		void twoEqualPairs() {
			Arbitrary<String> one = Arbitraries.frequency(Tuple.of(1, "a"), Tuple.of(1, "b"));
			Map<String, Long> counts = ArbitraryTestHelper.count(one.generator(1000), 1000);
			assertThat(counts.get("a") > 200).isTrue();
			assertThat(counts.get("b") > 200).isTrue();
		}

		@Property(tries = 10)
		void twoUnequalPairs() {
			Arbitrary<String> one = Arbitraries.frequency(Tuple.of(1, "a"), Tuple.of(10, "b"));
			Map<String, Long> counts = ArbitraryTestHelper.count(one.generator(1000), 1000);
			assertThat(counts.get("a")).isLessThan(counts.get("b"));
		}

		@Property(tries = 10)
		void fourUnequalPairs() {
			Arbitrary<String> one = Arbitraries.frequency(
				Tuple.of(1, "a"),
				Tuple.of(5, "b"),
				Tuple.of(10, "c"),
				Tuple.of(20, "d")
			);
			Map<String, Long> counts = ArbitraryTestHelper.count(one.generator(1000), 1000);
			assertThat(counts.get("a")).isLessThan(counts.get("b"));
			assertThat(counts.get("b")).isLessThan(counts.get("c"));
			assertThat(counts.get("c")).isLessThan(counts.get("d"));
		}

		@Example
		void noPositiveFrequencies() {
			assertThatThrownBy(() -> Arbitraries.frequency(Tuple.of(0, "a"))).isInstanceOf(JqwikException.class);
		}

	}

	@Group
	@Label("defaultFor(..)")
	class DefaultFor {
		@Example
		void simpleType() {
			Arbitrary<Integer> integerArbitrary = Arbitraries.defaultFor(Integer.class);
			assertAllGenerated(integerArbitrary.generator(1000), Objects::nonNull);
		}

		@SuppressWarnings("rawtypes")
		@Example
		void parameterizedType() {
			Arbitrary<List> list = Arbitraries.defaultFor(List.class, String.class);
			assertAllGenerated(list.generator(1000), List.class::isInstance);
		}

		@SuppressWarnings("rawtypes")
		@Example
		void moreThanOneDefault() {
			Arbitrary<Collection> collections = Arbitraries.defaultFor(Collection.class, String.class);
			ArbitraryTestHelper.assertAtLeastOneGenerated(collections.generator(1000), List.class::isInstance);
			ArbitraryTestHelper.assertAtLeastOneGenerated(collections.generator(1000), Set.class::isInstance);
		}

		@Property(tries = 100)
		void defaultForParameterizedType(@ForAll("stringLists") @Size(10) List<?> stringList) {
			assertThat(stringList).hasSize(10);
			assertThat(stringList).allMatch(element -> element instanceof String);
		}

		@SuppressWarnings("rawtypes")
		@Provide
		Arbitrary<List> stringLists() {
			return Arbitraries.defaultFor(List.class, String.class);
		}
	}

	@Group
	@Label("chars()")
	class Chars {
		@Example
		void charsDefault() {
			Arbitrary<Character> arbitrary = Arbitraries.chars();
			RandomGenerator<Character> generator = arbitrary.generator(1);
			assertAllGenerated(generator, Objects::nonNull);
		}

		@Example
		void chars() {
			Arbitrary<Character> arbitrary = Arbitraries.chars().range('a', 'd');
			RandomGenerator<Character> generator = arbitrary.generator(1);
			List<Character> allowedChars = Arrays.asList('a', 'b', 'c', 'd');
			assertAllGenerated(generator, (Character value) -> allowedChars.contains(value));
		}
	}

	@Group
	@Label("strings()")
	class Strings {
		@Example
		void string() {
			Arbitrary<String> stringArbitrary = Arbitraries.strings() //
														   .withCharRange('a', 'd') //
														   .ofMinLength(0).ofMaxLength(5);
			RandomGenerator<String> generator = stringArbitrary.generator(1);
			assertGeneratedString(generator, 0, 5);
		}

		@Property
		void stringWithFixedLength(@ForAll @IntRange(min = 1, max = 10) int size) {
			Arbitrary<String> stringArbitrary = Arbitraries.strings() //
														   .withCharRange('a', 'a') //
														   .ofMinLength(size).ofMaxLength(size);
			RandomGenerator<String> generator = stringArbitrary.generator(1);
			assertAllGenerated(generator, value -> value.length() == size);
			assertAllGenerated(generator, (String value) -> value.chars().allMatch(i -> i == 'a'));
		}

		@Example
		void stringFromCharset() {
			char[] validChars = new char[]{'a', 'b', 'c', 'd'};
			Arbitrary<String> stringArbitrary = Arbitraries.strings() //
														   .withChars(validChars) //
														   .ofMinLength(2).ofMaxLength(5);
			RandomGenerator<String> generator = stringArbitrary.generator(1);
			assertGeneratedString(generator, 2, 5);
		}
	}

	@Group
	@Label("Integrals")
	class IntegralNumbers {

		@Example
		void shorts() {
			Arbitrary<Short> enumArbitrary = Arbitraries.shorts();
			RandomGenerator<Short> generator = enumArbitrary.generator(100);
			assertAllGenerated(generator, (Short value) -> value >= Short.MIN_VALUE && value <= Short.MAX_VALUE);
		}

		@Example
		void shortsMinsAndMaxes() {
			Arbitrary<Short> enumArbitrary = Arbitraries.shorts().between((short) -10, (short) 10);
			RandomGenerator<Short> generator = enumArbitrary.generator(100);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value < 0 && value > -5);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value > 0 && value < 5);
			assertAllGenerated(generator, value -> value >= -10 && value <= 10);
		}

		@Example
		void bytes() {
			Arbitrary<Byte> enumArbitrary = Arbitraries.bytes();
			RandomGenerator<Byte> generator = enumArbitrary.generator(1);
			assertAllGenerated(generator, (Byte value) -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE);
		}

		@Example
		void bytesMinsAndMaxes() {
			Arbitrary<Byte> enumArbitrary = Arbitraries.bytes().between((byte) -10, (byte) 10);
			RandomGenerator<Byte> generator = enumArbitrary.generator(1);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value < 0 && value > -5);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value > 0 && value < 5);
			assertAllGenerated(generator, value -> value >= -10 && value <= 10);
		}

		@Example
		void integerMinsAndMaxes() {
			RandomGenerator<Integer> generator = Arbitraries.integers().generator(1);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == Integer.MIN_VALUE);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == Integer.MAX_VALUE);
		}

		@Example
		void integersInt() {
			Arbitrary<Integer> intArbitrary = Arbitraries.integers().between(-10, 10);
			RandomGenerator<Integer> generator = intArbitrary.generator(10);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value < 0 && value > -5);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value > 0 && value < 5);
			assertAllGenerated(generator, value -> value >= -10 && value <= 10);
		}

		@Example
		void longMinsAndMaxes() {
			RandomGenerator<Long> generator = Arbitraries.longs().generator(1);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == Long.MIN_VALUE);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == Long.MAX_VALUE);
		}

		@Example
		void integersLong() {
			Arbitrary<Long> longArbitrary = Arbitraries.longs().between(-100L, 100L);
			RandomGenerator<Long> generator = longArbitrary.generator(1000);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value < -50);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value > 50);
			assertAllGenerated(generator, value -> value >= -100L && value <= 100L);
		}

		@Example
		void bigIntegers() {
			Arbitrary<BigInteger> bigIntegerArbitrary = Arbitraries.bigIntegers().between(valueOf(-100L), valueOf(100L));
			RandomGenerator<BigInteger> generator = bigIntegerArbitrary.generator(1);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(valueOf(-50L)) < 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(valueOf(50L)) > 0);
			assertAllGenerated(
				generator,
				value -> value.compareTo(valueOf(-100L)) >= 0
							 && value.compareTo(valueOf(100L)) <= 0
			);
		}

		@Property(tries = 10)
		void bigIntegersWithUniformDistribution() {
			Arbitrary<BigInteger> bigIntegerArbitrary =
				Arbitraries.bigIntegers()
						   .between(valueOf(-1000L), valueOf(1000L))
						   .withDistribution(RandomDistribution.uniform());
			RandomGenerator<BigInteger> generator = bigIntegerArbitrary.generator(1);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.longValue() > -1000 && value.longValue() < -980);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.longValue() < 1000 && value.longValue() > 980);
			assertAllGenerated(
				generator,
				value -> value.compareTo(valueOf(-1000L)) >= 0 && value.compareTo(valueOf(1000L)) <= 0
			);
		}

		@Example
		void integralEdgeCasesAreGenerated() {
			BigInteger min = valueOf(Integer.MIN_VALUE);
			BigInteger max = valueOf(Integer.MAX_VALUE);
			BigInteger shrinkingTarget = valueOf(101);
			Arbitrary<BigInteger> bigIntegerArbitrary = Arbitraries.bigIntegers().between(min, max).shrinkTowards(shrinkingTarget);
			RandomGenerator<BigInteger> generator = bigIntegerArbitrary.generator(1000);
			assertAtLeastOneGeneratedOf(
				generator,
				shrinkingTarget,
				valueOf(-2), valueOf(-1),
				valueOf(0),
				valueOf(1), valueOf(2),
				min, max
			);
		}
	}

	@Group
	@Label("doubles()")
	class Doubles {

		@Example
		void doubleMinsAndMaxes() {
			RandomGenerator<Double> generator = Arbitraries.doubles().generator(1);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == 0.01);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == -0.01);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == -Double.MAX_VALUE);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == Double.MAX_VALUE);
		}

		@Example
		void doubles() {
			Arbitrary<Double> doubleArbitrary = Arbitraries.doubles().between(-10.0, 10.0).ofScale(2);
			RandomGenerator<Double> generator = doubleArbitrary.generator(1);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value == 0.0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value < -1.0 && value > -9.0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value > 1.0 && value < 9.0);
			assertAllGenerated(generator, value -> {
				double rounded = Math.round(value * 100) / 100.0;
				return value >= -10.0 && value <= 10.0 && value == rounded;
			});
		}

		@Example
		void doublesWithMaximumRange() {
			double min = -Double.MAX_VALUE;
			Arbitrary<Double> doubleArbitrary = Arbitraries.doubles().between(min, Double.MAX_VALUE).ofScale(2);
			RandomGenerator<Double> generator = doubleArbitrary.generator(100);

			assertAtLeastOneGeneratedOf(generator, 0.0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value < -1000.0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value > 1000.0);
		}

		@Example
		void doublesBorderIsMorePreciseThanScale() {
			double min = 0.001;
			double max = 0.199;
			Arbitrary<Double> arbitrary = Arbitraries.doubles().between(min, max).ofScale(2);
			assertThatThrownBy(() -> arbitrary.generator(1)).isInstanceOf(JqwikException.class);
		}

		@Example
		void excludedBordersDontAllowValueCreation() {
			double min = 0.01;
			double max = 0.02;
			Arbitrary<Double> arbitrary = Arbitraries.doubles().between(min, false, max, false).ofScale(2);
			assertThatThrownBy(() -> arbitrary.generator(1)).isInstanceOf(JqwikException.class);
		}

		@Example
		void doublesWithBordersExcluded() {
			double min = 1.0;
			double max = 2.0;
			Arbitrary<Double> doubleArbitrary = Arbitraries.doubles().between(min, false, max, false).ofScale(1);
			RandomGenerator<Double> generator = doubleArbitrary.generator(100);
			assertAllGenerated(generator, value -> value > min && value < max);
		}

		@Example
		void doublesLessThan() {
			double max = 2.0;
			Arbitrary<Double> doubleArbitrary = Arbitraries.doubles().lessThan(max).ofScale(0);
			RandomGenerator<Double> generator = doubleArbitrary.generator(100);
			assertAllGenerated(generator, value -> value < max);
		}

		@Example
		void doublesLessOrEqual() {
			double max = 2.0;
			Arbitrary<Double> doubleArbitrary = Arbitraries.doubles().lessOrEqual(max).ofScale(0);
			RandomGenerator<Double> generator = doubleArbitrary.generator(100);
			assertAllGenerated(generator, value -> value <= max);
		}

		@Example
		void doublesGreaterThan() {
			double min = 2.0;
			Arbitrary<Double> doubleArbitrary = Arbitraries.doubles().greaterThan(min).ofScale(0);
			RandomGenerator<Double> generator = doubleArbitrary.generator(100);
			assertAllGenerated(generator, value -> value > min);
		}

		@Example
		void doublesGreaterOrEqual() {
			double min = 2.0;
			Arbitrary<Double> doubleArbitrary = Arbitraries.doubles().greaterOrEqual(min).ofScale(0);
			RandomGenerator<Double> generator = doubleArbitrary.generator(100);
			assertAllGenerated(generator, value -> value >= min);
		}

		@Example
		void doublesWithShrinkingTargetOutsideBorders() {
			Arbitrary<Double> arbitrary = Arbitraries.doubles()
													 .between(1.0, 10.0)
													 .shrinkTowards(-1.0);
			assertThatThrownBy(() -> arbitrary.generator(1)).isInstanceOf(JqwikException.class);
		}

	}

	@Group
	@Label("floats()")
	class Floats {

		@Example
		void floatMinsAndMaxes() {
			RandomGenerator<Float> generator = Arbitraries.floats().generator(1);
			assertAtLeastOneGeneratedOf(generator, 0.01f, -0.01f, -Float.MAX_VALUE, Float.MAX_VALUE);
		}

		@Example
		void floats() {
			Arbitrary<Float> floatArbitrary = Arbitraries.floats().between(-10.0f, 10.0f).ofScale(2);
			RandomGenerator<Float> generator = floatArbitrary.generator(1);

			assertAtLeastOneGeneratedOf(generator, 0.0f);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value < -1.0 && value > -9.0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value > 1.0 && value < 9.0);
			assertAllGenerated(generator, value -> {
				float rounded = (float) (Math.round(value * 100) / 100.0);
				return value >= -10.0 && value <= 10.0 && value == rounded;
			});
		}

		@Example
		void floatsWithBordersExcluded() {
			float min = 1.0f;
			float max = 2.0f;
			Arbitrary<Float> floatArbitrary = Arbitraries.floats().between(min, false, max, false).ofScale(1);
			RandomGenerator<Float> generator = floatArbitrary.generator(100);
			assertAllGenerated(generator, value -> value > min && value < max);
		}

		@Example
		void floatsLessThan() {
			float max = 2.0f;
			Arbitrary<Float> floatArbitrary = Arbitraries.floats().lessThan(max).ofScale(0);
			RandomGenerator<Float> generator = floatArbitrary.generator(100);
			assertAllGenerated(generator, value -> value < max);
		}

		@Example
		void floatsLessOrEqual() {
			float max = 2.0f;
			Arbitrary<Float> floatArbitrary = Arbitraries.floats().lessOrEqual(max).ofScale(0);
			RandomGenerator<Float> generator = floatArbitrary.generator(100);
			assertAllGenerated(generator, value -> value <= max);
		}

		@Example
		void floatsGreaterThan() {
			float min = 2.0f;
			Arbitrary<Float> floatArbitrary = Arbitraries.floats().greaterThan(min).ofScale(0);
			RandomGenerator<Float> generator = floatArbitrary.generator(100);
			assertAllGenerated(generator, value -> value > min);
		}

		@Example
		void floatsGreaterOrEqual() {
			float min = 2.0f;
			Arbitrary<Float> floatArbitrary = Arbitraries.floats().greaterOrEqual(min).ofScale(0);
			RandomGenerator<Float> generator = floatArbitrary.generator(100);
			assertAllGenerated(generator, value -> value >= min);
		}

		@Example
		void floatsWithShrinkingTargetOutsideBorders() {
			Arbitrary<Float> arbitrary = Arbitraries.floats()
													.between(1.0f, 10.0f)
													.shrinkTowards(-1.0f);
			assertThatThrownBy(() -> arbitrary.generator(1)).isInstanceOf(JqwikException.class);
		}

	}

	@Group
	@Label("bigDecimals()")
	class BigDecimals {
		@Example
		void bigDecimals() {
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals()
														 .between(BigDecimal.valueOf(-100.0), BigDecimal.valueOf(100.0))
														 .ofScale(2)
														 .shrinkTowards(BigDecimal.valueOf(4.2));
			RandomGenerator<BigDecimal> generator = arbitrary.generator(1);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.valueOf(4.2)) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.valueOf(-100.0)) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.valueOf(100.0)) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.ZERO) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.ONE) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.ONE.negate()) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.doubleValue() < -1.0 && value.doubleValue() > -9.0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.doubleValue() > 1.0 && value.doubleValue() < 9.0);
			assertAllGenerated(generator, value -> value.scale() <= 2);
		}

		@Example
		void bigDecimalsLessOrEqual() {
			BigDecimal max = BigDecimal.valueOf(10);
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals().lessOrEqual(max);
			RandomGenerator<BigDecimal> generator = arbitrary.generator(1);
			assertAllGenerated(generator, value -> value.compareTo(max) <= 0);
		}

		@Example
		void bigDecimalsLessThan() {
			BigDecimal max = BigDecimal.valueOf(10);
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals().lessThan(max).ofScale(1);
			RandomGenerator<BigDecimal> generator = arbitrary.generator(1);
			assertAllGenerated(generator, value -> value.compareTo(max) < 0);
		}

		@Example
		void bigDecimalsGreaterOrEqual() {
			BigDecimal min = BigDecimal.valueOf(10);
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals().greaterOrEqual(min);
			RandomGenerator<BigDecimal> generator = arbitrary.generator(1);
			assertAllGenerated(generator, value -> value.compareTo(min) >= 0);
		}

		@Example
		void bigDecimalsGreaterThan() {
			BigDecimal min = BigDecimal.valueOf(10);
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals().greaterThan(min).ofScale(1);
			RandomGenerator<BigDecimal> generator = arbitrary.generator(1);
			assertAllGenerated(generator, value -> value.compareTo(min) > 0);
		}

		@Example
		void bigDecimalsWithShrinkingTargetOutsideBorders() {
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals()
														 .between(BigDecimal.ONE, BigDecimal.TEN)
														 .shrinkTowards(BigDecimal.valueOf(-1));
			assertThatThrownBy(() -> arbitrary.generator(1)).isInstanceOf(JqwikException.class);
		}

		@Example
		void bigDecimalsWithBordersExcluded() {
			Range<BigDecimal> range = Range.of(BigDecimal.valueOf(-10.0), false, BigDecimal.valueOf(10.0), false);
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals()
														 .between(range.min, range.minIncluded, range.max, range.maxIncluded)
														 .ofScale(1);
			RandomGenerator<BigDecimal> generator = arbitrary.generator(1000);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.ZERO) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.ONE) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.valueOf(-1)) == 0);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.compareTo(BigDecimal.ONE.negate()) == 0);
			assertAllGenerated(generator, range::includes);
		}

		@Property(tries = 10)
		void bigDecimalsWithUniformDistribution() {
			Range<BigDecimal> range = Range.of(BigDecimal.valueOf(-1000.0), BigDecimal.valueOf(1000.0));
			Arbitrary<BigDecimal> arbitrary = Arbitraries.bigDecimals()
														 .between(range.min, range.max)
														 .ofScale(0)
														 .withDistribution(RandomDistribution.uniform());
			RandomGenerator<BigDecimal> generator = arbitrary.generator(1);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.longValue() > -1000 && value.longValue() < -980);
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, value -> value.longValue() < 1000 && value.longValue() > 980);
			assertAllGenerated(
				generator,
				value -> value.compareTo(BigDecimal.valueOf(-1000L)) >= 0
							 && value.compareTo(BigDecimal.valueOf(1000L)) <= 0
			);
		}

	}

	@Group
	class GenericTypes {

		@Example
		void optional() {
			Arbitrary<String> stringArbitrary = Arbitraries.of("one", "two");
			Arbitrary<Optional<String>> optionalArbitrary = stringArbitrary.optional();

			RandomGenerator<Optional<String>> generator = optionalArbitrary.generator(1);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, optional -> optional.orElse("").equals("one"));
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, optional -> optional.orElse("").equals("two"));
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, optional -> !optional.isPresent());
		}

		@Example
		void map() {
			Arbitrary<Integer> keys = Arbitraries.integers().between(1, 10);
			Arbitrary<String> values = Arbitraries.strings().alpha().ofLength(5);

			MapArbitrary<Integer, String> mapArbitrary = Arbitraries.maps(keys, values).ofMinSize(0).ofMaxSize(10);

			RandomGenerator<Map<Integer, String>> generator = mapArbitrary.generator(1);

			assertAllGenerated(generator, map -> {
				assertThat(map.size()).isBetween(0, 10);
				if (map.isEmpty()) return;
				assertThat(map.keySet()).containsAnyOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
				assertThat(map.values()).allMatch(value -> value.length() == 5);
			});

			assertAtLeastOneGenerated(generator, Map::isEmpty);
			assertAtLeastOneGenerated(generator, map -> map.size() == 10);

			// Generated maps are mutable
			assertAllGenerated(generator, map -> {
				int sizeBefore = map.size();
				map.put(42, "fortytwo");
				assertThat(map.size()).isEqualTo(sizeBefore + 1);
			});
		}

		@Example
		void mapWithLessElementsThanMaxSize() {
			Arbitrary<Integer> keys = Arbitraries.integers().between(1, 3);
			Arbitrary<String> values = Arbitraries.strings().alpha().ofLength(5);

			MapArbitrary<Integer, String> mapArbitrary = Arbitraries.maps(keys, values);
			RandomGenerator<Map<Integer, String>> generator = mapArbitrary.generator(1);

			assertAllGenerated(generator, map -> {
				assertThat(map.size()).isBetween(0, 3);
			});

			assertAtLeastOneGenerated(generator, Map::isEmpty);
			assertAtLeastOneGenerated(generator, map -> map.size() == 3);
		}

		@Example
		void entry() {
			Arbitrary<Integer> keys = Arbitraries.integers().between(1, 10);
			Arbitrary<String> values = Arbitraries.strings().alpha().ofLength(5);

			Arbitrary<Map.Entry<Integer, String>> entryArbitrary = Arbitraries.entries(keys, values);

			RandomGenerator<Map.Entry<Integer, String>> generator = entryArbitrary.generator(1);

			assertAllGenerated(generator, entry -> {
				assertThat((int) entry.getKey()).isBetween(1, 10);
				assertThat(entry.getValue()).hasSize(5);
			});

			// Generated entries are mutable
			assertAllGenerated(generator, entry -> {
				entry.setValue("fortytwo");
				assertThat(entry.getValue()).isEqualTo("fortytwo");
			});
		}

	}

	private void assertGeneratedString(RandomGenerator<String> generator, int minLength, int maxLength) {
		assertAllGenerated(generator, value -> value.length() >= minLength && value.length() <= maxLength);
		List<Character> allowedChars = Arrays.asList('a', 'b', 'c', 'd');
		assertAllGenerated(
			generator,
			(String value) -> value.chars().allMatch(i -> allowedChars.contains((char) i))
		);
	}
}