package net.jqwik.execution;

import java.util.*;
import java.util.function.*;

import org.junit.platform.engine.reporting.*;

import net.jqwik.*;
import net.jqwik.api.*;
import net.jqwik.descriptor.*;
import net.jqwik.properties.*;
import net.jqwik.support.*;

public class CheckedProperty {

	public final String propertyName;
	public final CheckedFunction checkedFunction;
	public final List<MethodParameter> forAllParameters;
	private final ArbitraryResolver arbitraryResolver;
	private final Optional<Iterable<? extends Tuple>> optionalData;
	public final PropertyConfiguration configuration;

	public CheckedProperty(
		String propertyName, CheckedFunction checkedFunction, List<MethodParameter> forAllParameters,
		ArbitraryResolver arbitraryResolver, Optional<Iterable<? extends Tuple>> optionalData, PropertyConfiguration configuration
	) {
		this.propertyName = propertyName;
		this.checkedFunction = checkedFunction;
		this.forAllParameters = forAllParameters;
		this.arbitraryResolver = arbitraryResolver;
		this.optionalData = optionalData;
		this.configuration = configuration;
	}

	public PropertyCheckResult check(Consumer<ReportEntry> publisher, Reporting[] reporting) {
		PropertyConfiguration effectiveConfiguration = configurationWithEffectiveSeed();
		try {
			return createGenericProperty(effectiveConfiguration).check(publisher, reporting);
		} catch (CannotFindArbitraryException cannotFindArbitraryException) {
			return PropertyCheckResult.erroneous(effectiveConfiguration.getStereotype(), propertyName, 0, 0, effectiveConfiguration.getSeed(), Collections.emptyList(), cannotFindArbitraryException);
		}
	}

	private PropertyConfiguration configurationWithEffectiveSeed() {
		String effectiveSeed = configuration.getSeed().equals(Property.SEED_NOT_SET)
								   ? SourceOfRandomness.createRandomSeed()
								   : configuration.getSeed();
		return configuration.withSeed(effectiveSeed);
	}

	private GenericProperty createGenericProperty(PropertyConfiguration configuration) {
		// TODO: Clean up this terrible and partially untested code
		Optional<ExhaustiveShrinkablesGenerator> optionalExhaustive = createExhaustiveShrinkablesGenerator(configuration);
		if (configuration.getGenerationMode() == GenerationMode.AUTO) {
			if (optionalData.isPresent()) {
				configuration = configuration.withGenerationMode(GenerationMode.DATA_DRIVEN);
			} else if (optionalExhaustive.isPresent()) {
				configuration = configuration.withGenerationMode(GenerationMode.EXHAUSTIVE);
			} else {
				configuration = configuration.withGenerationMode(GenerationMode.RANDOMIZED);
			}
		} else if (configuration.getGenerationMode() == GenerationMode.EXHAUSTIVE) {
			if (optionalData.isPresent()) {
				throw new JqwikException("You cannot have both a @FromData annotation and @Property(generation = EXHAUSTIVE)");
			}
		} else if (configuration.getGenerationMode() == GenerationMode.RANDOMIZED) {
			if (optionalData.isPresent()) {
				throw new JqwikException("You cannot have both a @FromData annotation and @Property(generation = RANDOMIZED)");
			}
		}
		ShrinkablesGenerator shrinkablesGenerator =
			configuration.getGenerationMode() == GenerationMode.EXHAUSTIVE
				? optionalExhaustive.get()
				: configuration.getGenerationMode() == GenerationMode.DATA_DRIVEN
					  ? createDataBasedShrinkablesGenerator(configuration)
					  : createRandomizedShrinkablesGenerator(configuration);

		return new GenericProperty(propertyName, configuration, shrinkablesGenerator, checkedFunction);
	}

	private Optional<ExhaustiveShrinkablesGenerator> createExhaustiveShrinkablesGenerator(PropertyConfiguration configuration) {
		try {
			ExhaustiveShrinkablesGenerator exhaustiveShrinkablesGenerator =
				ExhaustiveShrinkablesGenerator.forParameters(forAllParameters, arbitraryResolver);
			return Optional.of(exhaustiveShrinkablesGenerator);
		} catch (JqwikException ex) {
			return Optional.empty();
		}
	}

	private ShrinkablesGenerator createDataBasedShrinkablesGenerator(PropertyConfiguration configuration) {
		if (configuration.getGenerationMode() != GenerationMode.DATA_DRIVEN) {
			throw new JqwikException("You cannot have both a @FromData annotation and @Property(generation = RANDOMIZED)");
		}
		return new DataBasedShrinkablesGenerator(forAllParameters, optionalData.get());
	}

	private ShrinkablesGenerator createRandomizedShrinkablesGenerator(PropertyConfiguration configuration) {
		Random random = SourceOfRandomness.create(configuration.getSeed());
		return RandomizedShrinkablesGenerator.forParameters(forAllParameters, arbitraryResolver, random, configuration.getTries());
	}

}
