package net.jqwik.execution;

import java.util.*;

import net.jqwik.execution.pipeline.*;

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
