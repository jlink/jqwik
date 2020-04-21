package net.jqwik.engine.properties.arbitraries.randomized;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;
import net.jqwik.engine.support.*;

abstract class AbstractFunctionGenerator<F, R> implements RandomGenerator<F> {
	final Class<F> functionalType;
	final RandomGenerator<R> resultGenerator;
	final List<Tuple2<Predicate<List<Object>>, Function<List<Object>, R>>> conditions;

	AbstractFunctionGenerator(
		Class<F> functionalType,
		RandomGenerator<R> resultGenerator,
		List<Tuple2<Predicate<List<Object>>, Function<List<Object>, R>>> conditions
	) {
		this.functionalType = functionalType;
		this.resultGenerator = resultGenerator;
		this.conditions = conditions;
	}

	F createFunctionProxy(InvocationHandler handler) {
		//noinspection unchecked
		return (F) Proxy.newProxyInstance(functionalType.getClassLoader(), new Class[]{functionalType}, handler);
	}


	Shrinkable<F> createConstantFunction(Shrinkable<R> shrinkableConstant) {
		return shrinkableConstant.map(this::constantFunction);
	}

	private F constantFunction(R constant) {
		InvocationHandler handler = (proxy, method, args) -> {
			if (JqwikReflectionSupport.isToStringMethod(method)) {
				return String.format(
					"Constant Function<%s>(%s)",
					functionalType.getSimpleName(),
					JqwikStringSupport.displayString(constant)
				);
			}
			return conditionalResult(args).orElse(new Object[]{constant})[0];
		};
		return createFunctionProxy(handler);
	}

	// Returns result wrapped in array to allow null as result
	protected Optional<Object[]> conditionalResult(Object[] args) {
		Optional<Object[]> conditionalResult = Optional.empty();
		for (Tuple2<Predicate<List<Object>>, Function<List<Object>, R>> condition : conditions) {
			List<Object> params = Arrays.asList(args);
			if (condition.get1().test(params)) {
				Object[] result = new Object[]{condition.get2().apply(params)};
				conditionalResult = Optional.of(result);
				break;
			}
		}
		return conditionalResult;
	}
}
