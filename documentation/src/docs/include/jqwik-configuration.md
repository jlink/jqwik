_jqwik_ uses JUnit's [configuration parameters](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params) to configure itself.

The simplest form is a file `junit-platform.properties` in your classpath in which you can configure
a few basic parameters:

```
jqwik.database = .jqwik-database             # The database file in which to store data of previous runs.
                                             # Set to empty to fully disable test run recording.
jqwik.tries.default = 1000                   # The default number of tries for each property
jqwik.maxdiscardratio.default = 5            # The default ratio before assumption misses make a property fail
jqwik.reporting.onlyfailures = false         # Set to true if only falsified properties should be reported
jqwik.reporting.usejunitplatform = false     # Set to true if you want to use platform reporting
jqwik.failures.runfirst = false              # Set to true if you want to run the failing tests from the previous run first
jqwik.failures.after.default = PREVIOUS_SEED # Set default behaviour for falsified properties:
                                             # PREVIOUS_SEED, SAMPLE_ONLY or SAMPLE_FIRST
jqwik.generation.default = AUTO              # Set default behaviour for generation:
                                             # AUTO, RANDOMIZED, or EXHAUSTIVE
jqwik.edgecases.default = MIXIN              # Set default behaviour for edge cases generation:
                                             # FIRST, MIXIN, or NONE
jqwik.shrinking.default = BOUNDED            # Set default shrinking behaviour:
                                             # BOUNDED, FULL, or OFF
jqwik.shrinking.bounded.seconds = 10         # The maximum number of seconds to shrink if
                                             # shrinking behaviour is set to BOUNDED
jqwik.seeds.whenfixed = ALLOW                # How a test should act when a seed is fixed. Can set to ALLOW, WARN or FAIL
                                             # Useful to prevent accidental commits of fixed seeds into source control.                                             
```

Prior releases of _jqwik_ used a custom `jqwik.properties`. While this continues to work, it is deprecated
and will be removed in a future release. Some names have changed:

- `database` -> `jqwik.database`
- `defaultTries` -> `jqwik.tries.default`
- `defaultMaxDiscardRatio` -> `jqwik.maxdiscardratio.default`
- `useJunitPlatformReporter` -> `jqwik.reporting.usejunitplatform`
- `defaultAfterFailure` -> `jqwik.failures.after.default`
- `reportOnlyFailures` -> `jqwik.reporting.onlyfailures`
- `defaultGeneration` -> `jqwik.generation.default`
- `defaultEdgeCases` -> `jqwik.edgecases.default`
- `defaultShrinking` -> `jqwik.shrinking.default`
- `boundedShrinkingSeconds` -> `jqwik.shrinking.bounded.seconds`
- `runFailuresFirst` -> `jqwik.failures.runfirst`

