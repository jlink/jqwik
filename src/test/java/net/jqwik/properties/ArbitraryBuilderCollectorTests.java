package net.jqwik.properties;

import net.jqwik.*;
import net.jqwik.api.*;
import net.jqwik.api.providers.*;
import net.jqwik.descriptor.*;
import net.jqwik.properties.arbitraries.*;
import net.jqwik.support.*;
import org.assertj.core.api.*;

import java.util.*;
import java.util.function.*;

import static net.jqwik.TestDescriptorBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

@Group
class ArbitraryBuilderCollectorTests {

	private static class Thing {

	}

	@Group
	class RegisteredDefaultProviders {

		@Example
		void singleSimpleParameter() {
			ArbitraryBuilderCollector collector = createCollector(DefaultParams.class);
			List<MethodParameter> parameters = getParameters(DefaultParams.class, "intParam");

			collector.collect(parameters.get(0));

			Map<TypeUsage, List<ArbitraryBuilder>> builders = collector.collectedBuilders();
			assertThat(builders).hasSize(1);

			List<ArbitraryBuilder> arbitraryBuilders = builders.get(TypeUsage.forParameter(parameters.get(0)));
			assertThat(arbitraryBuilders).hasSize(1);
			ArbitraryBuilder builder = arbitraryBuilders.get(0);
			assertThat(builder.build(new DefaultParams(), new HashMap<>())).isInstanceOf(DefaultIntegerArbitrary.class);
		}

//		@Example
//		void defaultProvidersAreUsedIfNothingIsProvided() {
//			PropertyMethodArbitraryResolver provider = getResolver(DefaultParams.class, "intParam", int.class);
//			MethodParameter parameter = getParameter(DefaultParams.class, "intParam");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(Integer.class);
//		}
//
//		@Example
//		void doNotUseDefaultIfForAllHasValue() {
//			PropertyMethodArbitraryResolver resolver = getResolver(DefaultParams.class, "intParamWithForAllValue", int.class);
//			MethodParameter parameter = getParameter(DefaultParams.class, "intParamWithForAllValue");
//			assertThat(resolver.forParameter(parameter)).isEmpty();
//		}
//
//		@Example
//		void useNextDefaultProviderIfFirstDoesNotProvideAnArbitrary() {
//			PropertyMethodDescriptor descriptor = getDescriptor(DefaultParams.class, "aString", String.class);
//			List<ArbitraryProvider> defaultProviders = Arrays.asList(createProvider(String.class, null),
//					createProvider(String.class, (Arbitrary<String>) tries -> random -> Shrinkable.unshrinkable("an arbitrary string")));
//			PropertyMethodArbitraryResolver resolver = new PropertyMethodArbitraryResolver(descriptor, new DefaultParams(),
//																						   new RegisteredArbitraryResolver(defaultProviders), Collections
//																							   .emptyList()
//			);
//			MethodParameter parameter = getParameter(DefaultParams.class, "aString");
//			Object actual = generateFirst(resolver, parameter);
//			assertThat(actual).isEqualTo("an arbitrary string");
//		}

		private ArbitraryProvider createProvider(Class targetClass, Arbitrary<?> arbitrary) {
			return new ArbitraryProvider() {
				@Override
				public boolean canProvideFor(TypeUsage targetType) {
					return targetType.isAssignableFrom(targetClass);
				}

				@Override
				public Arbitrary<?> provideFor(TypeUsage targetType, Function<TypeUsage, Optional<Arbitrary<?>>> subtypeProvider) {
					return arbitrary;
				}
			};
		}

	}

	private static class DefaultParams {
		@Property
		boolean intParam(@ForAll int aValue) {
			return true;
		}

		@Property
		boolean intParamWithForAllValue(@ForAll("someInt") int aValue) {
			return true;
		}

		@Property
		boolean aString(@ForAll String aString) {
			return true;
		}

	}


	@Group
	class ProvidedArbitraries {

//		@Example
//		void unnamedStringGenerator() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithUnnamedGenerator.class, "string", String.class);
//			MethodParameter parameter = getParameter(WithUnnamedGenerator.class, "string");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(String.class);
//		}
//
//		private class WithUnnamedGenerator {
//			@Property
//			boolean string(@ForAll String aString) {
//				return true;
//			}
//
//			@Provide
//			Arbitrary<String> aString() {
//				return Arbitraries.strings().withCharRange('a', 'z');
//			}
//		}
//
//		@Example
//		void findBoxedTypeGenerator() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.class, "longFromBoxedType", long.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.class, "longFromBoxedType");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(Long.class);
//		}
//
//		@Example
//		void findStringGeneratorByName() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.class, "string", String.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.class, "string");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(String.class);
//		}
//
//		@Example
//		void findStringGeneratorByMethodName() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.class, "stringByMethodName", String.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.class, "stringByMethodName");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(String.class);
//		}
//
//		@Example
//		void findGeneratorByMethodNameOutsideGroup() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.NestedWithNamedProviders.class,
//					"nestedStringByMethodName", String.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.NestedWithNamedProviders.class, "nestedStringByMethodName");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(String.class);
//		}
//
//		@Example
//		void findGeneratorByNameOutsideGroup() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.NestedWithNamedProviders.class, "nestedString",
//					String.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.NestedWithNamedProviders.class, "nestedString");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(String.class);
//		}
//
//		@Example
//		void findFirstFitIfNoNameIsGivenInOutsideGroup() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.NestedWithNamedProviders.class, "nestedThing",
//					Thing.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.NestedWithNamedProviders.class, "nestedThing");
//			Object actual = generateFirst(provider, parameter);
//			assertThat(actual).isInstanceOf(Thing.class);
//		}
//
//		@Example
//		void namedStringGeneratorNotFound() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.class, "otherString", String.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.class, "otherString");
//			assertThat(provider.forParameter(parameter)).isEmpty();
//		}
//
//		@Example
//		void findFirstFitIfNoNameIsGiven() {
//			PropertyMethodArbitraryResolver provider = getResolver(WithNamedProviders.class, "listOfThingWithoutName", List.class);
//			MethodParameter parameter = getParameter(WithNamedProviders.class, "listOfThingWithoutName");
//			List actualList = generateCollection(provider, parameter);
//			assertThat(actualList.get(0)).isInstanceOf(Thing.class);
//		}

		private class WithNamedProviders {
			@Property
			boolean string(@ForAll("aString") String aString) {
				return true;
			}

			@Provide("aString")
			Arbitrary<String> aString() {
				return Arbitraries.strings().withCharRange('a', 'z');
			}

			@Property
			boolean otherString(@ForAll("otherString") String aString) {
				return true;
			}

			@Property
			boolean stringByMethodName(@ForAll("byMethodName") String aString) {
				return true;
			}

			@Provide
			Arbitrary<String> byMethodName() {
				return Arbitraries.strings().withCharRange('x', 'y');
			}

			@Property
			boolean longFromBoxedType(@ForAll("longBetween1and10") long aLong) {
				return true;
			}

			@Provide
			Arbitrary<Long> longBetween1and10() {
				return Arbitraries.longs().between(1L, 10L);
			}

			@Property
			boolean listOfThingWithoutName(@ForAll List<Thing> thingList) {
				return true;
			}

			@Provide()
			Arbitrary<Thing> aThing() {
				return Arbitraries.of(new Thing());
			}

			@Group
			class NestedWithNamedProviders {
				@Property
				boolean nestedStringByMethodName(@ForAll("byMethodName") String aString) {
					return true;
				}

				@Property
				boolean nestedString(@ForAll("aString") String aString) {
					return true;
				}

				@Property
				boolean nestedThing(@ForAll Thing aThing) {
					return true;
				}

			}
		}

	}

	private static ArbitraryBuilderCollector createCollector(Class<?> container) {
		return new ArbitraryBuilderCollector(container);
	}

	private static List<MethodParameter> getParameters(Class container, String methodName) {
		return TestHelper.getParametersFor(container, methodName);
	}

}
