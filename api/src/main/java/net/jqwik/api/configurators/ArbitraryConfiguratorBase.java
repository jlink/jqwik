package net.jqwik.api.configurators;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import org.junit.platform.commons.support.*;

import net.jqwik.api.*;
import net.jqwik.api.providers.*;

import static org.junit.platform.commons.support.ReflectionSupport.*;

/**
 * Using this base class is the easiest way to make use of the configuration mechanism
 * described in {@linkplain ArbitraryConfigurator}
 *
 * <p>
 * Implementations must be registered in <code>/META-INF/services/net.jqwik.api.configurators.ArbitraryConfigurator</code>
 * so that they will be automatically considered for arbitrary configuration.
 * <p>
 * <p>
 * Some examples that come with jqwik:
 *
 * <ul>
 *     <li><a href="https://github.com/jlink/jqwik/blob/master/engine/src/main/java/net/jqwik/engine/configurators/CharsConfigurator.java"
 *     >net.jqwik.engine.configurators.CharsConfigurator</a></li>
 *     <li><a href="https://github.com/jlink/jqwik/blob/master/engine/src/main/java/net/jqwik/engine/configurators/SizeConfigurator.java"
 *     >net.jqwik.engine.configurators.SizeConfigurator</a></li>
 * </ul>
 */
public abstract class ArbitraryConfiguratorBase implements ArbitraryConfigurator {

	private final static String CONFIG_METHOD_NAME = "configure";

	@Override
	public <T> Arbitrary<T> configure(Arbitrary<T> arbitrary, TypeUsage targetType) {
		if (!acceptTargetType(targetType)) {
			return arbitrary;
		}
		List<Annotation> annotations = configurationAnnotations(targetType);
		for (Annotation annotation : annotations) {
			List<Method> configurationMethods = findConfigurationMethods(arbitrary, annotation);
			for (Method configurationMethod : configurationMethods) {
				arbitrary = configureWithMethod(arbitrary, annotation, configurationMethod);
			}
		}
		return arbitrary;
	}

	/**
	 * Override if configurator only works for certain types of domain objects
	 *
	 * @param targetType The concrete domain type to be generated
	 */
	protected boolean acceptTargetType(TypeUsage targetType) {
		return true;
	}

	private List<Annotation> configurationAnnotations(TypeUsage parameter) {
		return parameter.getAnnotations().stream() //
						  .filter(annotation -> !annotation.annotationType().equals(ForAll.class)) //
						  .collect(Collectors.toList());
	}


	private <T> Arbitrary<T> configureWithMethod(Arbitrary<T> arbitrary, Annotation annotation, Method configurationMethod) {
		Object configurationResult = invokeMethod(configurationMethod, this, arbitrary, annotation);
		if (configurationResult == null) {
			return arbitrary;
		}
		if (!(configurationResult instanceof Arbitrary)) {
			throw new ArbitraryConfigurationException(configurationMethod);
		}
		//noinspection unchecked
		return (Arbitrary<T>) configurationResult;
	}

	private <T> List<Method> findConfigurationMethods(Arbitrary<T> arbitrary, Annotation annotation) {
		Class<? extends Arbitrary> arbitraryClass = arbitrary.getClass();
		return findMethods(getClass(),
				method -> hasCompatibleConfigurationSignature(method, arbitraryClass, annotation), HierarchyTraversalMode.BOTTOM_UP);
	}

	private static boolean hasCompatibleConfigurationSignature(Method candidate, Class<? extends Arbitrary> arbitraryClass,
			Annotation annotation) {
		if (!CONFIG_METHOD_NAME.equals(candidate.getName())) {
			return false;
		}
		if (!Arbitrary.class.isAssignableFrom(candidate.getReturnType())) {
			return false;
		}
		if (candidate.getParameterCount() != 2) {
			return false;
		}
		if (candidate.getParameterTypes()[1] != annotation.annotationType()) {
			return false;
		}
		Class<?> upperArbitraryType = candidate.getParameterTypes()[0];
		return upperArbitraryType.isAssignableFrom(arbitraryClass);
	}

}
