package net.jqwik.execution;

import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

import net.jqwik.api.lifecycles.*;
import org.junit.platform.engine.*;

import net.jqwik.descriptor.*;
import net.jqwik.execution.pipeline.*;
import net.jqwik.recording.TestRunRecorder;

public class JqwikExecutor {

	private final LifecycleRegistry registry;
	private final TestRunRecorder recorder;
	private final Set<UniqueId> previousFailedTests;
	private final PropertyTaskCreator propertyTaskCreator = new PropertyTaskCreator();
	private final ContainerTaskCreator containerTaskCreator = new ContainerTaskCreator();
	private final ExecutionTaskCreator childTaskCreator = this::createTask;

	private static final Logger LOG = Logger.getLogger(JqwikExecutor.class.getName());

	public JqwikExecutor(LifecycleRegistry registry, TestRunRecorder recorder, Set<UniqueId> previousFailedTests) {
		this.registry = registry;
		this.recorder = recorder;
		this.previousFailedTests = previousFailedTests;
	}

	public void execute(TestDescriptor descriptor, EngineExecutionListener engineExecutionListener) {
		EngineExecutionListener recordingListener = new RecordingExecutionListener(recorder, engineExecutionListener);
		ExecutionPipeline pipeline = new ExecutionPipeline(recordingListener);
		ExecutionTask mainTask = createTask(descriptor, pipeline);
		pipeline.submit(mainTask);
		letNonSuccessfulTestsExecuteFirst(pipeline);
		pipeline.runToTermination();
	}

	private void letNonSuccessfulTestsExecuteFirst(ExecutionPipeline pipeline) {
		previousFailedTests.forEach(uniqueId -> pipeline.executeFirst(uniqueId));
	}

	private ExecutionTask createTask(TestDescriptor descriptor, Pipeline pipeline) {
		if (descriptor.getClass().equals(JqwikEngineDescriptor.class)) {
			return createContainerTask(descriptor, pipeline);
		}
		if (descriptor.getClass().equals(ContainerClassDescriptor.class)) {
			return createContainerTask(descriptor, pipeline);
		}
		if (descriptor.getClass().equals(PropertyMethodDescriptor.class)) {
			return createPropertyTask((PropertyMethodDescriptor) descriptor, pipeline);
		}
		if (descriptor.getClass().equals(SkipExecutionDecorator.class)) {
			return createSkippingTask((SkipExecutionDecorator) descriptor, pipeline);
		}
		return ExecutionTask.from(listener -> LOG.warning(() -> String.format("Cannot execute descriptor [%s]", descriptor)),
				descriptor.getUniqueId(), "log warning");
	}

	private ExecutionTask createSkippingTask(SkipExecutionDecorator descriptor, Pipeline pipeline) {
		String taskDescription = String.format("Skipping [%s] due to: %s", descriptor.getDisplayName(), descriptor.getSkippingReason());
		return ExecutionTask.from(listener -> listener.executionSkipped(descriptor, descriptor.getSkippingReason()),
				descriptor.getUniqueId(), taskDescription);
	}

	private ExecutionTask createPropertyTask(PropertyMethodDescriptor propertyMethodDescriptor, Pipeline pipeline) {
		Function<Object, PropertyLifecycle> lifecycleSupplier = registry.supplierFor(propertyMethodDescriptor);
		return propertyTaskCreator.createTask(propertyMethodDescriptor, lifecycleSupplier);
	}

	private ExecutionTask createContainerTask(TestDescriptor containerDescriptor, Pipeline pipeline) {
		return containerTaskCreator.createTask(containerDescriptor, childTaskCreator, pipeline);
	}

}
