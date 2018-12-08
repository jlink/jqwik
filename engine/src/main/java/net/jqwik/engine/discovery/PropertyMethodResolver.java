package net.jqwik.engine.discovery;

import java.lang.reflect.*;
import java.util.*;

import org.junit.platform.commons.support.*;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.hierarchical.Node.*;

import net.jqwik.api.*;
import net.jqwik.engine.*;
import net.jqwik.engine.descriptor.*;
import net.jqwik.engine.discovery.specs.*;
import net.jqwik.engine.recording.*;

class PropertyMethodResolver implements ElementResolver {

	private final PropertyDiscoverySpec methodSpec = new PropertyDiscoverySpec();
	private final TestRunData testRunData;
	private final PropertyDefaultValues propertyDefaultValues;

	PropertyMethodResolver(TestRunData testRunData, PropertyDefaultValues propertyDefaultValues) {
		this.testRunData = testRunData;
		this.propertyDefaultValues = propertyDefaultValues;
	}

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
		if (!(element instanceof Method))
			return Collections.emptySet();

		if (!(parent instanceof ContainerClassDescriptor))
			return Collections.emptySet();

		Method method = (Method) element;
		if (!isRelevantMethod(method))
			return Collections.emptySet();

		return Collections.singleton(createTestDescriptor(parent, method));
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {
		if (!segment.getType().equals(getSegmentType()))
			return Optional.empty();

		if (!(parent instanceof ContainerClassDescriptor))
			return Optional.empty();

		Optional<Method> optionalMethod = findMethod(segment, (ContainerClassDescriptor) parent);
		if (!optionalMethod.isPresent())
			return Optional.empty();

		Method method = optionalMethod.get();
		if (!isRelevantMethod(method))
			return Optional.empty();

		return Optional.of(createTestDescriptor(parent, method));
	}

	private boolean isRelevantMethod(Method candidate) {
		return methodSpec.shouldBeDiscovered(candidate);
	}

	private Optional<Method> findMethod(UniqueId.Segment segment, ContainerClassDescriptor parent) {
		return JqwikUniqueIDs.findMethodBySegment(segment, parent.getContainerClass());
	}

	private TestDescriptor createTestDescriptor(TestDescriptor parent, Method method) {
		UniqueId uniqueId = createUniqueId(method, parent);
		Class<?> testClass = ((ContainerClassDescriptor) parent).getContainerClass();
		TestDescriptor newDescriptor = createTestDescriptor(uniqueId, testClass, method);
		SkipResult shouldBeSkipped = methodSpec.shouldBeSkipped(method);
		if (shouldBeSkipped.isSkipped()) {
			return new SkipExecutionDecorator(newDescriptor, shouldBeSkipped.getReason().orElse(""));
		} else {
			return newDescriptor;
		}
	}

	private TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method) {
		Property property = AnnotationSupport.findAnnotation(method, Property.class).orElseThrow(() -> {
			String message = String.format("Method [%s] is not annotated with @Property", method);
			return new JqwikException(message);
		});
		String previousSeed = previousSeed(uniqueId);
		List falsifiedSample = falsifiedSample(uniqueId);
		PropertyConfiguration propertyConfig = PropertyConfiguration.from(property, propertyDefaultValues, previousSeed, falsifiedSample);
		return new PropertyMethodDescriptor(uniqueId, method, testClass, propertyConfig);
	}

	private String previousSeed(UniqueId uniqueId) {
		return testRunData.byUniqueId(uniqueId)
						  .filter(TestRun::isNotSuccessful)
						  .flatMap(TestRun::randomSeed)
						  .orElse(null);
	}

	private List falsifiedSample(UniqueId uniqueId) {
		return testRunData.byUniqueId(uniqueId)
						  .filter(TestRun::isNotSuccessful)
						  .flatMap(TestRun::falsifiedSample)
						  .orElse(null);
	}

	private String getSegmentType() {
		return JqwikUniqueIDs.PROPERTY_SEGMENT_TYPE;
	}

	private UniqueId createUniqueId(Method method, TestDescriptor parent) {
		return JqwikUniqueIDs.appendProperty(parent.getUniqueId(), method);
	}
}
