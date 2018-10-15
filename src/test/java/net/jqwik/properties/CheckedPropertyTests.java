package net.jqwik.properties;

import java.util.*;
import java.util.function.*;

import org.junit.platform.engine.reporting.*;
import org.opentest4j.*;

import net.jqwik.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.descriptor.*;
import net.jqwik.execution.*;
import net.jqwik.support.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.TestHelper.*;
import static net.jqwik.api.GenerationMode.*;
import static net.jqwik.properties.PropertyCheckResult.Status.*;
import static net.jqwik.properties.PropertyConfigurationBuilder.aConfig;

@Group
class CheckedPropertyTests {

	private static final Consumer<ReportEntry> NULL_PUBLISHER = entry -> {
	};

	@Group
	class CheckedPropertyCreation {
		@Example
		void createCheckedPropertyWithoutParameters() {
			PropertyMethodDescriptor descriptor =
				(PropertyMethodDescriptor) TestDescriptorBuilder
											   .forMethod(CheckingExamples.class, "propertyWithoutParameters", int.class)
											   .build();
			CheckedPropertyFactory factory = new CheckedPropertyFactory();
			CheckedProperty checkedProperty = factory.fromDescriptor(descriptor, new Object());

			assertThat(checkedProperty.configuration.getStereotype()).isEqualTo(Property.DEFAULT_STEREOTYPE);
			assertThat(checkedProperty.configuration.getTries()).isEqualTo(TestDescriptorBuilder.TRIES);
			assertThat(checkedProperty.configuration.getMaxDiscardRatio()).isEqualTo(TestDescriptorBuilder.MAX_DISCARD_RATIO);
			assertThat(checkedProperty.configuration.getShrinkingMode()).isEqualTo(ShrinkingMode.BOUNDED);
		}

		@Example
		void createCheckedPropertyWithTriesParameter() {
			PropertyMethodDescriptor descriptor =
				(PropertyMethodDescriptor) TestDescriptorBuilder
											   .forMethod(CheckingExamples.class, "propertyWith42TriesAndMaxDiscardRatio2", int.class)
											   .build();
			CheckedPropertyFactory factory = new CheckedPropertyFactory();
			CheckedProperty checkedProperty = factory.fromDescriptor(descriptor, new Object());

			assertThat(checkedProperty.configuration.getStereotype()).isEqualTo("OtherStereotype");
			assertThat(checkedProperty.configuration.getTries()).isEqualTo(42);
			assertThat(checkedProperty.configuration.getMaxDiscardRatio()).isEqualTo(2);
			assertThat(checkedProperty.configuration.getShrinkingMode()).isEqualTo(ShrinkingMode.OFF);
		}
	}

	@Group
	class PropertyChecking {
		@Example
		void intParametersSuccess() {
			intOnlyExample("prop0", params -> params.size() == 0, SATISFIED);
			intOnlyExample("prop1", params -> params.size() == 1, SATISFIED);
			intOnlyExample("prop2", params -> params.size() == 2, SATISFIED);
			intOnlyExample("prop8", params -> params.size() == 8, SATISFIED);
		}

		@Example
		void intParametersFailure() {
			intOnlyExample("prop0", params -> false, FALSIFIED);
			intOnlyExample("prop1", params -> false, FALSIFIED);
			intOnlyExample("prop2", params -> false, FALSIFIED);
			intOnlyExample("prop8", params -> false, FALSIFIED);
		}

		@Example
		void exceptionDuringCheck() {
			RuntimeException toThrow = new RuntimeException("test");
			intOnlyExample("prop0", params -> {
				throw toThrow;
			}, ERRONEOUS);
			intOnlyExample("prop8", params -> {
				throw toThrow;
			}, ERRONEOUS);
		}

		@Example
		void testAbortMakesCheckExhausted() {
			RuntimeException toThrow = new TestAbortedException("test");
			intOnlyExample("prop0", params -> {
				throw toThrow;
			}, EXHAUSTED);
			intOnlyExample("prop8", params -> {
				throw toThrow;
			}, EXHAUSTED);
		}

		@Example
		void assertionErrorMakesCheckFalsified() {
			AssertionError toThrow = new AssertionError("test");
			intOnlyExample("prop0", params -> {
				throw toThrow;
			}, FALSIFIED);
			intOnlyExample("prop8", params -> {
				throw toThrow;
			}, FALSIFIED);
		}

		@Example
		void ifNoArbitraryForParameterCanBeFound_checkIsErroneous() {
			List<MethodParameter> parameters = getParametersForMethod("stringProp");

			CheckedProperty checkedProperty = new CheckedProperty(
				"stringProp",
				params -> false,
				parameters,
				p -> Collections.emptySet(),
				Optional.empty(),
				aConfig().build()
			);

			PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
			assertThat(check.status()).isEqualTo(PropertyCheckResult.Status.ERRONEOUS);
			assertThat(check.throwable()).isPresent();
			assertThat(check.throwable().get()).isInstanceOf(CannotFindArbitraryException.class);
		}

		@Example
		void usingASeedWillAlwaysProvideSameArbitraryValues() {
			List<Integer> allGeneratedInts = new ArrayList<>();
			CheckedFunction addIntToList = params -> allGeneratedInts.add((int) params.get(0));
			CheckedProperty checkedProperty = new CheckedProperty(
				"prop1", addIntToList, getParametersForMethod("prop1"),
				p -> Collections.singleton(new GenericArbitrary(Arbitraries.integers().between(-100, 100))),
				Optional.empty(),
				aConfig().withSeed("42").withTries(20).build()
			);

			PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
			assertThat(check.randomSeed()).isEqualTo("42");

			assertThat(check.status()).isEqualTo(SATISFIED);
			assertThat(allGeneratedInts).containsExactly(0, -56, 1, -1, -100, 3, 31, 27, -27, 53, -77, 8, -60, 69, 29, -6, -7, 38, 37, -10);
		}

		@Group
		class DataDrivenProperty {
			@Example
			@Label("works with GenerationMode.AUTO")
			void runWithGenerationModeAuto() {
				List<Tuple.Tuple2> allGeneratedParameters = new ArrayList<>();
				CheckedFunction rememberParameters = params -> allGeneratedParameters.add(Tuple.of(params.get(0), params.get(1)));
				CheckedProperty checkedProperty = new CheckedProperty(
					"dataDrivenProperty", rememberParameters, getParametersForMethod("dataDrivenProperty"),
					p -> Collections.emptySet(),
					Optional.of(Table.of(Tuple.of(1, "1"), Tuple.of(3, "Fizz"), Tuple.of(5, "Buzz"))),
					aConfig().withGeneration(AUTO).build()
				);

				PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
				assertThat(check.generation()).isEqualTo(GenerationMode.DATA_DRIVEN);
				assertThat(check.countTries()).isEqualTo(3);
				assertThat(check.status()).isEqualTo(SATISFIED);
				assertThat(allGeneratedParameters).containsExactly(Tuple.of(1, "1"), Tuple.of(3, "Fizz"), Tuple.of(5, "Buzz"));
			}

			@Example
			@Label("works with GenerationMode.DATA_DRIVEN")
			void runWithGenerationModeDataDriven() {
				List<Tuple.Tuple2> allGeneratedParameters = new ArrayList<>();
				CheckedFunction rememberParameters = params -> allGeneratedParameters.add(Tuple.of(params.get(0), params.get(1)));
				CheckedProperty checkedProperty = new CheckedProperty(
					"dataDrivenProperty", rememberParameters, getParametersForMethod("dataDrivenProperty"),
					p -> Collections.emptySet(),
					Optional.of(Table.of(Tuple.of(1, "1"), Tuple.of(3, "Fizz"), Tuple.of(5, "Buzz"))),
					aConfig().withGeneration(DATA_DRIVEN).build()
				);

				PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
				assertThat(check.generation()).isEqualTo(GenerationMode.DATA_DRIVEN);
				assertThat(check.countTries()).isEqualTo(3);
				assertThat(check.status()).isEqualTo(SATISFIED);
				assertThat(allGeneratedParameters).containsExactly(Tuple.of(1, "1"), Tuple.of(3, "Fizz"), Tuple.of(5, "Buzz"));
			}

			@Example
			@Label("fails if it has GenerationMode.RANDOMIZED")
			void failIfItHasGenerationModeRandomized() {
				CheckedProperty checkedProperty = new CheckedProperty(
					"dataDrivenProperty", params -> true, getParametersForMethod("dataDrivenProperty"),
					p -> Collections.emptySet(),
					Optional.of(Table.of(Tuple.of(1, "1"))),
					aConfig().withGeneration(RANDOMIZED).build()
				);

				assertThatThrownBy(() -> checkedProperty.check(NULL_PUBLISHER, new Reporting[0])).isInstanceOf(JqwikException.class);
			}

			@Example
			@Label("fails if it has GenerationMode.EXHAUSTIVE")
			void failIfItHasGenerationModeExhaustive() {
				CheckedProperty checkedProperty = new CheckedProperty(
					"dataDrivenProperty", params -> true, getParametersForMethod("dataDrivenProperty"),
					p -> Collections.emptySet(),
					Optional.of(Table.of(Tuple.of(1, "1"))),
					aConfig().withGeneration(EXHAUSTIVE).build()
				);

				assertThatThrownBy(() -> checkedProperty.check(NULL_PUBLISHER, new Reporting[0])).isInstanceOf(JqwikException.class);
			}

			@Example
			@Label("fails if optional data is empty")
			void failIfItOptionalDataIsEmpty() {
				CheckedProperty checkedProperty = new CheckedProperty(
					"dataDrivenProperty", params -> true, getParametersForMethod("dataDrivenProperty"),
					p -> Collections.emptySet(),
					Optional.empty(),
					aConfig().withGeneration(DATA_DRIVEN).build()
				);

				assertThatThrownBy(() -> checkedProperty.check(NULL_PUBLISHER, new Reporting[0])).isInstanceOf(JqwikException.class);
			}
		}

		@Group
		class ExhaustiveProperty {

			@Example
			@Label("works with GenerationMode.EXHAUSTIVE")
			void runWithGenerationModeExhaustive() {
				List<Tuple.Tuple1> allGeneratedParameters = new ArrayList<>();
				CheckedFunction rememberParameters = params -> allGeneratedParameters.add(Tuple.of(params.get(0)));
				CheckedProperty checkedProperty = new CheckedProperty(
					"exhaustiveProperty", rememberParameters, getParametersForMethod("exhaustiveProperty"),
					p -> Collections.singleton(Arbitraries.integers().between(1, 3)),
					Optional.empty(),
					aConfig().withGeneration(EXHAUSTIVE).build()
				);

				PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
				assertThat(check.generation()).isEqualTo(GenerationMode.EXHAUSTIVE);
				assertThat(check.countTries()).isEqualTo(3);
				assertThat(check.status()).isEqualTo(SATISFIED);
				assertThat(allGeneratedParameters).containsExactly(Tuple.of(1), Tuple.of(2), Tuple.of(3));
			}

			@Example
			@Label("with explicit GenerationMode.EXHAUSTIVE number of tries is set to countMax")
			void withExplicitGenerationModeExhaustive() {
				CheckedProperty checkedProperty = new CheckedProperty(
					"exhaustiveProperty", params -> true, getParametersForMethod("exhaustiveProperty"),
					p -> Collections.singleton(Arbitraries.integers().between(1, 99)),
					Optional.empty(),
					aConfig().withTries(50).withGeneration(EXHAUSTIVE).build()
				);

				PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
				assertThat(check.generation()).isEqualTo(GenerationMode.EXHAUSTIVE);
				assertThat(check.countTries()).isEqualTo(99);
				assertThat(check.status()).isEqualTo(SATISFIED);
			}

			@Example
			@Label("works with GenerationMode.AUTO")
			void runWithGenerationModeAuto() {
				List<Tuple.Tuple1> allGeneratedParameters = new ArrayList<>();
				CheckedFunction rememberParameters = params -> allGeneratedParameters.add(Tuple.of(params.get(0)));
				CheckedProperty checkedProperty = new CheckedProperty(
					"exhaustiveProperty", rememberParameters, getParametersForMethod("exhaustiveProperty"),
					p -> Collections.singleton(Arbitraries.integers().between(1, 3)),
					Optional.empty(),
					aConfig().withGeneration(AUTO).build()
				);

				PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
				assertThat(check.generation()).isEqualTo(GenerationMode.EXHAUSTIVE);
				assertThat(check.countTries()).isEqualTo(3);
				assertThat(check.status()).isEqualTo(SATISFIED);
				assertThat(allGeneratedParameters).containsExactly(Tuple.of(1), Tuple.of(2), Tuple.of(3));
			}

			@Example
			@Label("fails if it no exhaustive generators are provided")
			void failIfNoExhaustiveGeneratorsAreProvided() {
				CheckedProperty checkedProperty = new CheckedProperty(
					"exhaustiveProperty", params -> true, getParametersForMethod("exhaustiveProperty"),
					p -> Collections.singleton(Arbitraries.integers()),
					Optional.empty(),
					aConfig().withGeneration(EXHAUSTIVE).build()
				);

				assertThatThrownBy(() -> checkedProperty.check(NULL_PUBLISHER, new Reporting[0])).isInstanceOf(JqwikException.class);
			}

			@Example
			@Label("use randomized generation if countMax is larger than configured tries")
			void useRandomizedGenerationIfCountMaxIsAboveTries() {
				CheckedProperty checkedProperty = new CheckedProperty(
					"exhaustiveProperty", params -> true, getParametersForMethod("exhaustiveProperty"),
					p -> Collections.singleton(Arbitraries.integers().between(1, 21)),
					Optional.empty(),
					aConfig().withTries(20).build()
				);

				PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
				assertThat(check.generation()).isEqualTo(GenerationMode.RANDOMIZED);
				assertThat(check.countTries()).isEqualTo(20);
				assertThat(check.status()).isEqualTo(SATISFIED);
			}

			@Example
			@Label("use randomized generation with explicit GenerationMode.RANDOMIZED")
			void useRandomizedWithExplicitGenerationModeRandomized() {
				CheckedProperty checkedProperty = new CheckedProperty(
					"exhaustiveProperty", params -> true, getParametersForMethod("exhaustiveProperty"),
					p -> Collections.singleton(Arbitraries.integers().between(1, 3)),
					Optional.empty(),
					aConfig().withTries(20).withGeneration(RANDOMIZED).build()
				);

				PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
				assertThat(check.generation()).isEqualTo(GenerationMode.RANDOMIZED);
				assertThat(check.countTries()).isEqualTo(20);
				assertThat(check.status()).isEqualTo(SATISFIED);
			}

		}
	}

	private void intOnlyExample(String methodName, CheckedFunction forAllFunction, PropertyCheckResult.Status expectedStatus) {
		CheckedProperty checkedProperty = new CheckedProperty(
			methodName, forAllFunction, getParametersForMethod(methodName),
			p -> Collections.singleton(new GenericArbitrary(Arbitraries.integers().between(-50, 50))),
			Optional.empty(),
			aConfig().build()
		);
		PropertyCheckResult check = checkedProperty.check(NULL_PUBLISHER, new Reporting[0]);
		assertThat(check.status()).isEqualTo(expectedStatus);
	}

	private List<MethodParameter> getParametersForMethod(String methodName) {
		return getParametersFor(CheckingExamples.class, methodName);
	}

	private static class CheckingExamples {

		@Property
		public boolean propertyWithoutParameters(@ForAll int anyNumber) {
			return true;
		}

		@Property(stereotype = "OtherStereotype", tries = 42, maxDiscardRatio = 2, shrinking = ShrinkingMode.OFF)
		public boolean propertyWith42TriesAndMaxDiscardRatio2(@ForAll int anyNumber) {
			return true;
		}

		public boolean stringProp(@ForAll String aString) {
			return true;
		}

		public boolean prop0() {
			return true;
		}

		public boolean prop1(@ForAll int n1) {
			return true;
		}

		public boolean prop2(@ForAll int n1, @ForAll int n2) {
			return true;
		}

		public boolean prop8(
			@ForAll int n1, @ForAll int n2, @ForAll int n3, @ForAll int n4, @ForAll int n5, @ForAll int n6, @ForAll int n7,
			@ForAll int n8
		) {
			return true;
		}

		@FromData("fizzBuzzSamples")
		public boolean dataDrivenProperty(@ForAll int index, @ForAll String fizzBuzz) {
			return true;
		}

		public boolean exhaustiveProperty(@ForAll @IntRange(min = 1, max = 3) int anInt) {
			return true;
		}

	}
}
