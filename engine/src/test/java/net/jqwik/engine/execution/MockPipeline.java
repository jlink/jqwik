package net.jqwik.engine.execution;

import java.util.*;

import net.jqwik.engine.execution.pipeline.*;

public class MockPipeline implements Pipeline {
	private List<ExecutionTask> tasks = new ArrayList<>();

	@Override
	public void submit(ExecutionTask task, ExecutionTask... predecessors) {
		tasks.add(task);
	}

	void runWith(PropertyExecutionListener listener) {
		tasks.forEach(task -> task.execute(listener));
	}
}
