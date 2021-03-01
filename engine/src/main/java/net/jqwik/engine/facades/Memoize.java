package net.jqwik.engine.facades;

import java.util.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;
import net.jqwik.api.lifecycle.*;

class Memoize {

	private static final Store<Map<Tuple3<Arbitrary<?>, Integer, Boolean>, RandomGenerator<?>>> generatorStore = createStore();

	private static Store<Map<Tuple3<Arbitrary<?>, Integer, Boolean>, RandomGenerator<?>>> createStore() {
		Store<Map<Tuple3<Arbitrary<?>, Integer, Boolean>, RandomGenerator<?>>> store = Store.create(Memoize.class, Lifespan.PROPERTY, HashMap::new);
		return store.onClose(Map::clear);
	}

	@SuppressWarnings("unchecked")
	static <U> RandomGenerator<U> memoizedGenerator(
			Arbitrary<U> arbitrary,
			int genSize,
			boolean withEdgeCases,
			Supplier<RandomGenerator<U>> generatorSupplier
	) {
		Tuple3<Arbitrary<?>, Integer, Boolean> key = Tuple.of(arbitrary, genSize, withEdgeCases);

		RandomGenerator<?> generator = computeIfAbsent(
				generatorStore.get(),
				key,
				ignore -> generatorSupplier.get()
		);
		return (RandomGenerator<U>) generator;
	}

	// Had to roll my on computeIfAbsent because HashMap.computeIfAbsent()
	// does not allow modifications of the map within the mapping function
	private static <K, V> V computeIfAbsent(
			Map<K, V> cache,
			K key,
			Function<? super K, ? extends V> mappingFunction
	) {
		V result = cache.get(key);

		if (result == null) {
			result = mappingFunction.apply(key);
			cache.put(key, result);
		}

		return result;
	}

}