package net.jqwik.execution.lifecycle;

import java.lang.reflect.*;
import java.util.*;

import org.junit.platform.commons.support.*;
import org.junit.platform.engine.*;

import net.jqwik.api.lifecycle.*;
import net.jqwik.descriptor.*;

public class JqwikLifecycleRegistrator {

	private final LifecycleRegistry lifecycleRegistry;

	public JqwikLifecycleRegistrator(LifecycleRegistry lifecycleRegistry) {
		this.lifecycleRegistry = lifecycleRegistry;
	}

	public void registerLifecycleHooks(TestDescriptor rootDescriptor) {
		registerGlobalHooks(rootDescriptor);
		register(rootDescriptor);
	}

	private void registerGlobalHooks(TestDescriptor rootDescriptor) {
		for (LifecycleHook lifecycleHook : RegisteredLifecycleHooks.getRegisteredHooks()) {
			lifecycleRegistry.registerLifecycleInstance(rootDescriptor, lifecycleHook);
		}
	}

	private void register(TestDescriptor descriptor) {
		if (descriptor instanceof PropertyMethodDescriptor) {
			registerPropertyMethodHooks((PropertyMethodDescriptor) descriptor);
		}
		if (descriptor instanceof ContainerClassDescriptor) {
			registerContainerHooks((ContainerClassDescriptor) descriptor);
		}
		for (TestDescriptor childDescriptor : descriptor.getChildren()) {
			register(childDescriptor);
		}
	}

	private void registerContainerHooks(ContainerClassDescriptor containerClassDescriptor) {
		Class<?> containerClass = containerClassDescriptor.getContainerClass();
		registerHooks(containerClassDescriptor, containerClass);
	}

	private void registerPropertyMethodHooks(PropertyMethodDescriptor propertyMethodDescriptor) {
		Method targetMethod = propertyMethodDescriptor.getTargetMethod();
		registerHooks(propertyMethodDescriptor, targetMethod);
	}

	private void registerHooks(TestDescriptor descriptor, AnnotatedElement element) {
		List<AddLifecycleHook> addLifecycleHooks = AnnotationSupport.findRepeatableAnnotations(element, AddLifecycleHook.class);
		for (AddLifecycleHook addLifecycleHook : addLifecycleHooks) {
			lifecycleRegistry.registerLifecycleHook(descriptor, addLifecycleHook.value());
		}
	}

}
