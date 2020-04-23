package net.jqwik.engine.properties.arbitraries;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.engine.properties.arbitraries.randomized.*;

public class MapArbitrary<K, V> extends AbstractArbitraryBase implements SizableArbitrary<Map<K, V>> {

	private final Arbitrary<K> keysArbitrary;
	private final Arbitrary<V> valuesArbitrary;

	private int minSize = 0;
	private int maxSize = RandomGenerators.DEFAULT_COLLECTION_SIZE;

	public MapArbitrary(Arbitrary<K> keysArbitrary, Arbitrary<V> valuesArbitrary) {
		this.keysArbitrary = keysArbitrary;
		this.valuesArbitrary = valuesArbitrary;
	}

	@Override
	public SizableArbitrary<Map<K, V>> ofMinSize(int minSize) {
		MapArbitrary<K, V> clone = typedClone();
		clone.minSize = minSize;
		return clone;
	}

	@Override
	public SizableArbitrary<Map<K, V>> ofMaxSize(int maxSize) {
		MapArbitrary<K, V> clone = typedClone();
		clone.maxSize = maxSize;
		return clone;
	}

	@Override
	public RandomGenerator<Map<K, V>> generator(int genSize) {
		return mapArbitrary().generator(genSize);
	}

	private Arbitrary<Map<K, V>> mapArbitrary() {
		// Using list of generated Map.Entry does not work because of potential duplicate keys
		Arbitrary<List<K>> keySets = keysArbitrary.set().ofMinSize(minSize).ofMaxSize(maxSize).map(ArrayList::new);
		return keySets.flatMap(keys -> valuesArbitrary.list().ofSize(keys.size()).map(
			values -> {
				HashMap<K, V> map = new HashMap<>();
				for (int i = 0; i < keys.size(); i++) {
					K key = keys.get(i);
					V value = values.get(i);
					map.put(key, value);
				}
				return map;
			}));
	}

	@Override
	public Optional<ExhaustiveGenerator<Map<K, V>>> exhaustive(long maxNumberOfSamples) {
		return mapArbitrary().exhaustive(maxNumberOfSamples);
	}

	@Override
	public EdgeCases<Map<K, V>> edgeCases() {
		EdgeCases<Map<K, V>> emptyMapEdgeCase =
			minSize == 0
				? EdgeCases.fromSupplier(() -> Shrinkable.unshrinkable(new HashMap<>()))
				: EdgeCases.none();
		EdgeCases<Map<K, V>> singleEntryEdgeCases =
			minSize <= 1
				? singleEntryEdgeCases()
				: EdgeCases.none();
		return EdgeCases.concat(emptyMapEdgeCase, singleEntryEdgeCases);
	}

	private EdgeCases<Map<K, V>> singleEntryEdgeCases() {
		return keysArbitrary.edgeCases().flatMapArbitrary(
			key ->
				valuesArbitrary.map(value -> {
					HashMap<K, V> map = new HashMap<>();
					map.put(key, value);
					return map;
				})
		);
	}
}
