package net.jqwik.engine;

import java.nio.file.*;
import java.util.*;

import examples.packageWithDisabledTests.*;
import examples.packageWithErrors.*;
import examples.packageWithFailings.*;
import examples.packageWithSeveralContainers.*;
import examples.packageWithSingleContainer.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.*;
import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.*;
import org.junit.platform.engine.reporting.*;
import org.junit.platform.testkit.engine.*;

import net.jqwik.api.*;
import net.jqwik.engine.recording.*;
import net.jqwik.engine.support.*;

import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
import static org.junit.platform.testkit.engine.Event.*;
import static org.junit.platform.testkit.engine.EventConditions.*;
import static org.junit.platform.testkit.engine.EventType.*;

class JqwikIntegrationTests {

	private JqwikConfiguration configuration(final boolean useJunitPlatformReporter) {
		return new JqwikConfiguration() {
			@Override
			public PropertyAttributesDefaults propertyDefaultValues() {
				return TestHelper.propertyAttributesDefaults();
			}

			@Override
			public TestEngineConfiguration testEngineConfiguration() {
				return new TestEngineConfiguration() {
					@Override
					public TestRunRecorder recorder() {
						return testRun -> {
						};
					}

					@Override
					public TestRunData previousRun() {
						return new TestRunData();
					}

					@Override
					public Set<UniqueId> previousFailures() {
						return Collections.emptySet();
					}

				};
			}

			@Override
			public boolean useJunitPlatformReporter() {
				return useJunitPlatformReporter;
			}

			@Override
			public boolean reportOnlyFailures() {
				return true;
			}
		};
	}

	private JqwikTestEngine createTestEngine(final boolean useJunitPlatformReporter) {
		return new JqwikTestEngine(unusedConfigurationProperties -> configuration(useJunitPlatformReporter));
	}

	private JqwikTestEngine createTestEngine() {
		return createTestEngine(false);
	}

	@Example
	@SuppressLogging
	void runTestsFromRootDir() {
		Set<Path> classpathRoots = JqwikReflectionSupport.getAllClasspathRootDirectories();
		ClasspathRootSelector[] classpathRootSelectors = selectClasspathRoots(classpathRoots)
															 .toArray(new ClasspathRootSelector[classpathRoots.size()]);
		Events events = EngineTestKit
							.engine(createTestEngine())
							.selectors(classpathRootSelectors)
							.filters((Filter<?>) PackageNameFilter.includePackageNames("examples.packageWithSingleContainer"))
							.execute()
							.allEvents();

		assertSimpleExampleTests(events);
	}

	@Example
	@SuppressLogging
	void runTestsFromPackage() {
		Events events = EngineTestKit
							.engine(createTestEngine())
							.selectors(selectPackage("examples.packageWithSingleContainer"))
							.execute()
							.allEvents();

		assertSimpleExampleTests(events);
	}

	@Example
	@SuppressLogging
	void runTestsFromClass() {
		Events events = EngineTestKit
							.engine(createTestEngine())
							.selectors(selectClass(SimpleExampleTests.class))
							.execute()
							.allEvents();

		assertSimpleExampleTests(events);
	}

	@Example
	void failingConstructorFailsTests() {
		Events events = EngineTestKit
							.engine(createTestEngine())
							.selectors(selectClass(ContainerWithFailingConstructor.class))
							.execute()
							.allEvents();

		events.assertEventsMatchExactly(
			event(engine(), started()),
			event(container(ContainerWithFailingConstructor.class), started()),
			event(test("success"), started()),
			event(test("success"), finishedWithFailure()),
			event(container(ContainerWithFailingConstructor.class), finishedSuccessfully()),
			event(engine(), finishedSuccessfully())
		);
	}

	private void assertSimpleExampleTests(Events events) {
		events.assertEventsMatchExactly(
			event(engine(), started()),
			event(container(SimpleExampleTests.class), started()),
			event(test("failing"), started()),
			event(test("failing"), finishedWithFailure()),
			event(test("withJupiterAnnotation"), skippedWithReason(s -> true)),
			event(test("staticExample"), skippedWithReason(s -> true)),
			event(test("succeeding"), started()),
			event(test("succeeding"), finishedSuccessfully()),
			event(container(SimpleExampleTests.class), finishedSuccessfully()),
			event(engine(), finishedSuccessfully())
		);
	}

	@Example
	void runTestsFromMethod() {
		Events events = EngineTestKit
							.engine(createTestEngine())
							.selectors(selectMethod(SimpleExampleTests.class, "succeeding"))
							.execute()
							.allEvents();

		events.assertEventsMatchExactly(
			event(engine(), started()),
			event(container(SimpleExampleTests.class), started()),
			event(test("succeeding"), started()),
			event(test("succeeding"), finishedSuccessfully()),
			event(container(SimpleExampleTests.class), finishedSuccessfully()),
			event(engine(), finishedSuccessfully())
		);
	}

	@Example
	void runMixedExamples() {

		Events events = EngineTestKit
							.engine(createTestEngine())
							.selectors(selectPackage("examples.packageWithSeveralContainers"))
							.execute()
							.allEvents();

		// Order of classes is platform dependent :-(
		// events.assertEventsMatchLooselyInOrder(
		events.assertEventsMatchLoosely(
			event(engine(), started()),

			// ExampleTests
			event(container(ExampleTests.class), started()),
			event(test("succeeding"), started()),
			event(test("succeeding"), finishedSuccessfully()),
			event(test("failingSimple"), started()),
			event(test("failingSimple"), finishedWithFailure()),
			event(container(ExampleTests.class), finishedSuccessfully()),

			// PropertyTests
			event(container(PropertyTests.class), started()),
			event(test("isTrue"), started()),
			event(test("isTrue"), finishedSuccessfully()),
			event(test("allNumbersAreZero"), started()),
			event(test("allNumbersAreZero"), finishedWithFailure()),
			event(test("withEverything"), started()),
			event(test("withEverything"), finishedSuccessfully()),
			event(test("isFalse"), started()),
			event(test("isFalse"), finishedWithFailure()),
			event(container(PropertyTests.class), finishedSuccessfully()),

			// MixedTests
			event(container(MixedTests.class), started()),
			event(test("anExample"), started()),
			event(test("anExample"), finishedSuccessfully()),
			event(test("aProperty"), started()),
			event(test("aProperty"), finishedSuccessfully()),
			event(container(MixedTests.class), finishedSuccessfully()),

			event(engine(), finishedSuccessfully())
		);

	}

	@Example
	void runDisabledTests() {

		Events events = EngineTestKit
							.engine(createTestEngine())
							.selectors(selectPackage("examples.packageWithDisabledTests"))
							.execute()
							.allEvents();

		events.assertEventsMatchLooselyInOrder(
			event(container(DisabledTests.class), started()),
			event(test("disabledFailure"), skippedWithReason(r -> r.startsWith("@Disabled:"))),
			event(container(DisabledTests.class), finishedSuccessfully())
		);

		events.assertEventsMatchLooselyInOrder(
			event(container(DisabledTests.class), started()),
			event(test("disabledSuccess"), skippedWithReason("a reason")),
			event(container(DisabledTests.class), finishedSuccessfully())
		);

		events.assertEventsMatchLooselyInOrder(
			event(container(DisabledTests.class), started()),
			event(container(DisabledTests.DisabledGroup.class), skippedWithReason(r -> r.startsWith("@Disabled:"))),
			event(container(DisabledTests.class), finishedSuccessfully())
		);

	}

	@Example
	void statisticsAreBeingReported() {

		Events events = EngineTestKit
							.engine(createTestEngine(true))
							.selectors(selectClass(ContainerWithStatistics.class))
							.execute()
							.allEvents();

		events.assertEventsMatchLooselyInOrder(
			event(container(ContainerWithStatistics.class), started()),
			event(test("propertyWithStatistics"), reported("[ContainerWithStatistics:propertyWithStatistics] (100) statistics")),
			event(test("propertyWithStatistics"), finishedSuccessfully()),
			event(container(ContainerWithStatistics.class), finishedSuccessfully())
		);

	}

	@Example
	void outOfMemoryErrorIsPropagatedToTop() {
		Assertions.assertThatThrownBy(
			() -> EngineTestKit
					  .engine(createTestEngine(true))
					  .selectors(selectClass(ContainerWithOOME.class))
					  .execute()
		).isInstanceOf(OutOfMemoryError.class);
	}

	private Condition<Event> reported(String key) {
		Condition<ReportEntry> condition = new Condition<ReportEntry>() {
			@Override
			public boolean matches(ReportEntry entry) {
				return entry.getKeyValuePairs().containsKey(key);
			}
		};
		return Assertions.allOf(
			type(REPORTING_ENTRY_PUBLISHED),
			new Condition<>(byPayload(ReportEntry.class, condition::matches), "event with result where %s", condition)
		);
	}

}
