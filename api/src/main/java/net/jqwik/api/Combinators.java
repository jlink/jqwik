package net.jqwik.api;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.apiguardian.api.*;

import static org.apiguardian.api.API.Status.*;

@API(status = MAINTAINED, since = "1.0")
public class Combinators {

	@API(status = INTERNAL)
	public static abstract class CombinatorsFacade {
		private static CombinatorsFacade implementation;

		static {
			implementation = FacadeLoader.load(CombinatorsFacade.class);
		}

		public abstract <R> Shrinkable<R> combineShrinkables(
			List<Shrinkable<Object>> shrinkables,
			Function<List<Object>, R> combineFunction
		);

		public abstract <R> Optional<ExhaustiveGenerator<R>> combineExhaustive(
			List<Arbitrary<Object>> arbitraries,
			Function<List<Object>, R> combineFunction,
			long maxNumberOfSamples
		);
	}

	private Combinators() {
	}

	/**
	 * Combine 2 arbitraries into one.
	 *
	 * @return Combinator2 instance which can be evaluated using {@linkplain Combinator2#as}
	 */
	public static <T1, T2> Combinator2<T1, T2> combine(Arbitrary<T1> a1, Arbitrary<T2> a2) {
		return new Combinator2<>(a1, a2);
	}

	/**
	 * Combine 3 arbitraries into one.
	 *
	 * @return Combinator3 instance which can be evaluated using {@linkplain Combinator3#as}
	 */
	public static <T1, T2, T3> Combinator3<T1, T2, T3> combine(Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3) {
		return new Combinator3<>(a1, a2, a3);
	}

	/**
	 * Combine 4 arbitraries into one.
	 *
	 * @return Combinator4 instance which can be evaluated using {@linkplain Combinator4#as}
	 */
	public static <T1, T2, T3, T4> Combinator4<T1, T2, T3, T4> combine(
		Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3,
		Arbitrary<T4> a4
	) {
		return new Combinator4<>(a1, a2, a3, a4);
	}

	/**
	 * Combine 5 arbitraries into one.
	 *
	 * @return Combinator5 instance which can be evaluated using {@linkplain Combinator5#as}
	 */
	public static <T1, T2, T3, T4, T5> Combinator5<T1, T2, T3, T4, T5> combine(
		Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3,
		Arbitrary<T4> a4, Arbitrary<T5> a5
	) {
		return new Combinator5<>(a1, a2, a3, a4, a5);
	}

	/**
	 * Combine 6 arbitraries into one.
	 *
	 * @return Combinator6 instance which can be evaluated using {@linkplain Combinator6#as}
	 */
	public static <T1, T2, T3, T4, T5, T6> Combinator6<T1, T2, T3, T4, T5, T6> combine(
		Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3,
		Arbitrary<T4> a4, Arbitrary<T5> a5, Arbitrary<T6> a6
	) {
		return new Combinator6<>(a1, a2, a3, a4, a5, a6);
	}

	/**
	 * Combine 7 arbitraries into one.
	 *
	 * @return Combinator7 instance which can be evaluated using {@linkplain Combinator7#as}
	 */
	public static <T1, T2, T3, T4, T5, T6, T7> Combinator7<T1, T2, T3, T4, T5, T6, T7> combine(
		Arbitrary<T1> a1, Arbitrary<T2> a2,
		Arbitrary<T3> a3, Arbitrary<T4> a4, Arbitrary<T5> a5, Arbitrary<T6> a6, Arbitrary<T7> a7
	) {
		return new Combinator7<>(a1, a2, a3, a4, a5, a6, a7);
	}

	/**
	 * Combine 8 arbitraries into one.
	 *
	 * @return Combinator8 instance which can be evaluated using {@linkplain Combinator8#as}
	 */
	public static <T1, T2, T3, T4, T5, T6, T7, T8> Combinator8<T1, T2, T3, T4, T5, T6, T7, T8> combine(
		Arbitrary<T1> a1, Arbitrary<T2> a2,
		Arbitrary<T3> a3, Arbitrary<T4> a4, Arbitrary<T5> a5, Arbitrary<T6> a6, Arbitrary<T7> a7, Arbitrary<T8> a8
	) {
		return new Combinator8<>(a1, a2, a3, a4, a5, a6, a7, a8);
	}

	/**
	 * Combine a list of arbitraries into one.
	 *
	 * @return ListCombinator instance which can be evaluated using {@linkplain ListCombinator#as}
	 */
	public static <T> ListCombinator<T> combine(List<Arbitrary<T>> listOfArbitraries) {
		return new ListCombinator<>(listOfArbitraries);
	}

	/**
	 * Combine Arbitraries by means of a builder.
	 *
	 * @param builderSupplier The supplier will be called freshly for each value generation.
	 *                        For exhaustive generation all supplied objects are
	 *                        supposed to be identical.
	 *
	 * @return BuilderCombinator instance
	 */
	@API(status = MAINTAINED, since = "1.2.0")
	public static <B> BuilderCombinator<B> withBuilder(Supplier<B> builderSupplier) {
		return new BuilderCombinator<>(Arbitraries.create(builderSupplier));
	}

	/**
	 * Combine Arbitraries by means of a builder.
	 *
	 * @param builderArbitrary The arbitrary is used to generate a builder object
	 *                         as starting point for building on each value generation.
	 * @return BuilderCombinator instance
	 */
	@API(status = MAINTAINED, since = "1.2.0")
	public static <B> BuilderCombinator<B> withBuilder(Arbitrary<B> builderArbitrary) {
		return new BuilderCombinator<>(builderArbitrary);
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2, R> Function<List<Object>, R> combineFunction(F2<T1, T2, R> combinator2) {
		return params -> combinator2
			.apply((T1) params.get(0), (T2) params.get(1));
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2, T3, R> Function<List<Object>, R> combineFunction(F3<T1, T2, T3, R> combinator3) {
		return params -> combinator3
			.apply((T1) params.get(0), (T2) params.get(1), (T3) params.get(2));
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2, T3, T4, R> Function<List<Object>, R> combineFunction(F4<T1, T2, T3, T4, R> combinator4) {
		return params -> combinator4
			.apply(
				(T1) params.get(0), (T2) params.get(1),
				(T3) params.get(2), (T4) params.get(3)
			);
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2, T3, T4, T5, R> Function<List<Object>, R> combineFunction(F5<T1, T2, T3, T4, T5, R> combinator5) {
		return params -> combinator5
			.apply(
				(T1) params.get(0), (T2) params.get(1),
				(T3) params.get(2), (T4) params.get(3),
				(T5) params.get(4)
			);
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2, T3, T4, T5, T6, R> Function<List<Object>, R> combineFunction(F6<T1, T2, T3, T4, T5, T6, R> combinator6) {
		return params -> combinator6
			.apply(
				(T1) params.get(0), (T2) params.get(1),
				(T3) params.get(2), (T4) params.get(3),
				(T5) params.get(4), (T6) params.get(5)
			);
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2, T3, T4, T5, T6, T7, R> Function<List<Object>, R> combineFunction(F7<T1, T2, T3, T4, T5, T6, T7, R> combinator7) {
		return params -> combinator7
			.apply(
				(T1) params.get(0), (T2) params.get(1),
				(T3) params.get(2), (T4) params.get(3),
				(T5) params.get(4), (T6) params.get(5),
				(T7) params.get(6)
			);
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function<List<Object>, R> combineFunction(F8<T1, T2, T3, T4, T5, T6, T7, T8, R> combinator8) {
		return params -> combinator8
			.apply(
				(T1) params.get(0), (T2) params.get(1),
				(T3) params.get(2), (T4) params.get(3),
				(T5) params.get(4), (T6) params.get(5),
				(T7) params.get(6), (T8) params.get(7)
			);
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> asTypedList(Object... objects) {
		List<T> list = new ArrayList<>();
		for (Object object : objects) {
			list.add((T) object);
		}
		return list;
	}

	public static class Combinator2<T1, T2> {
		private final Arbitrary<T1> a1;
		private final Arbitrary<T2> a2;

		private Combinator2(Arbitrary<T1> a1, Arbitrary<T2> a2) {
			this.a1 = a1;
			this.a2 = a2;
		}

		// This is a shorter implementation of as, which however would have worse shrinking
		// behaviour because it builds on flatMap:
		//		public <R> Arbitrary<R> as(F2<T1, T2, R> combinator) {
		//			return a1.flatMap(v1 -> a2.map(v2 -> combinator.apply(v1, v2)));
		//		}
		public <R> Arbitrary<R> as(F2<T1, T2, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					RandomGenerator<T1> g1 = a1.generator(genSize);
					RandomGenerator<T2> g2 = a2.generator(genSize);
					return random -> {
						List<Shrinkable<Object>> shrinkables = asTypedList(g1.next(random), g2.next(random));
						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction(combinator));
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(a1, a2),
						combineFunction(combinator),
						maxNumberOfSamples
					);
				}

				@Override
				public EdgeCases<R> edgeCases() {
					return a1.edgeCases().flatMap(
						v1 -> a2.edgeCases().map(
							v2 -> combinator.apply(v1, v2)));
				}
			};
		}

		public <R> Arbitrary<R> flatAs(F2<T1, T2, Arbitrary<R>> flatCombinator) {
			return a1.flatMap(v1 -> a2.flatMap(v2 -> flatCombinator.apply(v1, v2)));
		}
	}

	public static class Combinator3<T1, T2, T3> {
		private final Arbitrary<T1> a1;
		private final Arbitrary<T2> a2;
		private final Arbitrary<T3> a3;

		private Combinator3(Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
		}

		public <R> Arbitrary<R> as(F3<T1, T2, T3, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					RandomGenerator<T1> g1 = a1.generator(genSize);
					RandomGenerator<T2> g2 = a2.generator(genSize);
					RandomGenerator<T3> g3 = a3.generator(genSize);
					return random -> {
						List<Shrinkable<Object>> shrinkables = asTypedList(
							g1.next(random),
							g2.next(random),
							g3.next(random)
						);
						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction(combinator));
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(a1, a2, a3),
						combineFunction(combinator),
						maxNumberOfSamples
					);
				}

				@Override
				public EdgeCases<R> edgeCases() {
					return a1.edgeCases().flatMap(
						v1 -> a2.edgeCases().flatMap(
							v2 -> a3.edgeCases().map(
								v3 -> combinator.apply(v1, v2, v3))));
				}

			};
		}

		public <R> Arbitrary<R> flatAs(F3<T1, T2, T3, Arbitrary<R>> flatCombinator) {
			return a1.flatMap(
				v1 -> a2.flatMap(
					v2 -> a3.flatMap(
						v3 -> flatCombinator.apply(v1, v2, v3))));
		}

	}

	public static class Combinator4<T1, T2, T3, T4> {
		private final Arbitrary<T1> a1;
		private final Arbitrary<T2> a2;
		private final Arbitrary<T3> a3;
		private final Arbitrary<T4> a4;

		private Combinator4(Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3, Arbitrary<T4> a4) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.a4 = a4;
		}

		@SuppressWarnings("unchecked")
		public <R> Arbitrary<R> as(F4<T1, T2, T3, T4, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					RandomGenerator<T1> g1 = a1.generator(genSize);
					RandomGenerator<T2> g2 = a2.generator(genSize);
					RandomGenerator<T3> g3 = a3.generator(genSize);
					RandomGenerator<T4> g4 = a4.generator(genSize);

					return random -> {
						List<Shrinkable<Object>> shrinkables = asTypedList(
							g1.next(random),
							g2.next(random),
							g3.next(random),
							g4.next(random)
						);
						Function<List<Object>, R> combineFunction = params ->
							combinator.apply(
								(T1) params.get(0), (T2) params.get(1),
								(T3) params.get(2), (T4) params.get(3)
							);

						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction);
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(a1, a2, a3, a4),
						combineFunction(combinator),
						maxNumberOfSamples
					);
				}

				@Override
				public EdgeCases<R> edgeCases() {
					return a1.edgeCases().flatMap(
						v1 -> a2.edgeCases().flatMap(
							v2 -> a3.edgeCases().flatMap(
								v3 -> a4.edgeCases().map(
									v4-> combinator.apply(v1, v2, v3, v4)))));
				}

			};
		}

		public <R> Arbitrary<R> flatAs(F4<T1, T2, T3, T4, Arbitrary<R>> flatCombinator) {
			return a1.flatMap(
				v1 -> a2.flatMap(
					v2 -> a3.flatMap(
						v3 -> a4.flatMap(
							v4 -> flatCombinator.apply(v1, v2, v3, v4)))));
		}

	}

	public static class Combinator5<T1, T2, T3, T4, T5> {
		private final Arbitrary<T1> a1;
		private final Arbitrary<T2> a2;
		private final Arbitrary<T3> a3;
		private final Arbitrary<T4> a4;
		private final Arbitrary<T5> a5;

		private Combinator5(Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3, Arbitrary<T4> a4, Arbitrary<T5> a5) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.a4 = a4;
			this.a5 = a5;
		}

		@SuppressWarnings("unchecked")
		public <R> Arbitrary<R> as(F5<T1, T2, T3, T4, T5, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					RandomGenerator<T1> g1 = a1.generator(genSize);
					RandomGenerator<T2> g2 = a2.generator(genSize);
					RandomGenerator<T3> g3 = a3.generator(genSize);
					RandomGenerator<T4> g4 = a4.generator(genSize);
					RandomGenerator<T5> g5 = a5.generator(genSize);

					return random -> {
						List<Shrinkable<Object>> shrinkables = asTypedList(
							g1.next(random),
							g2.next(random),
							g3.next(random),
							g4.next(random),
							g5.next(random)
						);
						Function<List<Object>, R> combineFunction =
							params ->
								combinator.apply(
									(T1) params.get(0), (T2) params.get(1),
									(T3) params.get(2), (T4) params.get(3),
									(T5) params.get(4)
								);

						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction);
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(a1, a2, a3, a4, a5),
						combineFunction(combinator),
						maxNumberOfSamples
					);
				}

				@Override
				public EdgeCases<R> edgeCases() {
					return a1.edgeCases().flatMap(
						v1 -> a2.edgeCases().flatMap(
							v2 -> a3.edgeCases().flatMap(
								v3 -> a4.edgeCases().flatMap(
									v4-> a5.edgeCases().map(
										v5 -> combinator.apply(v1, v2, v3, v4, v5))))));
				}

			};
		}

		public <R> Arbitrary<R> flatAs(F5<T1, T2, T3, T4, T5, Arbitrary<R>> flatCombinator) {
			return a1.flatMap(
				v1 -> a2.flatMap(
					v2 -> a3.flatMap(
						v3 -> a4.flatMap(
							v4 -> a5.flatMap(
								v5 -> flatCombinator.apply(v1, v2, v3, v4, v5))))));
		}

	}

	public static class Combinator6<T1, T2, T3, T4, T5, T6> {
		private final Arbitrary<T1> a1;
		private final Arbitrary<T2> a2;
		private final Arbitrary<T3> a3;
		private final Arbitrary<T4> a4;
		private final Arbitrary<T5> a5;
		private final Arbitrary<T6> a6;

		private Combinator6(Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3, Arbitrary<T4> a4, Arbitrary<T5> a5, Arbitrary<T6> a6) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.a4 = a4;
			this.a5 = a5;
			this.a6 = a6;
		}

		@SuppressWarnings("unchecked")
		public <R> Arbitrary<R> as(F6<T1, T2, T3, T4, T5, T6, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					RandomGenerator<T1> g1 = a1.generator(genSize);
					RandomGenerator<T2> g2 = a2.generator(genSize);
					RandomGenerator<T3> g3 = a3.generator(genSize);
					RandomGenerator<T4> g4 = a4.generator(genSize);
					RandomGenerator<T5> g5 = a5.generator(genSize);
					RandomGenerator<T6> g6 = a6.generator(genSize);

					return random -> {
						List<Shrinkable<Object>> shrinkables = asTypedList(
							g1.next(random),
							g2.next(random),
							g3.next(random),
							g4.next(random),
							g5.next(random),
							g6.next(random)
						);
						Function<List<Object>, R> combineFunction =
							params ->
								combinator.apply(
									(T1) params.get(0), (T2) params.get(1),
									(T3) params.get(2), (T4) params.get(3),
									(T5) params.get(4), (T6) params.get(5)
								);

						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction);
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(a1, a2, a3, a4, a5, a6),
						combineFunction(combinator),
						maxNumberOfSamples
					);
				}

				@Override
				public EdgeCases<R> edgeCases() {
					return a1.edgeCases().flatMap(
						v1 -> a2.edgeCases().flatMap(
							v2 -> a3.edgeCases().flatMap(
								v3 -> a4.edgeCases().flatMap(
									v4-> a5.edgeCases().flatMap(
										v5 -> a6.edgeCases().map(
											v6 -> combinator.apply(v1, v2, v3, v4, v5, v6)))))));
				}

			};
		}

		public <R> Arbitrary<R> flatAs(F6<T1, T2, T3, T4, T5, T6, Arbitrary<R>> flatCombinator) {
			return a1.flatMap(
				v1 -> a2.flatMap(
					v2 -> a3.flatMap(
						v3 -> a4.flatMap(
							v4 -> a5.flatMap(
								v5 -> a6.flatMap(
									v6 -> flatCombinator.apply(v1, v2, v3, v4, v5, v6)))))));
		}

	}

	public static class Combinator7<T1, T2, T3, T4, T5, T6, T7> {
		private final Arbitrary<T1> a1;
		private final Arbitrary<T2> a2;
		private final Arbitrary<T3> a3;
		private final Arbitrary<T4> a4;
		private final Arbitrary<T5> a5;
		private final Arbitrary<T6> a6;
		private final Arbitrary<T7> a7;

		private Combinator7(
			Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3, Arbitrary<T4> a4, Arbitrary<T5> a5, Arbitrary<T6> a6,
			Arbitrary<T7> a7
		) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.a4 = a4;
			this.a5 = a5;
			this.a6 = a6;
			this.a7 = a7;
		}

		@SuppressWarnings("unchecked")
		public <R> Arbitrary<R> as(F7<T1, T2, T3, T4, T5, T6, T7, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					RandomGenerator<T1> g1 = a1.generator(genSize);
					RandomGenerator<T2> g2 = a2.generator(genSize);
					RandomGenerator<T3> g3 = a3.generator(genSize);
					RandomGenerator<T4> g4 = a4.generator(genSize);
					RandomGenerator<T5> g5 = a5.generator(genSize);
					RandomGenerator<T6> g6 = a6.generator(genSize);
					RandomGenerator<T7> g7 = a7.generator(genSize);

					return random -> {
						List<Shrinkable<Object>> shrinkables = asTypedList(
							g1.next(random),
							g2.next(random),
							g3.next(random),
							g4.next(random),
							g5.next(random),
							g6.next(random),
							g7.next(random)
						);
						Function<List<Object>, R> combineFunction =
							params ->
								combinator.apply(
									(T1) params.get(0), (T2) params.get(1),
									(T3) params.get(2), (T4) params.get(3),
									(T5) params.get(4), (T6) params.get(5),
									(T7) params.get(6)
								);
						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction);
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(a1, a2, a3, a4, a5, a6, a7),
						combineFunction(combinator),
						maxNumberOfSamples
					);
				}
			};
		}

		public <R> Arbitrary<R> flatAs(F7<T1, T2, T3, T4, T5, T6, T7, Arbitrary<R>> flatCombinator) {
			return a1.flatMap(
				v1 -> a2.flatMap(
					v2 -> a3.flatMap(
						v3 -> a4.flatMap(
							v4 -> a5.flatMap(
								v5 -> a6.flatMap(
									v6 -> a7.flatMap(
										v7 -> flatCombinator.apply(v1, v2, v3, v4, v5, v6, v7))))))));
		}

	}

	public static class Combinator8<T1, T2, T3, T4, T5, T6, T7, T8> {
		private final Arbitrary<T1> a1;
		private final Arbitrary<T2> a2;
		private final Arbitrary<T3> a3;
		private final Arbitrary<T4> a4;
		private final Arbitrary<T5> a5;
		private final Arbitrary<T6> a6;
		private final Arbitrary<T7> a7;
		private final Arbitrary<T8> a8;

		private Combinator8(
			Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3, Arbitrary<T4> a4, Arbitrary<T5> a5, Arbitrary<T6> a6,
			Arbitrary<T7> a7, Arbitrary<T8> a8
		) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.a4 = a4;
			this.a5 = a5;
			this.a6 = a6;
			this.a7 = a7;
			this.a8 = a8;
		}

		@SuppressWarnings("unchecked")
		public <R> Arbitrary<R> as(F8<T1, T2, T3, T4, T5, T6, T7, T8, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					RandomGenerator<T1> g1 = a1.generator(genSize);
					RandomGenerator<T2> g2 = a2.generator(genSize);
					RandomGenerator<T3> g3 = a3.generator(genSize);
					RandomGenerator<T4> g4 = a4.generator(genSize);
					RandomGenerator<T5> g5 = a5.generator(genSize);
					RandomGenerator<T6> g6 = a6.generator(genSize);
					RandomGenerator<T7> g7 = a7.generator(genSize);
					RandomGenerator<T8> g8 = a8.generator(genSize);

					return random -> {
						List<Shrinkable<Object>> shrinkables = asTypedList(
							g1.next(random),
							g2.next(random),
							g3.next(random),
							g4.next(random),
							g5.next(random),
							g6.next(random),
							g7.next(random),
							g8.next(random)
						);
						Function<List<Object>, R> combineFunction =
							params ->
								combinator.apply(
									(T1) params.get(0), (T2) params.get(1),
									(T3) params.get(2), (T4) params.get(3),
									(T5) params.get(4), (T6) params.get(5),
									(T7) params.get(6), (T8) params.get(7)
								);

						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction);
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(a1, a2, a3, a4, a5, a6, a7, a8),
						combineFunction(combinator),
						maxNumberOfSamples
					);
				}
			};
		}

		public <R> Arbitrary<R> flatAs(F8<T1, T2, T3, T4, T5, T6, T7, T8, Arbitrary<R>> flatCombinator) {
			return a1.flatMap(
				v1 -> a2.flatMap(
					v2 -> a3.flatMap(
						v3 -> a4.flatMap(
							v4 -> a5.flatMap(
								v5 -> a6.flatMap(
									v6 -> a7.flatMap(
										v7 -> a8.flatMap(
											v8 -> flatCombinator.apply(v1, v2, v3, v4, v5, v6, v7, v8)))))))));
		}
	}

	public static class ListCombinator<T> {
		private final List<Arbitrary<T>> listOfArbitraries;

		private ListCombinator(List<Arbitrary<T>> listOfArbitraries) {
			this.listOfArbitraries = listOfArbitraries;
		}

		@SuppressWarnings("unchecked")
		public <R> Arbitrary<R> as(Function<List<T>, R> combinator) {
			return new Arbitrary<R>() {
				@Override
				public RandomGenerator<R> generator(int genSize) {
					List<RandomGenerator<T>> listOfGenerators =
						listOfArbitraries
							.stream()
							.map(a -> a.generator(genSize))
							.collect(Collectors.toList());

					return random -> {
						List<Shrinkable<Object>> shrinkables =
							listOfGenerators
								.stream()
								.map(g -> g.next(random))
								.map(s -> (Shrinkable<Object>) s)
								.collect(Collectors.toList());

						Function<List<Object>, R> combineFunction = params -> combinator.apply((List<T>) params);

						return CombinatorsFacade.implementation.combineShrinkables(shrinkables, combineFunction);
					};
				}

				@Override
				public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
					Function<List<Object>, R> combinedFunction = params -> combinator.apply((List<T>) params);
					return CombinatorsFacade.implementation.combineExhaustive(
						asTypedList(listOfArbitraries.toArray()),
						combinedFunction,
						maxNumberOfSamples
					);
				}
			};
		}

		public <R> Arbitrary<R> flatAs(Function<List<T>, Arbitrary<R>> flatCombinator) {
			return combineFlat(new ArrayList<>(listOfArbitraries), new ArrayList<>(), flatCombinator);
		}

		// Caution: This method is NOT tail-recursive.
		// No easy conversion to iteration possible.
		// Stack overflow possible.
		private <R> Arbitrary<R> combineFlat(
			List<Arbitrary<T>> arbitraries,
			List<T> values,
			Function<List<T>, Arbitrary<R>> flatCombinator
		) {
			if (arbitraries.isEmpty())
				return flatCombinator.apply(values);
			Arbitrary<T> first = arbitraries.remove(0);
			return first.flatMap(value -> {
				values.add(0, value);
				return combineFlat(arbitraries, values, flatCombinator);
			});
		}
	}

	@API(status = MAINTAINED, since = "1.2.0")
	public static class BuilderCombinator<B> {
		private Arbitrary<B> builder;

		private BuilderCombinator(Arbitrary<B> delegate) {
			this.builder = delegate;
		}

		public <T> CombinableBuilder<B, T> use(Arbitrary<T> arbitrary) {
			return new CombinableBuilder<>(builder, arbitrary);
		}

		public <T> Arbitrary<T> build(Function<B, T> buildFunction) {
			return builder.map(buildFunction);
		}

	}

	@API(status = MAINTAINED, since = "1.2.0")
	public static class CombinableBuilder<B, T> {
		private final Arbitrary<B> builder;
		private final Arbitrary<T> arbitrary;

		private CombinableBuilder(Arbitrary<B> builder, Arbitrary<T> arbitrary) {
			this.builder = builder;
			this.arbitrary = arbitrary;
		}

		public <C> BuilderCombinator<C> in(Combinators.F2<B, T, C> toFunction) {
			Arbitrary<C> arbitraryOfC = arbitrary.flatMap(t -> builder.map(b -> toFunction.apply(b, t)));
			return new BuilderCombinator<>(arbitraryOfC);
		}
	}

	@FunctionalInterface
	@API(status = INTERNAL)
	public interface F2<T1, T2, R> {
		R apply(T1 t1, T2 t2);
	}

	@FunctionalInterface
	@API(status = INTERNAL)
	public interface F3<T1, T2, T3, R> {
		R apply(T1 t1, T2 t2, T3 t3);
	}

	@FunctionalInterface
	@API(status = INTERNAL)
	public interface F4<T1, T2, T3, T4, R> {
		R apply(T1 t1, T2 t2, T3 t3, T4 t4);
	}

	@FunctionalInterface
	@API(status = INTERNAL)
	public interface F5<T1, T2, T3, T4, T5, R> {
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
	}

	@FunctionalInterface
	@API(status = INTERNAL)
	public interface F6<T1, T2, T3, T4, T5, T6, R> {
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
	}

	@FunctionalInterface
	@API(status = INTERNAL)
	public interface F7<T1, T2, T3, T4, T5, T6, T7, R> {
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);
	}

	@FunctionalInterface
	@API(status = INTERNAL)
	public interface F8<T1, T2, T3, T4, T5, T6, T7, T8, R> {
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
	}

}
