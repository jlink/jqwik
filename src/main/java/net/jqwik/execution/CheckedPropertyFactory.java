package net.jqwik.execution;

import net.jqwik.api.*;
import net.jqwik.descriptor.*;
import net.jqwik.properties.*;
import net.jqwik.support.*;
import org.junit.platform.commons.support.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class CheckedPropertyFactory {

	private static List<Class<?>> BOOLEAN_RETURN_TYPES = Arrays.asList(boolean.class, Boolean.class);

	public CheckedProperty fromDescriptor(PropertyMethodDescriptor propertyMethodDescriptor, Object testInstance) {
		String propertyName = propertyMethodDescriptor.getLabel();

		Method propertyMethod = propertyMethodDescriptor.getTargetMethod();
		PropertyConfiguration configuration = propertyMethodDescriptor.getConfiguration();

		CheckedFunction checkedFunction = createCheckedFunction(propertyMethodDescriptor, testInstance);
		List<MethodParameter> forAllParameters = extractForAllParameters(propertyMethod, propertyMethodDescriptor.getContainerClass());

		PropertyMethodArbitraryResolver arbitraryProvider = new PropertyMethodArbitraryResolver(propertyMethodDescriptor.getContainerClass(), testInstance);
		PropertyMethodDataResolver dataResolver = new PropertyMethodDataResolver(propertyMethodDescriptor.getContainerClass(), testInstance);

		return new CheckedProperty(propertyName, checkedFunction, forAllParameters, arbitraryProvider, dataResolver, configuration);
	}

	private CheckedFunction createCheckedFunction(PropertyMethodDescriptor propertyMethodDescriptor, Object testInstance) {
		// Todo: Bind all non @ForAll params first
		Class<?> returnType = propertyMethodDescriptor.getTargetMethod().getReturnType();
		Function<List, Object> function = params -> ReflectionSupport.invokeMethod(propertyMethodDescriptor.getTargetMethod(), testInstance,
				params.toArray());
		if (BOOLEAN_RETURN_TYPES.contains(returnType))
			return params -> (boolean) function.apply(params);
		else
			return params -> {
				function.apply(params);
				return true;
			};
	}

	private List<MethodParameter> extractForAllParameters(Method targetMethod, Class<?> containerClass) {
		return Arrays //
				.stream(JqwikReflectionSupport.getMethodParameters(targetMethod, containerClass)) //
				.filter(this::isForAllPresent) //
				.collect(Collectors.toList());
	}

	private boolean isForAllPresent(MethodParameter parameter) {
		return parameter.isAnnotated(ForAll.class);
	}

}
