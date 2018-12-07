package net.jqwik.api.lifecycle;

import java.util.*;

/**
 * Experimental feature. Not ready for public usage yet.
 */
public class PropertyExecutionResult {

	/**
	 * Status of executing a single test or container.
	 */
	public enum Status {

		/**
		 * Indicates that the execution of a property was
		 * <em>successful</em>.
		 */
		SUCCESSFUL,

		/**
		 * Indicates that the execution of a property was
		 * <em>aborted</em> (started but not finished).
		 */
		ABORTED,

		/**
		 * Indicates that the execution of a property has
		 * <em>failed</em>.
		 */
		FAILED
	}

	private final Status status;
	private final String seed;
	private final List<Object> falsifiedSample;
	private final Throwable throwable;

	private PropertyExecutionResult(Status status, String seed, Throwable throwable, List<Object> falsifiedSample) {
		this.status = status;
		this.seed = seed != null ? (seed.isEmpty() ? null : seed) : null;
		this.throwable = throwable;
		this.falsifiedSample = falsifiedSample;
	}

	public static PropertyExecutionResult successful() {
		return new PropertyExecutionResult(Status.SUCCESSFUL, null, null, null);
	}

	public static PropertyExecutionResult successful(String seed) {
		return new PropertyExecutionResult(Status.SUCCESSFUL, seed, null, null);
	}

	public static PropertyExecutionResult failed(Throwable throwable, String seed, List<Object> sample) {
		return new PropertyExecutionResult(Status.FAILED, seed, throwable, sample);
	}

	public static PropertyExecutionResult aborted(Throwable throwable, String seed) {
		return new PropertyExecutionResult(Status.ABORTED, seed, throwable, null);
	}

	public Optional<String> getSeed() {
		return Optional.ofNullable(seed);
	}

	public Optional<List<Object>> getFalsifiedSample() {
		return Optional.ofNullable(falsifiedSample);
	}

	public Status getStatus() {
		return status;
	}

	public Optional<Throwable> getThrowable() {
		return Optional.ofNullable(throwable);
	}

	@Override
	public String toString() {
		return String.format("PropertyExecutionResult[%s]", status);
	}

}
