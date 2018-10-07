<h1>The jqwik User Guide
<span style="padding-left:1em;font-size:50%;font-weight:lighter">0.9.0-SNAPSHOT</span>
</h1>

<!-- use `doctoc --maxlevel 4 user-guide.md` to recreate the TOC -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
### Table of Contents  

- [How to Use](#how-to-use)
  - [Gradle](#gradle)
    - [Using JUnit's own Gradle Plugin](#using-junits-own-gradle-plugin)
    - [Using Gradle's Built-in Support](#using-gradles-built-in-support)
  - [Maven](#maven)
  - [Snapshot Releases](#snapshot-releases)
  - [Project without Build Tool](#project-without-build-tool)
- [Creating an Example-based Test](#creating-an-example-based-test)
- [Creating a Property](#creating-a-property)
  - [Optional `@Property` Parameters](#optional-property-parameters)
- [Assertions](#assertions)
- [Lifecycle](#lifecycle)
  - [Method Lifecycle](#method-lifecycle)
  - [Other Lifecycles](#other-lifecycles)
- [Grouping Tests](#grouping-tests)
- [Labeling Tests](#labeling-tests)
- [Tagging Tests](#tagging-tests)
- [Default Parameter Generation](#default-parameter-generation)
  - [Constraining Default Generation](#constraining-default-generation)
    - [Allow Null Values](#allow-null-values)
    - [Unique Values](#unique-values)
    - [String Length](#string-length)
    - [Character Sets](#character-sets)
    - [List, Set, Stream and Array Size:](#list-set-stream-and-array-size)
    - [Integer Constraints](#integer-constraints)
    - [Decimal Constraints](#decimal-constraints)
  - [Constraining parameterized types](#constraining-parameterized-types)
  - [Providing variable types](#providing-variable-types)
- [Self-Made Annotations](#self-made-annotations)
- [Customized Parameter Generation](#customized-parameter-generation)
  - [Static `Arbitraries` methods](#static-arbitraries-methods)
    - [Generate values yourself](#generate-values-yourself)
    - [Select values randomly](#select-values-randomly)
    - [Select randomly with Weights](#select-randomly-with-weights)
    - [Integers](#integers)
    - [Decimals](#decimals)
    - [Characters and Strings](#characters-and-strings)
    - [java.util.Random](#javautilrandom)
    - [Constants](#constants)
    - [Default Types](#default-types)
  - [Collections, Streams, Arrays and Optional](#collections-streams-arrays-and-optional)
  - [Fluent Configuration Interfaces](#fluent-configuration-interfaces)
  - [Generate `null` values](#generate-null-values)
  - [Filtering](#filtering)
  - [Creating unique values](#creating-unique-values)
  - [Mapping](#mapping)
  - [Flat Mapping](#flat-mapping)
  - [Flat Mapping with Tuple Types](#flat-mapping-with-tuple-types)
  - [Randomly Choosing among Arbitraries](#randomly-choosing-among-arbitraries)
  - [Combining Arbitraries](#combining-arbitraries)
    - [Flat Combination](#flat-combination)
  - [Fix an Arbitrary's `genSize`](#fix-an-arbitrarys-gensize)
- [Recursive Arbitraries](#recursive-arbitraries)
  - [Probabilistic Recursion](#probabilistic-recursion)
  - [Deterministic Recursion](#deterministic-recursion)
- [Contract Tests](#contract-tests)
- [Stateful Testing](#stateful-testing)
  - [Specify Actions](#specify-actions)
  - [Check Postconditions](#check-postconditions)
  - [Check Invariants](#check-invariants)
- [Assumptions](#assumptions)
- [Result Shrinking](#result-shrinking)
  - [Integrated Shrinking](#integrated-shrinking)
  - [Switch Shrinking Off](#switch-shrinking-off)
  - [Switch Shrinking to Full Mode](#switch-shrinking-to-full-mode)
- [Collecting and Reporting Statistics](#collecting-and-reporting-statistics)
- [Running and Configuration](#running-and-configuration)
  - [jqwik Configuration](#jqwik-configuration)
- [Providing Default Arbitraries](#providing-default-arbitraries)
  - [Simple Arbitrary Providers](#simple-arbitrary-providers)
  - [Arbitrary Providers for Parameterized Types](#arbitrary-providers-for-parameterized-types)
  - [Arbitrary Provider Priority](#arbitrary-provider-priority)
- [Create your own Annotations for Arbitrary Configuration](#create-your-own-annotations-for-arbitrary-configuration)
  - [Arbitrary Configuration Example: `@Odd`](#arbitrary-configuration-example-odd)
- [Implement your own Arbitraries and Generators](#implement-your-own-arbitraries-and-generators)
- [Data-Driven Properties](#data-driven-properties)
- [Release Notes](#release-notes)
  - [0.9.0-SNAPSHOT](#090-snapshot)
  - [0.8.x](#08x)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## How to Use

__jqwik__ is an alternative test engine for the
[JUnit 5 platform](http://junit.org/junit5/docs/current/api/org/junit/platform/engine/TestEngine.html).
That means that you can use it either stand-alone or combine it with any other JUnit 5 engine, e.g. 
[Jupiter (the standard engine)](http://junit.org/junit5/docs/current/user-guide/#dependency-metadata-junit-jupiter) or 
[Vintage (aka JUnit 4)](http://junit.org/junit5/docs/current/user-guide/#dependency-metadata-junit-vintage).
All you have to do is add all needed engines to your `testCompile` dependencies as shown in the
[gradle file](#gradle) below.

The latest release of __jqwik__ is deployed to [Maven Central](https://mvnrepository.com/).

Snapshot releases can be fetched from https://oss.sonatype.org/content/repositories/snapshots.



### Gradle

To use __jqwik__ in a gradle-based project add the following stuff to your `build.gradle` file:

#### Using JUnit's own Gradle Plugin

```
buildscript {
	dependencies {
	    ...
		classpath 'org.junit.platform:junit-platform-gradle-plugin:1.1.1'
	}
}

apply plugin: 'org.junit.platform.gradle.plugin'

repositories {
    ...
    mavenCentral()
    
    # For snapshot releases only:
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }

}

ext.junitPlatformVersion = '1.2.0'
ext.junitJupiterVersion = '5.2.0'

ext.jqwikVersion = '0.8.15'
#ext.jqwikVersion = '0.8.16-SNAPSHOT'

junitPlatform {
	filters {
		includeClassNamePattern '.*Test'
		includeClassNamePattern '.*Tests'
		includeClassNamePattern '.*Properties'
	}
	// Only enable if you also want to run tests outside the junit platform runner:
	enableStandardTestTask false
}

dependencies {
    ...

    // Needed to enable the platform to run tests at all
    testCompile("org.junit.platform:junit-platform-launcher:${junitPlatformVersion}")
    
    // jqwik dependency
    testCompile "net.jqwik:jqwik:${jqwikVersion}"
    
    // Add if you want to also use the Jupiter engine
    // Also add if you use IntelliJ 2017.2 or older to enable JUnit-5 support
    testCompile("org.junit.jupiter:junit-jupiter-engine:${junitJupiterVersion}")
    
    // You'll probably need some assertions
    testCompile("org.assertj:assertj-core:3.9.1")

}
```

See [the Gradle section in JUnit 5's user guide](http://junit.org/junit5/docs/current/user-guide/#running-tests-build-gradle)
for more details on how to configure test execution.

#### Using Gradle's Built-in Support

Since version 4.6, Gradle has 
[built-in support for the JUnit platform](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html).
In its current state I do not recommend it for use with _jqwik_ because some of the 
[important information is not reported](https://github.com/gradle/gradle/issues/4605)
by Gradle. Just wait till they fix it.

### Maven

Configure the surefire plugin as described in 
[the Maven section in JUnit 5's user guide](http://junit.org/junit5/docs/current/user-guide/#running-tests-build-maven)
and add the following dependency to your `pom.xml` file:

```
<dependencies>
    ...
    <dependency>
        <groupId>net.jqwik</groupId>
        <artifactId>jqwik</artifactId>
        <version>0.8.15</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```


### Snapshot Releases

Snapshot releases are available through Sonatype's 
[snapshot repositories](#https://oss.sonatype.org/content/repositories/snapshots).

Adding 

```
https://oss.sonatype.org/content/repositories/snapshots
``` 

as a maven repository
will allow you to use _jqwik_'s snapshot release which contains all the latest features.

### Project without Build Tool

I've never tried it but using jqwik without gradle or some other tool to manage dependencies should also work.
You will have to add _at least_ the following jars to your classpath:

- `jqwik-0.8.15.jar`
- `junit-platform-engine-1.2.0.jar`
- `junit-platform-commons-1.2.0.jar`
- `opentest4j-1.0.0.jar`
- `assertj-core-3.9.x.jar` in case you need assertion support

## Creating an Example-based Test

Just annotate a `public`, `protected` or package-scoped method with [`@Example`](https://jqwik.net/javadoc/net/jqwik/api/Example.html).
Example-based tests work just like plain JUnit-style test cases and
are not supposed to take any parameters.

A test case method must
- either return a `boolean` value that signifies success (`true`)
  or failure (`false`) of this test case.
- or return nothing (`void`) in which case you will probably
  use [assertions](#assertions) in order to verify the test condition.
  
[Here](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/ExampleBasedTests.java) 
is a test class with two example-based tests:

```java
import static org.assertj.core.api.Assertions.*;

import net.jqwik.api.*;
import org.assertj.core.data.*;

class ExampleBasedTests {
	
	@Example
	void squareRootOf16is4() { 
		assertThat(Math.sqrt(16)).isCloseTo(4.0, Offset.offset(0.01));
	}

	@Example
	boolean add1plu3is4() {
		return (1 + 3) == 4;
	}
}
```

## Creating a Property

_Properties_ are the core concept of [property-based testing](/#properties).

You create a _Property_ by annotating a `public`, `protected` 
or package-scoped method with 
[`@Property`](https://jqwik.net/javadoc/net/jqwik/api/Property.html). 
In contrast to examples a property method is supposed to have one or
more parameters, all of which must be annotated with 
[`@ForAll`](https://jqwik.net/javadoc/net/jqwik/api/ForAll.html).

At test runtime the exact parameter values of the property method
will be filled in by _jqwik_.

Just like an example test a property method has to 
- either return a `boolean` value that signifies success (`true`)
  or failure (`false`) of this property.
- or return nothing (`void`). In that case you will probably
  use [assertions](#assertions) to check the property's invariant.

If not [specified differently](#optional-property-parameters), 
_jqwik_ __will run 1000 tries__, i.e. a 1000 different sets of 
parameter values and execute the property method with each of those parameter sets. 
The first failed execution will stop value generation 
and be reported as failure - usually followed by an attempt to 
[shrink](#result-shrinking) the falsified parameter set.

[Here](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/PropertyBasedTests.java) 
are two properties whose failures might surprise you:

```java
import net.jqwik.api.*;
import org.assertj.core.api.*;

class PropertyBasedTests {

	@Property
	boolean absoluteValueOfAllNumbersIsPositive(@ForAll int anInteger) {
		return Math.abs(anInteger) >= 0;
	}

	@Property
	void lengthOfConcatenatedStringIsGreaterThanLengthOfEach(
		@ForAll String string1, @ForAll String string2
	) {
		String conc = string1 + string2;
		Assertions.assertThat(conc.length()).isGreaterThan(string1.length());
		Assertions.assertThat(conc.length()).isGreaterThan(string2.length());
	}
}
```

Currently _jqwik_ cannot deal with parameters that are not
annotated with '@ForAll'. However, this might change
in future versions.

### Optional `@Property` Parameters

The [`@Property`](https://jqwik.net/javadoc/net/jqwik/api/Property.html) 
annotation has a few optional values:

- `int tries`: The number of times _jqwik_ tries to generate parameter values for this method.
  
  The default is `1000` which can be overridden in [`jqwik.properties`](#jqwik-configuration).

- `String seed`: The _random seed_ to use for generating values. If you do not specify a values
  _jqwik_ will use a random _random seed_. The actual seed used is being reported by 
  each run property.

- `int maxDiscardRatio`: The maximal number of tried versus actually checked property runs
  in case you are using [Assumptions](#assumptions). If the ratio is exceeded _jqwik_ will
  report this property as a failure. 
  
  The default is `5` which can be overridden in [`jqwik.properties`](#jqwik-configuration).

- `ShrinkingMode shrinking`: You can influence the way shrinking is done
  - `ShrinkingMode.OFF`: No shrinking at all
  - `ShrinkingMode.FULL`: Shrinking continues until no smaller value can
    be found that also falsifies the property.
    This might take very long or not end at all in rare cases.
  - `ShrinkingMode.BOUNDED`: Shrinking is tried to a depth of 1000 steps
    maximum per value. This is the default.

  Most of the time you want to stick with the default. Only if
  bounded shrinking is reported - look at a falsified property's output! -
  should you try with `ShrinkingMode.FULL`.

- `Reporting[] reporting`: You can switch on additional reporting aspects. 
  by specifying one or more of the following `Reporting` values:
  - `Reporting.GENERATED` will report each generated set of parameters.
  - `Reporting.FALSIFIED` will report _some_ falsified sets of parameters
    during shrinking.
  
  The default is _no_ additional reporting aspects are switched on.

## Assertions

__jqwik__ does not come with any assertions, so you have to use one of the
third-party assertion libraries, e.g. [Hamcrest](http://hamcrest.org/) or 
[AssertJ](http://joel-costigliola.github.io/assertj/). 

If you have Jupiter in your test dependencies anyway, you can also use the
static methods in `org.junit.jupiter.api.Assertions`.

## Lifecycle

### Method Lifecycle

The current lifecycle of jqwik test methods is rather simple:

- For each method, annotated with 
  [`@Property`](https://jqwik.net/javadoc/net/jqwik/api/Property.html) 
  or [`@Example`](https://jqwik.net/javadoc/net/jqwik/api/Example.html), 
  a new instance of the containing test class is created
  in order to keep the individual tests isolated from each other.
- If you have preparatory work to do for each method, 
  create a constructor without parameters and do the work there.
- If you have cleanup work to do for each method, 
  the containing test class can implement `java.lang.AutoCloseable`.
  The `close`-Method will be called after each test method execution.
  
```java
import net.jqwik.api.*;

class TestsWithLifecycle implements AutoCloseable {

	TestsWithLifecycle() {
		System.out.println("Before each");
	}

	@Example void anExample() {
		System.out.println("anExample");
	}

	@Property(tries = 5)
	void aProperty(@ForAll String aString) {
		System.out.println("anProperty: " + aString);
	}

	@Override
	public void close() throws Exception {
		System.out.println("After each");
	}
}
```

[In this example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/TestsWithLifecycle.java)
both the constructor and `close()` will be called twice times: 
Once for `anExample()` and once for `aProperty(...)`.

### Other Lifecycles

Currently _jqwik_ does not have special support for a lifecycle per test container,
per test try or even package. Later versions of _jqwik_ might possible bring
more features in that field. 
[Create an issue on github](https://github.com/jlink/jqwik/issues) with your concrete needs.


## Grouping Tests

Within a containing test class you can group other containers by embedding
another non-static and non-private inner class and annotating it with `@Group`.
Grouping examples and properties is a means to improve the organization and 
maintainability of your tests.

Groups can be nested and there lifecycle is also nested, that means that
the lifecycle of a test class is also applied to inner groups of that container.
Have a look at [this example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/TestsWithGroups.java):

```java
import net.jqwik.api.*;

class TestsWithGroups {

	@Property
	void outer(@ForAll String aString) {
	}

	@Group
	class Group1 {
		@Property
		void group1Property(@ForAll String aString) {
		}

		@Group
		class Subgroup {
			@Property
			void subgroupProperty(@ForAll String aString) {
			}
		}
	}

	@Group
	class Group2 {
		@Property
		void group2Property(@ForAll String aString) {
		}
	}
}
```

## Labeling Tests

Test container classes, groups, example methods and property methods can be labeled
using the annotation `@Label("a label")`. This label will be used to display the element
in test reports or within the IDE. 
[In the following example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/LabelingExamples.java), 
every test relevant element has been labeled:

```java
@Label("Labeling")
class LabelingExamples {

	@Property
	@Label("a property")
	void aPropertyWithALabel() { }

	@Group
	@Label("A Group")
	class GroupWithLabel {
		@Example
		@Label("an example with äöüÄÖÜ")
		void anExampleWithALabel() { }
	}
}
```

Labels can consist of any characters and don't have to be unique - but you probably want them 
to be unique within their container.

## Tagging Tests

Test container classes, groups, example methods and property methods can be tagged
using the annotation `@Tag("a-tag")`. You can have many tags on the same element.

Those tag can be used to filter the set of tests 
[run by the IDE](https://blog.jetbrains.com/idea/2018/01/intellij-idea-starts-2018-1-early-access-program/) or 
[the build tool](https://docs.gradle.org/4.6/release-notes.html#junit-5-support).
Tags are handed down from container (class or group) to its children (test methods or groups).

Have a look at 
[the following example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/TaggingExamples.java). 
Including the tag `integration-test` will include
all tests of the class.

```java
@Tag("integration-test")
class TaggingExamples {

	@Property
	@Tag("fast")
	void aFastProperty() { }

	@Example
	@Tag("slow") @Tag("involved")
	void aSlowTest() { }
}
```

Tags must follow certain rules as described 
[here](https://jqwik.net/javadoc/net/jqwik/api/Tag.html)

## Default Parameter Generation

_jqwik_ tries to generate values for those property method parameters that are
annotated with [`@ForAll`](https://jqwik.net/javadoc/net/jqwik/api/ForAll.html). If the annotation does not have a `value` parameter,
jqwik will use default generation for the following types:

- `Object`
- `String`
- Integral types `Byte`, `byte`, `Short`, `short` `Integer`, `int`, `Long`, `long` and `BigInteger`
- Floating types  `Float`, `float`, `Double`, `double` and `BigDecimal`
- `Boolean` and `boolean`
- `Character` and `char`
- All `enum` types
- Collection types `List<T>`, `Set<T>` and `Stream<T>` 
  as long as `T` can also be provided by default generation.
- `Optional<T>` of types that are provided by default.
- Array `T[]` of types that are provided by default.
- `java.util.Random`

If you use [`@ForAll`](https://jqwik.net/javadoc/net/jqwik/api/ForAll.html) 
with a value, e.g. `@ForAll("aMethodName")`, the method
referenced by `"aMethodName"` will be called to provide an Arbitrary of the 
required type (see [Customized Parameter Generation](#customized-parameter-generation)). 

### Constraining Default Generation

Default parameter generation can be influenced and constrained by additional annotations, 
depending on the requested parameter type.

#### Allow Null Values

- [`@WithNull(double value = 0.1)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/WithNull.html): 
  Inject `null` into generated values with a probability of `value`. 
  
  Works for all generated types.
   
#### Unique Values

- [`@Unique`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Unique.html):
  Prevent duplicate values to be generated _per try_. That means that
  there can still be duplicate values across several tries. That also means
  that `@Unique` only makes sense as annotation for a parameter type, e.g.:

  ```java
    @Property
    void uniqueInList(@ForAll @Size(5) List<@IntRange(min = 0, max = 10) @Unique Integer> aList) {
        Assertions.assertThat(aList).doesNotHaveDuplicates();
        Assertions.assertThat(aList).allMatch(anInt -> anInt >= 0 && anInt <= 10);
    }
  ```

  Trying to generate a list with more than 11 elements would not work here.

  Works for all generated types.

#### String Length

- [`@StringLength(int value = 0, int min = 0, int max = 0)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/StringLength.html):
  Set either fixed length through `value` or configure the length range between `min` and `max`.

#### Character Sets

When generating chars any unicode character might be generated.

When generating Strings, however,
Unicode "noncharacters" and "private use characters"
will not be generated unless you explicitly include them using
`@Chars` or `@CharRange` (see below).

You can use the following annotations to restrict the set of allowed characters and even
combine several of them:

- [`@Chars(chars[] value = {})`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Chars.html):
  Specify a set of characters.
  This annotation can be repeated which will add up all allowed chars.
- [`@CharRange(char from = 0, char to = 0)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/CharRange.html):
  Specify a start and end character.
  This annotation can be repeated which will add up all allowed chars.
- [`@NumericChars`](https://jqwik.net/javadoc/net/jqwik/api/constraints/NumericChars.html):
  Use digits `0` through `9`
- [`@LowerChars`](https://jqwik.net/javadoc/net/jqwik/api/constraints/LowerChars.html):
  Use lower case chars `a` through `z`
- [`@UpperChars`](https://jqwik.net/javadoc/net/jqwik/api/constraints/UpperChars.html):
  Use upper case chars `A` through `Z`
- [`@AlphaChars`](https://jqwik.net/javadoc/net/jqwik/api/constraints/AlphaChars.html):
  Lower and upper case chars are allowed.
- [`@Whitespace`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Whitespace.html):
  All whitespace characters are allowed.

They work for generated `String`s and `Character`s.

#### List, Set, Stream and Array Size:

- [`@Size(int value = 0, int min = 0, int max = 0)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Size.html): 
  Set either fixed size through `value` or configure the size range between `min` and `max`.


#### Integer Constraints

- [`@ByteRange(byte min = 0, byte max)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/ByteRange.html):
  For `Byte` and `byte` only.
- [`@ShortRange(short min = 0, short max)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/ShortRange.html):
  For `Short` and `short` only.
- [`@IntRange(int min = 0, int max)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/IntRange.html):
  For `Integer` and `int` only.
- [`@LongRange(long min = 0L, long max)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/LongRange.html):
  For `Long` and `long` only.
- [`@BigRange(String min = "", String max = "")`](https://jqwik.net/javadoc/net/jqwik/api/constraints/BigRange.html):
  For `BigInteger` generation.
- [`@Positive`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Positive.html):
  Numbers equal to or larger than `0`. For all integral types.
- [`@Negative`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Negative.html):
  Numbers lower than or equal to `-0`. For all integral types.


#### Decimal Constraints

- [`@FloatRange(float min = 0.0f, float max)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/FloatRange.html):
  For `Float` and `float` only.
- [`@DoubleRange(double min = 0.0, double max)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/DoubleRange.html):
  For `Double` and `double` only.
- [`@BigRange(String min = "", String max = "")`](https://jqwik.net/javadoc/net/jqwik/api/constraints/BigRange.html):
  For `BigDecimal` generation.
- [`@Scale(int value)`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Scale.html):
  Specify the maximum number of decimal places. For all decimal types.
- [`@Positive`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Positive.html):
  Numbers equal to or larger than `0.0`. For all decimal types.
- [`@Negative`](https://jqwik.net/javadoc/net/jqwik/api/constraints/Negative.html):
  Numbers lower than or equal to `-0.0`. For all decimal types.

### Constraining parameterized types

When you want to constrain the generation of contained parameter types you can annotate 
the parameter type directly, e.g.:

```java
@Property
void aProperty(@ForAll @Size(min= 1) List<@StringLength(max=10) String> listOfStrings) {
}
```
will generate lists with a minimum size of 1 filled with Strings that have 10 characters max.

### Providing variable types

While checking properties of generically typed classes or functions, you often don't care
about the exact type of variables and therefore want to express them with type variables.
_jqwik_ can also handle type variables and wildcard types. The handling of upper and lower
bounds works mostly as you would expect it.

Consider
[the following examples](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/VariableTypedPropertyExamples.java):

```java
class VariableTypedPropertyExamples {

	@Property
	<T> boolean unboundedGenericTypesAreResolved(@ForAll List<T> items, @ForAll T newItem) {
		items.add(newItem);
		return items.contains(newItem);
	}

	@Property(reporting = Reporting.GENERATED)
	<T extends Serializable & Comparable> void someBoundedGenericTypesCanBeResolved(@ForAll List<T> items, @ForAll T newItem) {
	}

	@Property(reporting = Reporting.GENERATED)
	void someWildcardTypesWithUpperBoundsCanBeResolved(@ForAll List<? extends Serializable> items) {
	}

}
```

In the case of unbounded type variables or an unbounded wildcard type, _jqwik_
will create instanced of a special class (`WildcardObject`) under the hood.

In the case of bounded type variables and bounded wildcard types, _jqwik_
will check if any [registered arbitrary provider](#providing-default-arbitraries)
can provide suitable arbitraries and choose randomly between those.

There is, however, a potentially unexpected behaviour,
when the same type variable is used in more than one place and can be
resolved by more than one arbitrary. In this case it can happen that the variable
does not represent the same type in all places. You can see this above
in property method `someBoundedGenericTypesCanBeResolved()` where `items`
might be a list of Strings but `newItem` of some number type - and all that
_in the same call to the method_!

## Self-Made Annotations

You can [make your own annotations](http://junit.org/junit5/docs/5.0.0/user-guide/#writing-tests-meta-annotations)
instead of using _jqwik_'s built-in ones. BTW, '@Example' is nothing but a plain annotation using [`@Property`](https://jqwik.net/javadoc/net/jqwik/api/Property.html)
as "meta"-annotation.

The following example provides an annotation to constrain String or Character generation to German letters only:

```java
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Digits
@AlphaChars
@Chars({'ä', 'ö', 'ü', 'Ä', 'Ö', 'Ü', 'ß'})
@Chars({' ', '.', ',', ';', '?', '!'})
@StringLength(min = 10, max = 100)
public @interface GermanText { }

@Property(tries = 10, reporting = Reporting.GENERATED)
void aGermanText(@ForAll @GermanText String aText) {}
```

The drawback of self-made annotations is that they do not forward their parameters to meta-annotations,
which constrains their applicability to simple cases.


## Customized Parameter Generation

Sometimes the possibilities of adjusting default parameter generation
through annotations is not enough. In that case you can delegate parameter
provision to another method. Look at the 
[following example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/ProvideMethodExamples.java):

```java
@Property
boolean concatenatingStringWithInt(
    @ForAll("shortStrings") String aShortString,
    @ForAll("10 to 99") int aNumber
) {
    String concatenated = aShortString + aNumber;
    return concatenated.length() > 2 && concatenated.length() < 11;
}

@Provide
Arbitrary<String> shortStrings() {
    return Arbitraries.strings().withCharRange('a', 'z')
        .ofMinLength(1).ofMaxLength(8);
}

@Provide("10 to 99")
Arbitrary<Integer> numbers() {
    return Arbitraries.integers().between(10, 99);
}
```

The String value of the [`@ForAll`](https://jqwik.net/javadoc/net/jqwik/api/ForAll.html) 
annotation serves as a reference to a 
method within the same class (or one of its superclasses or owning classes).
This reference refers to either the method's name or the String value
of the method's `@Provide` annotation.

The providing method has to return an object of type 
[`@Arbitrary<T>`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html) 
where `T` is the static type of the parameter to be provided. 

Parameter provision usually starts with a 
[static method call to `Arbitraries`](#static-arbitraries-methods), maybe followed
by one or more [filtering](#filtering), [mapping](#mapping) or 
[combining](#combining-arbitraries) actions.

For types that have no default generation at all, _jqwik_ will use
any provider method returning the correct type even if there is no
explicit reference value in [`@ForAll`](https://jqwik.net/javadoc/net/jqwik/api/ForAll.html). If provision is ambiguous
_jqwik_ will complain and throw an exception at runtime. 


### Static `Arbitraries` methods 

The starting point for generation usually is a static method call on class 
[`Arbitraries`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html). 

#### Generate values yourself

- [`Arbitrary<T> randomValue(Function<Random, T> generator)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#randomValue-java.util.function.Function-): 
  Take a `random` instance and create an object from it.
  Those values cannot be [shrunk](#result-shrinking), though.
  
  Generating prime numbers might look like that:
  ```java
  @Provide
  Arbitrary<Integer> primesGenerated() {
      return Arbitraries.randomValue(random -> generatePrime(random));
  }

  private Integer generatePrime(Random random) {
      int candidate;
      do {
          candidate = random.nextInt(10000) + 2;
      } while (!isPrime(candidate));
      return candidate;
  }
  ```

- [`Arbitrary<T> fromGenerator(RandomGenerator<T> generator)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#fromGenerator-net.jqwik.api.RandomGenerator-):
  If the number of _tries_ influences value generation or if you want 
  to allow for [shrinking](#result-shrinking) you have to provide 
  your own `RandomGenerator` implementation. 
  
#### Select values randomly

- [`Arbitrary<U> of(U... values)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#of-U...-):
  Choose randomly from a list of values. Shrink towards the first one.
  
- [`Arbitrary<T> samples(T... samples)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#samples-T...-):
  Go through samples from first to last. Shrink towards the first sample.
  
  If instead you want to _add_ samples to an existing arbitrary you'd rather use 
  [`Arbitrary.withSamples(T... samples)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#withSamples-T...-).
  The following arbitrary:
  
  ```java
  @Provide 
  Arbitrary<Integer> integersWithPrimes() {
	  return Arbitraries.integers(- 1000, 1000).withSamples(2,3,5,7,11,13,17);
  }
  ```
  
  will first generate the 7 enumerated prime numbers and only then generate random 
  integers between -1000 and +1000.
  
- [`Arbitrary<T> of(Class<T  extends Enum> enumClass)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#of-java.lang.Class-):
  Choose randomly from all values of an `enum`. Shrink towards first enum value.

#### Select randomly with Weights

If you have a set of values to choose from with weighted probabilities, use 
[`Arbitraries.frequency(...)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#frequency-net.jqwik.api.Tuple.Tuple2...-):

```java
@Property
void abcdWithFrequencies(@ForAll("abcdWeighted") String aString) {
    Statistics.collect(aString);
}

@Provide
Arbitrary<String> abcdWeighted() {
    return Arbitraries.frequency(
        Tuple.of(1, "a"),
        Tuple.of(5, "b"),
        Tuple.of(10, "c"),
        Tuple.of(20, "d")
    );
}
```

The first value of the tuple specifies the frequency of a particular value in relation to the
sum of all frequencies. In 
[the given example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/ChoosingExamples.java#L17)
the sum is 36, thus `"a"` will be generated with a probability of `1/36` 
whereas `"d"` has a generation probability of `20/36` (= `5/9`).

Shrinking moves towards the start of the frequency list.

#### Integers

- [`ByteArbitrary bytes()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#bytes--)
- [`ShortArbitrary shorts()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#shorts--)
- [`IntegerArbitrary integers()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#integers--)
- [`LongArbitrary longs()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#longs--)
- [`BigIntegerArbitrary bigIntegers()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#bigIntegers--)

#### Decimals

- [`FloatArbitrary floats()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#floats--)
- [`DoubleArbitrary doubles()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#doubles--)
- [`BigDecimalArbitrary bigDecimals()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#bigDecimals--)

#### Characters and Strings

- [`StringArbitrary strings()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#strings--)
- [`CharacterArbitrary chars()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#chars--)

#### java.util.Random

- [`Arbitrary<Random> randoms()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#randoms--): 
  Random instances will never be shrunk

#### Constants

- [`Arbitrary<T> constant(T value)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#constant-T-): 
  Always return an unshrinkable `value` of type `T`.

#### Default Types

- [`Arbitrary<T> defaultFor(Class<T> type, Class<?> ... parameterTypes)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#defaultFor-java.lang.Class-java.lang.Class...-): 
  Return the default arbitrary available for type `type` [if one is provided](#providing-default-arbitraries)
  by default. For parameterized types you can also specify the parameter types. 
  
  Keep in mind, though, that the parameter types are lost in the type signature and therefore
  cannot be used in the respective [`@ForAll`](https://jqwik.net/javadoc/net/jqwik/api/ForAll.html) property method parameter. Raw types and wildcards, 
  however, match; thus the following example will work:
  
  ```java
  @Property
  boolean listWithWildcard(@ForAll("stringLists") List<?> stringList) {
      return stringList.isEmpty() || stringList.get(0) instanceof String;
  }
   
  @Provide
  Arbitrary<List> stringLists() {
      return Arbitraries.defaultFor(List.class, String.class);
  }
  ```

### Collections, Streams, Arrays and Optional

Generating types who have generic type parameters, requires to start with 
an `Arbitrary` instance for the generic type. You can create the corresponding collection arbitrary from there:

- [`Arbitrary.list()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#list--)
- [`Arbitrary.set()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#set--)
- [`Arbitrary.streamOf()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#stream--)
- [`Arbitrary.array(Class<A> arrayClass)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#array-java.lang.Class-)
- [`Arbitrary.optional()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#optional--)

### Fluent Configuration Interfaces

Most specialized arbitrary interfaces provide special methods to configure things
like size, length, boundaries etc. Have a look at the Java doc for the following types:

- [BigDecimalArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/BigDecimalArbitrary.html)
- [BigIntegerArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/BigIntegerArbitrary.html)
- [ByteArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/ByteArbitrary.html)
- [CharacterArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/CharacterArbitrary.html)
- [DoubleArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/DoubleArbitrary.html)
- [FloatArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/FloatArbitrary.html)
- [IntegerArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/IntegerArbitrary.html)
- [LongArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/LongArbitrary.html)
- [ShortArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/ShortArbitrary.html)
- [SizableArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/SizableArbitrary.html)
- [StringArbitrary](https://jqwik.net/javadoc/net/jqwik/api/arbitraries/StringArbitrary.html)


Here are a 
[two examples](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/FluentConfigurationExamples.java) 
to give you a hint of what you can do:

```java
@Provide
Arbitrary<String> alphaNumericStringsWithMinLength5() {
    return Arbitraries.strings().ofMinLength(5).alpha().numeric();
}

@Provide
Arbitrary<List<Integer>> fixedSizedListOfPositiveIntegers() {
    return Arbitraries.integers().greaterOrEqual(0).list().ofSize(17);
}
```

### Generate `null` values

Predefined generators will never create `null` values. If you want to allow that,
call [`Arbitrary.injectNull(double probability)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#injectNull-double-). 
The following provider method creates an arbitrary that will return a `null` String 
in about 1 of 100 generated values.

```java
@Provide 
Arbitrary<String> stringsWithNull() {
  return Arbitraries.strings(0, 10).injectNull(0.01);
}
```

### Filtering

If you want to include only part of all the values generated by an arbitrary,
use 
[`Arbitrary.filter(Predicate<T> filterPredicate)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#filter-java.util.function.Predicate-). 
The following arbitrary will filter out all
even numbers from the stream of generated integers:

```java
@Provide 
Arbitrary<Integer> oddNumbers() {
  return Arbitraries.integers().filter(aNumber -> aNumber % 2 != 0);
}
```

Keep in mind that your filter condition should not be too restrictive. 
If the generator fails to find a suitable value after 10000 trials,
the current property will be abandoned by throwing an exception.

### Creating unique values

If you want to make sure that all the values generated by an arbitrary are unique,
use
[`Arbitrary.unique()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#unique--).
The following arbitrary will generate integers between 1 and 1000 but never the same integer twice:

```java
@Provide
Arbitrary<Integer> oddNumbers() {
  return Arbitraries.integers().between(1, 1000).unique();
}
```

This means that a maximum of 1000 values can be generated. If the generator fails
to find a yet unseen value after 10000 trials,
the current property will be abandoned by throwing an exception.

### Mapping

Sometimes it's easier to start with an existing arbitrary and use its generated values to
build other objects from them. In that case, use 
[`Arbitrary.map(Function<T, U> mapper)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#map-java.util.function.Function-).
The following example uses generated integers to create numerical Strings: 

```java
@Provide 
Arbitrary<String> fiveDigitStrings() {
  return Arbitraries.integers(10000, 99999).map(aNumber -> String.valueOf(aNumber));
}
```

You could generate the same kind of values by constraining and filtering a generated String.
However, the [shrinking](#result-shrinking) target would probably be different. In the example above, shrinking
will move towards the lowest allowed number, that is `10000`.


### Flat Mapping

Similar as in the case of `Arbitrary.map(..)` there are situations in which you want to use
a generated value in order to create another Arbitrary from it. Sounds complicated?
Have a look at the 
[following example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/FlatMappingExamples.java#L26):

```java
@Property
boolean fixedSizedStrings(@ForAll("listsOfEqualSizedStrings")List<String> lists) {
    return lists.stream().distinct().count() == 1;
}

@Provide
Arbitrary<List<String>> listsOfEqualSizedStrings() {
    Arbitrary<Integer> integers2to5 = Arbitraries.integers().between(2, 5);
    return integers2to5.flatMap(stringSize -> {
        Arbitrary<String> strings = Arbitraries.strings() //
                .withCharRange('a', 'z') //
                .ofMinLength(stringSize).ofMaxLength(stringSize);
        return strings.list();
    });
}
```
The provider method will create random lists of strings, but in each list the size of the contained strings
will always be the same - between 2 and 5.

### Flat Mapping with Tuple Types

In the example above you used a generated value in order to create another arbitrary.
In those situations you often want to also provide the original values to your property test.

Imagine, for instance, that you'd like to test properties of `String.substring(begin, end)`.
To randomize the method call, you not only need a string but also the `begin` and `end` indices.
However, both have dependencies:
- `end` must not be larger than the string size
- `begin` must not be larger than `end`
You can make _jqwik_ create all three values by using 
[`flatMap`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#flatMap-java.util.function.Function-) 
combined with a tuple type 
[like this](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/FlatMappingExamples.java#L32):


```java
@Property(reporting = Reporting.GENERATED)
void substringLength(@ForAll("stringWithBeginEnd") Tuple3<String, Integer, Integer> stringBeginEnd) {
    String aString = stringBeginEnd.get1();
    int begin = stringBeginEnd.get2();
    int end = stringBeginEnd.get3();
    Assertions.assertThat(aString.substring(begin, end).length())
        .isEqualTo(end - begin);
}

@Provide
Arbitrary<Tuple3<String, Integer, Integer>> stringWithBeginEnd() {
    Arbitrary<String> stringArbitrary = Arbitraries.strings() //
            .withCharRange('a', 'z') //
            .ofMinLength(2).ofMaxLength(20);
    return stringArbitrary //
            .flatMap(aString -> Arbitraries.integers().between(0, aString.length()) //
                    .flatMap(end -> Arbitraries.integers().between(0, end) //
                            .map(begin -> Tuple.of(aString, begin, end))));
}
```

Mind the nested flat mapping, which is an aesthetic nuisance but nevertheless
very useful. 

### Randomly Choosing among Arbitraries

If you have several arbitraries of the same type, you can create a new arbitrary of
the same type which will choose randomly one of those arbitraries before generating
a value:

```java
@Property
boolean intsAreCreatedFromOneOfThreeArbitraries(@ForAll("oneOfThree") int anInt) {
    String classifier = anInt < -1000 ? "below" : anInt > 1000 ? "above" : "one";
    Statistics.collect(classifier);
    
    return anInt < -1000 //
            || Math.abs(anInt) == 1 //
            || anInt > 1000;
}

@Provide
Arbitrary<Integer> oneOfThree() {
    IntegerArbitrary below1000 = Arbitraries.integers().between(-2000, -1001);
    IntegerArbitrary above1000 = Arbitraries.integers().between(1001, 2000);
    Arbitrary<Integer> oneOrMinusOne = Arbitraries.samples(-1, 1);
    
    return Arbitraries.oneOf(below1000, above1000, oneOrMinusOne);
}
```

[In this example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/OneOfExamples.java)
the statistics should also give you an equal distribution between
the three types of integers.

### Combining Arbitraries

Sometimes just mapping a single stream of generated values is not enough to generate
a more complicated domain object. In those cases you can combine several arbitraries to
a single result arbitrary using 
[`Combinators.combine()`](https://jqwik.net/javadoc/net/jqwik/api/Combinators.html#combine-net.jqwik.api.Arbitrary-net.jqwik.api.Arbitrary-) 
with up to eight arbitraries. 
[Create an issue on github](https://github.com/jlink/jqwik/issues) if you need more than eight. 

[The following example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/MappingAndCombinatorExamples.java#L25) 
generates `Person` instances from three arbitraries as inputs.

```java
@Property
void validPeopleHaveIDs(@ForAll Person aPerson) {
    Assertions.assertThat(aPerson.getID()).contains("-");
    Assertions.assertThat(aPerson.getID().length()).isBetween(5, 24);
}

@Provide
Arbitrary<Person> validPeople() {
    Arbitrary<Character> initials = Arbitraries.chars('A', 'Z');
    Arbitrary<String> names = Arbitraries.strings().withCharRange('a', 'z')
        .ofMinLength(2).ofMaxLength(20);
    Arbitrary<Integer> ages = Arbitraries.integers().between(0, 130);
    return Combinators.combine(initials, names, ages)
        .as((initial, name, age) -> new Person(initial + name, age));
}

class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getID() {
        return name + "-" + age;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", name, age);
    }
}
```

The property should fail, thereby shrinking the falsified Person instance to
`[Aaaaaaaaaaaaaaaaaaaaa:100]`.

#### Flat Combination

If generating domain values requires to use several generated values to be used
in generating another one, there's the combination of flat mapping and combining:

```java
@Property
boolean fullNameHasTwoParts(@ForAll("fullName") String aName) {
    return aName.split(" ").length == 2;
}

@Provide
Arbitrary<String> fullName() {
    IntegerArbitrary firstNameLength = Arbitraries.integers().between(2, 10);
    IntegerArbitrary lastNameLength = Arbitraries.integers().between(2, 10);
    return Combinators.combine(firstNameLength, lastNameLength).flatAs( (fLength, lLength) -> {
        Arbitrary<String> firstName = Arbitraries.strings().alpha().ofLength(fLength);
        Arbitrary<String> lastName = Arbitraries.strings().alpha().ofLength(fLength);
        return Combinators.combine(firstName, lastName).as((f,l) -> f + " " + l);
    });
}
```

Often, however, there's an easier way to achieve the same goal which
does not require the flat combination of arbitraries:

```java
@Provide
Arbitrary<String> fullName2() {
    Arbitrary<String> firstName = Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10);
    Arbitrary<String> lastName = Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10);
    return Combinators.combine(firstName, lastName).as((f, l) -> f + " " + l);
}
```

This is not only easier to understand but it usually improves shrinking.

### Fix an Arbitrary's `genSize`

Some generators (e.g. most number generators) are sensitive to the 
`genSize` value that is used when creating them. 
The default value for `genSize` is the number of tries configured for the property
they are used in. If there is a need to influence the behaviour of generators
you can do so by using 
[`Arbitrary.fixGenSize(int)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitrary.html#fixGenSize-int-)..

## Recursive Arbitraries

Sometimes it seems like a good idea to compose arbitraries and thereby
recursively calling an arbitrary creation method. Generating recursive data types
is one application field but you can also use it for other stuff. 

### Probabilistic Recursion

Look at the 
[following example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/RecursiveExamples.java) 
which generates sentences by recursively adding words to a sentence:

```java
@Property(reporting = Reporting.GENERATED)
boolean sentencesEndWithAPoint(@ForAll("sentences") String aSentence) {
    return aSentence.endsWith(".");
}

@Provide
Arbitrary<String> sentences() {
    Arbitrary<String> sentence = Combinators.combine( //
        Arbitraries.lazy(this::sentences), //
        word() //
    ).as((s, w) -> w + " " + s);
    return Arbitraries.oneOf( //
        word().map(w -> w + "."), //
        sentence, //
        sentence, //
        sentence //
    );
}

private StringArbitrary word() {
    return Arbitraries.strings().alpha().ofLength(5);
}
``` 

There are two things to which you must pay attention:

- Use [`Arbitraries.lazy(Supplier<Arbitrary<T>>)`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#lazy-java.util.function.Supplier-) 
  to wrap the recursive call itself. 
  Otherwise _jqwik_'s attempt to build the arbitrary will quickly result in a stack overflow.
- Every recursion needs one or more base cases in order to stop recursion at some point. 
  Base cases must have a high enough probability, 
  otherwise a stack overflow will get you during value generation.
  
### Deterministic Recursion

An alternative to the non-deterministic recursion shown above, is to use classical
recursion with a counter to determine the base case. If you then use an arbitrary value
for the counter, the generated sentences will be very similar, and there is _no need_
for using `Arbitraries.lazy()` at all:

```java
@Property(tries = 10, reporting = Reporting.GENERATED)
boolean sentencesEndWithAPoint_2(@ForAll("deterministic") String aSentence) {
    return aSentence.endsWith(".");
}

@Provide
Arbitrary<String> deterministic() {
    Arbitrary<Integer> length = Arbitraries.integers().between(1, 10);
    Arbitrary<String> lastWord = word().map(w -> w + ".");
    return length.flatMap(l -> deterministic(l, lastWord));
}

@Provide
Arbitrary<String> deterministic(int length, Arbitrary<String> sentence) {
    if (length == 0) {
        return sentence;
    }
    Arbitrary<String> more = Combinators.combine(word(), sentence).as((w, s) -> w + " " + s);
    return deterministic(length - 1, more);
}
```

## Contract Tests

When you combine type variables with properties defined in superclasses or interfaces
you can do some kind of _contract testing_. That means that you specify
the properties in a generically typed interface and specify the concrete class to
instantiate in a test container implementing the interface.

The following example was influence by a similar feature in
[junit-quickcheck](http://pholser.github.io/junit-quickcheck/site/0.8/usage/contract-tests.html).
Here's the contract:

```java
interface ComparatorContract<T> {
	Comparator<T> subject();

	@Property
	default void symmetry(@ForAll("anyT") T x, @ForAll("anyT") T y) {
		Comparator<T> subject = subject();

		Assertions.assertThat(signum(subject.compare(x, y))).isEqualTo(-signum(subject.compare(y, x)));
	}

	@Provide
	Arbitrary<T> anyT();
}
```

And here's the concrete test container that can be run to execute
the property with generated Strings:

```java
class StringCaseInsensitiveProperties implements ComparatorContract<String> {

	@Override public Comparator<String> subject() {
		return String::compareToIgnoreCase;
	}

	@Override
	@Provide
	public Arbitrary<String> anyT() {
		return Arbitraries.strings().alpha().ofMaxLength(20);
	}
}
```

What we can see here is that _jqwik_ is able to figure out the concrete
type of type variables when they are used in subtypes that fill in
the variables.


## Stateful Testing

Despite its bad reputation _state_ is an important concept in object-oriented languages like Java.
We often have to deal with stateful objects or components whose state can be changed through methods. 

Thinking in a more formal way we can look at those objects as _state machines_ and the methods as
_actions_ that move the object from one state to another. Some actions have preconditions to constrain
when they can be invoked and some objects have invariants that should never be violated regardless
of the sequence of performed actions.

To make this abstract concept concrete, let's look at a 
[simple stack implementation](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/stateful/mystack/MyStringStack.java):

```java
public class MyStringStack {
	public void push(String element) { ... }
	public String pop() { ... }
	public void clear() { ... }
	public boolean isEmpty() { ... }
	public int size() { ... }
	public String top() { ... }
}
```

### Specify Actions

We can see at least three _actions_ with their preconditions and expected state changes:

- [`Push`](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/stateful/mystack/PushAction.java): 
  Push a string onto the stack. The string should be on top afterwards and the size
  should have increased by 1.
  
  ```java
  import net.jqwik.api.stateful.*;
  import org.assertj.core.api.*;
  
  class PushAction implements Action<MyStringStack> {
  
  	private final String element;
  
  	PushAction(String element) {
  		this.element = element;
  	}
  
  	@Override
  	public MyStringStack run(MyStringStack model) {
  		int sizeBefore = model.size();
  		model.push(element);
  		Assertions.assertThat(model.isEmpty()).isFalse();
  		Assertions.assertThat(model.size()).isEqualTo(sizeBefore + 1);
  		return model;
  	}
  
  	@Override
  	public String toString() { return String.format("push(%s)", element); }
  }
  ``` 

- [`Pop`](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/stateful/mystack/PopAction.java): 
  If (and only if) the stack is not empty, pop the element on top off the stack. 
  The size of the stack should have decreased by 1.
  
  ```java
  class PopAction implements Action<MyStringStack> {
    
        @Override
        public boolean precondition(MyStringStack model) {
            return !model.isEmpty();
        }
    
        @Override
        public MyStringStack run(MyStringStack model) {
            int sizeBefore = model.size();
            String topBefore = model.top();
    
            String popped = model.pop();
            Assertions.assertThat(popped).isEqualTo(topBefore);
            Assertions.assertThat(model.size()).isEqualTo(sizeBefore - 1);
            return model;
        }
    
        @Override
        public String toString() { return "pop"; }
  }
  ``` 

- [`Clear`](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/stateful/mystack/ClearAction.java): 
  Remove all elements from the stack which should be empty afterwards.
  
  ```java
  class ClearAction implements Action<MyStringStack> {

        @Override
        public MyStringStack run(MyStringStack model) {
            model.clear();
            Assertions.assertThat(model.isEmpty()).isTrue();
            return model;
        }
    
        @Override
        public String toString() { return "clear"; }
  }
  ``` 

### Check Postconditions

The fundamental property that _jqwik_ should try to falsify is:

    For any valid sequence of actions all required state changes
    (aka postconditions) should be fulfilled.
    
We can formulate that quite easily as a 
[_jqwik_ property](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/stateful/mystack/MyStringStackProperties.java):

```java
class MyStringStackProperties {

	@Property
	void checkMyStack(@ForAll("sequences") ActionSequence<MyStringStack> actions) {
		actions.run(new MyStringStack());
	}

	@Provide
	Arbitrary<ActionSequence<MyStringStack>> sequences() {
		return Arbitraries.sequences(Arbitraries.oneOf(push(), pop(), clear()));
	}

	private Arbitrary<Action<MyStringStack>> push() {
		return Arbitraries.strings().alpha().ofLength(5).map(PushAction::new);
	}

	private Arbitrary<Action<MyStringStack>> clear() {
		return Arbitraries.constant(new ClearAction());
	}

	private Arbitrary<Action<MyStringStack>> pop() {
		return Arbitraries.constant(new PopAction());
	}
}
```

The interesting API elements are 
- [`ActionSequence`](https://jqwik.net/javadoc/net/jqwik/api/stateful/ActionSequence.html):
  A generic collection type especially crafted for holding and shrinking of a list of actions.
  As a convenience it will apply the actions to a model when you call `run(model)`.
  
- [`Arbitraries.sequences()`](https://jqwik.net/javadoc/net/jqwik/api/Arbitraries.html#sequences-net.jqwik.api.Arbitrary-):
  This method will create the arbitrary for generating an `ActionSequence` given the
  arbitrary for generating actions.

To give _jqwik_ something to falsify, we broke the implementation of `clear()` so that
it won't clear everything if there are more than two elements on the stack:

```java
public void clear() {
    // Wrong implementation to provoke falsification for stacks with more than 2 elements
    if (elements.size() > 2) {
        elements.remove(0);
    } else {
        elements.clear();
    }
}
```

Running the property should now produce a result similar to:

```
org.opentest4j.AssertionFailedError: Run failed after following actions:
    push(AAAAA)
    push(AAAAA)
    push(AAAAA)
    clear
  final state: ["AAAAA", "AAAAA"]
```
   
### Check Invariants

We can also add invariants to our sequence checking property:

```java
@Property
void checkMyStackWithInvariant(@ForAll("sequences") ActionSequence<MyStringStack> actions) {
    actions
        .withInvariant(stack -> Assertions.assertThat(stack.size()).isGreaterThanOrEqualTo(0))
        .withInvariant(stack -> Assertions.assertThat(stack.size()).isLessThan(5))
        .run(new MyStringStack());
}
```

If we first fix the bug in `MyStringStack.clear()` our property should eventually fail 
with the following result:

```
org.opentest4j.AssertionFailedError: Run failed after following actions:
    push(AAAAA)
    push(AAAAA)
    push(AAAAA)
    push(AAAAA)
    push(AAAAA)
  final state: ["AAAAA", "AAAAA", "AAAAA", "AAAAA", "AAAAA"]
```


## Assumptions

If you want to constrain the set of generated values in a way that embraces
more than one parameter, [filtering](#filtering) does not work. What you
can do instead is putting one or more assumptions at the beginning of your property.

[The following property](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/AssumptionExamples.java) 
works only on strings that are not equal:

```java
@Property
boolean comparingUnequalStrings( //
        @ForAll @StringLength(min = 1, max = 10) String string1, //
        @ForAll @StringLength(min = 1, max = 10) String string2 //
) {
    Assume.that(!string1.equals(string2));

    return string1.compareTo(string2) != 0;
}
```

This is a reasonable use of 
[`Assume.that(boolean condition)`](https://jqwik.net/javadoc/net/jqwik/api/Assume.html#that-boolean-) 
because most generated value sets will pass through.

Have a look at a seemingly similar example:

```java
@Property
boolean findingContainedStrings( //
        @ForAll @StringLength(min = 1, max = 10) String container, //
        @ForAll @StringLength(min = 1, max = 5) String contained //
) {
    Assume.that(container.contains(contained));

    return container.indexOf(contained) >= 0;
}
```

Despite the fact that the property condition itself is correct, the property will most likely
fail with the following message:

```
timestamp = 2017-11-06T14:36:15.134, 
    seed = 1066117555581106850
    tries = 1000, 
    checks = 20, 

org.opentest4j.AssertionFailedError: 
    Property [findingContainedStrings] exhausted after [1000] tries and [980] rejections
```

The problem is that - given a random generation of two strings - only in very few cases
one string will be contained in the other. _jqwik_ will report a property as `exhausted`
if the ratio between generated and accepted parameters is higher than 5. You can change
the maximum discard ratio by specifying a parameter `maxDiscardRatio` in the 
[`@Property`](https://jqwik.net/javadoc/net/jqwik/api/Property.html) annotation.
That's why changing to `@Property(maxDiscardRatio = 100)` in the previous example 
will probably result in a successful property run, even though only a handful 
cases - of 1000 generated - will actually be checked.

In many cases turning up the accepted discard ration is a bad idea. With some creativity
we can often avoid the problem by generating out test data a bit differently. 
Look at this variant of the above property, which also uses 
[`Assume.that()`](https://jqwik.net/javadoc/net/jqwik/api/Assume.html#that-boolean-)
but with a much lower discard ratio:

```java
@Property
boolean findingContainedStrings_variant( //
        @ForAll @StringLength(min = 5, max = 10) String container, //
        @ForAll @IntRange(min = 1, max = 5) int length, //
        @ForAll @IntRange(min = 0, max = 9) int startIndex //
) {
    Assume.that((length + startIndex) <= container.length());

    String contained = container.substring(startIndex, startIndex + length);
    return container.indexOf(contained) >= 0;
}
```

## Result Shrinking

If a property could be falsified with a generated set of values, _jqwik_ will
try to "shrink" this sample in order to find a "smaller" sample that also falsifies the property.

Try this property:

```java
@Property
boolean stringShouldBeShrunkToAA(@ForAll @AlphaChars String aString) {
    return aString.length() > 5 || aString.length() < 2;
}
```

The test run result should look something like:
```
timestamp = 2017-11-04T16:42:25.859, 
    seed = -633877439388930932, 
    tries = 38, 
    checks = 38, 
    originalSample = ["LVtyB"], 
    sample = ["AA"]

AssertionFailedError: Property [stringShouldBeShrunkToAA] falsified with sample ["AA"]
```

In this case the _originalSample_ could be any string between 2 and 5 chars, whereas the final _sample_
should be exactly `AA` since this is the shortest failing string and `A` has the lowest numeric value
of all allowed characters.

### Integrated Shrinking

_jqwik_'s shrinking approach is called _integrated shrinking_, as opposed to _type-based shrinking_
which most property-based testing tools use.
The general idea and its advantages are explained 
[here](http://hypothesis.works/articles/integrated-shrinking/).

Consider a somewhat 
[more complicated example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/ShrinkingExamples.java#L15):

```java
@Property
boolean shrinkingCanTakeLong(@ForAll("first") String first, @ForAll("second") String second) {
    String aString = first + second;
    return aString.length() > 5 || aString.length() < 2;
}

@Provide
Arbitrary<String> first() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(1).ofMaxLength(10)
        .filter(string -> string.endsWith("h"));
}

@Provide
Arbitrary<String> second() {
    return Arbitraries.strings()
        .withCharRange('0', '9')
        .ofMinLength(0).ofMaxLength(10)
        .filter(string -> string.length() >= 1);
}
```

Shrinking still works, although there's quite a bit of filtering and string concatenation happening:
```
timestamp = 2017-11-04T16:58:45.431, 
    seed = -5596810132893895291, 
    checks = 20, 
    tries = 20, 
    originalSample = ["gh", "774"], 
    sample = ["h", "0"]

AssertionFailedError: Property [shrinkingCanTakeLong] falsified with sample ["h", "0"]
```

### Switch Shrinking Off

Sometimes shrinking takes a really long time or won't finish at all (usually a _jqwik_ bug!). 
In those cases you can switch shrinking off for an individual property:

```java
@Property(shrinking = ShrinkingMode.OFF)
void aPropertyWithLongShrinkingTimes(
	@ForAll List<Set<String>> list1, 
	@ForAll List<Set<String>> list2
) {	... }
```

### Switch Shrinking to Full Mode

Sometimes you can find a message similar to

```
shrinking bound reached =
    steps : 1000
    original value : [blah blah blah ...]
    shrunk value   : [bl bl bl ...]
```

in your testrun's output.
This happens in rare cases when _jqwik_ has not found the end of its search for
simpler falsifiable values after 1000 iterations. In those cases you
can try

```java
@Property(shrinking = ShrinkingMode.FULL)
```

to tell _jqwik_ to go all the way, even if it takes a million steps,
even if it never ends...

## Collecting and Reporting Statistics

In many situations you'd like to know if _jqwik_ will really generate
the kind of values you expect and if the frequency and distribution of
certain value classes meets your testing needs. 
[`Statistics.collect()`](https://jqwik.net/javadoc/net/jqwik/api/Statistics.html#collect-java.lang.Object...-)
is made for this exact purpose.

In the most simple case you'd like to know how often a certain value
is being generated:

```java
@Property
void simpleStats(@ForAll RoundingMode mode) {
    Statistics.collect(mode);
}
```

will create an output similar to that:

```
collected statistics = 
     UNNECESSARY : 15 %
     DOWN        : 14 %
     FLOOR       : 13 %
     UP          : 13 %
     HALF_DOWN   : 13 %
     HALF_EVEN   : 12 %
     CEILING     : 11 %
     HALF_UP     : 11 %
```

More typical is the case in which you'll classify generated values
into two or more groups:

```java
@Property
void integerStats(@ForAll int anInt) {
    Statistics.collect(anInt > 0 ? "positive" : "negative");
}
```

```
collected statistics = 
     negative : 52 %
     positive : 48 %
```

You can also collect the distribution in more than one category
and combine those categories:

```java
@Property
void combinedIntegerStats(@ForAll int anInt) {
    String posOrNeg = anInt > 0 ? "positive" : "negative";
    String evenOrOdd = anInt % 2 == 0 ? "even" : "odd";
    String bigOrSmall = Math.abs(anInt) > 50 ? "big" : "small";
    Statistics.collect(posOrNeg, evenOrOdd, bigOrSmall);
}
```

```
collected statistics = 
     positive odd big    : 23 %
     negative even big   : 22 %
     positive even big   : 22 %
     negative odd big    : 21 %
     positive odd small  : 4 %
     negative odd small  : 3 %
     negative even small : 3 %
     positive even small : 2 %
```

And, of course, you can combine different generated parameters into
one statistical group:

```java
@Property
void twoParameterStats(
    @ForAll @Size(min = 1, max = 10) List<Integer> aList, //
    @ForAll @IntRange(min = 0, max = 10) int index //
) {
    Statistics.collect(aList.size() > index ? "index within size" : null);
}
```

```
collected statistics = 
     index within size : 48 %
```

As you can see, collected `null` values are not being reported.

[Here](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/StatisticsExamples.java)
are a couple of examples to try out.

## Running and Configuration

When running _jqwik_ tests (through your IDE or your build tool) you might notice 
that - once a property has been falsified - it will always be tried
with the same seed to enhance the reproducibility of a bug. This requires
that _jqwik_ will persist some runtime data across test runs.

You can configure this and other default behaviour in [jqwik's configuration](#jqwik_configuration).

### jqwik Configuration

_jqwik_ will look for a file `jqwik.properties` in your classpath in which you can configure
a few basic parameters:

```
database = .jqwik-database
rerunFailuresWithSameSeed = true
defaultTries = 1000
defaultMaxDiscardRatio = 5
```

## Providing Default Arbitraries

Sometimes you want to use a certain, self-made `Arbitrary` for one of your own domain
classes, in all of your properties, and without having to add `@Provide` method
to all test classes. _jqwik_ enables this feature by using 
Java’s `java.util.ServiceLoader` mechanism. All you have to do is:

- Implement the interface [`ArbitraryProvider`](https://jqwik.net/javadoc/net/jqwik/api/providers/ArbitraryProvider.html).<br/>
  The implementing class _must_ have a default constructor without parameters.
- Register the implementation class in file

  ```
  META-INF/services/net.jqwik.api.providers.ArbitraryProvider
  ```

_jqwik_ will then add an instance of your arbitrary provider into the list of
its default providers. Those default providers are considered for every test parameter annotated 
with [`@ForAll`](https://jqwik.net/javadoc/net/jqwik/api/ForAll.html) that has no explicit `value`.
By using this mechanism you can also replace the default providers
packaged into _jqwik_.

### Simple Arbitrary Providers

A simple provider is one that delivers arbitraries for types without type variables.
Consider the class [`Money`](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/defaultprovider/Money.java):

```java
public class Money {
	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public Money(BigDecimal amount, String currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public Money times(int factor) {
		return new Money(amount.multiply(new BigDecimal(factor)), currency);
	}
}
``` 

If you register the following class
[`MoneyArbitraryProvider`](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/defaultprovider/MoneyArbitraryProvider.java):

```java
package my.own.provider;

public class MoneyArbitraryProvider implements ArbitraryProvider {
	@Override
	public boolean canProvideFor(TypeUsage targetType) {
		return targetType.isOfType(Money.class);
	}

	@Override
	public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
		Arbitrary<BigDecimal> amount = Arbitraries.bigDecimals() //
				  .between(BigDecimal.ZERO, new BigDecimal(1_000_000_000)) //
				  .ofScale(2);
		Arbitrary<String> currency = Arbitraries.of("EUR", "USD", "CHF");
		return Collections.singleton(Combinators.combine(amount, currency).as(Money::new));
	}
}
```

in file 
[`META-INF/services/net.jqwik.api.providers.ArbitraryProvider`](https://github.com/jlink/jqwik/blob/master/src/test/resources/META-INF/services/net.jqwik.api.providers.ArbitraryProvider) 
with such an entry:

```
my.own.provider.MoneyArbitraryProvider
```

The 
[following property](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/defaultprovider/MoneyProperties.java)
will run without further ado - regardless the class you put it in:

```java
@Property
void moneyCanBeMultiplied(@ForAll Money money) {
    Money times2 = money.times(2);
    Assertions.assertThat(times2.getCurrency()).isEqualTo(money.getCurrency());
    Assertions.assertThat(times2.getAmount())
        .isEqualTo(money.getAmount().multiply(new BigDecimal(2)));
}
```

### Arbitrary Providers for Parameterized Types

Providing arbitraries for generic types requires a little bit more effort
since you have to create arbitraries for the "inner" types as well. 
Let's have a look at the default provider for `java.util.Optional<T>`:

```java
public class OptionalArbitraryProvider implements ArbitraryProvider {
	@Override
	public boolean canProvideFor(TypeUsage targetType) {
		return targetType.isOfType(Optional.class);
	}

	@Override
	public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
		TypeUsage innerType = targetType.getTypeArguments().get(0);
		return subtypeProvider.apply(innerType).stream() //
			.map(Arbitrary::optional)
			.collect(Collectors.toSet());
	}
}
```

Mind that `provideFor` returns a set of potential arbitraries.
That's necessary because the `subtypeProvider` might also deliver a choice of
subtype arbitraries. Not too difficult, is it?


### Arbitrary Provider Priority

When more than one provider is suitable for a given type, _jqwik_ will randomly
choose between all available options. That's why you'll have to take additional
measures if you want to replace an already registered provider. The trick
is to override a provider's `priority()` method that returns `0` by default:

```java
public class AlternativeStringArbitraryProvider implements ArbitraryProvider {
	@Override
	public boolean canProvideFor(TypeUsage targetType) {
		return targetType.isAssignableFrom(String.class);
	}

	@Override
	public int priority() {
		return 1;
	}

	@Override
	public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
		return Collections.singleton(Arbitraries.constant("A String"));
	}
}
```

If you register this class as arbitrary provider any `@ForAll String` will
be resolved to `"A String"`.

## Create your own Annotations for Arbitrary Configuration

All you can do [to constrain default parameter generation](#constraining-default-generation)
is adding another annotation to a parameter or its parameter types. What if the existing parameters
do not suffice your needs? Is there a way to enhance the set of constraint annotations? Yes, there is!

The mechanism you can plug into is similar to what you do when 
[providing your own default arbitrary providers](#providing-default-arbitraries). That means:

1. Create an implementation of an interface, in this case 
  [`ArbitraryConfigurator`](https://jqwik.net/javadoc/net/jqwik/api/configurators/ArbitraryConfigurator.html).
2. Register the implementation using using Java’s `java.util.ServiceLoader` mechanism.

### Arbitrary Configuration Example: `@Odd`

To demonstrate the idea let's create an annotation `@Odd` which will constrain any integer
generation to only generate odd numbers. First things first, so here's 
the [`@Odd` annotation](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/arbitraryconfigurator/Odd.java) 
together with the 
[configurator implementation](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/arbitraryconfigurator/OddConfigurator.java):

```java
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Odd {
}

public class OddConfigurator extends ArbitraryConfiguratorBase {
	public Arbitrary<Integer> configure(Arbitrary<Integer> arbitrary, Odd odd) {
		return arbitrary.filter(number -> Math.abs(number % 2) == 1);
	}
}
```

Mind that the implementation uses an abstract base class - instead of the interface itself -
which simplifies implementation if you're only interested in a single annotation.

If you now 
[register the implementation](https://github.com/jlink/jqwik/blob/master/src/test/resources/META-INF/services/net.jqwik.api.configurators.ArbitraryConfigurator), 
the [following example](https://github.com/jlink/jqwik/blob/master/src/test/java/examples/docs/arbitraryconfigurator/OddProperties.java) 
will work:

```java
@Property(reporting = Reporting.GENERATED)
boolean oddIntegersOnly(@ForAll @Odd int aNumber) {
    return Math.abs(aNumber % 2) == 1;
}
```

There are two catches, though: 

- Currently `OddConfigurator` only works for numbers of type `Integer` or `int`. If you want
  to generalize it to all integral types, you have to provide additional `configure` methods
  that accept `ByteInteger`, `ShortInteger` and so on as their first parameter.
- You can combine `@Odd` with other annotations like `@Positive` or `@Range` or another
  self-made configurator. In this case the order of configurator registration might play a role, 
  because a configurator will potentially change the runtime type of an arbitrary instance.
 

## Implement your own Arbitraries and Generators

In your everyday property testing you will often get along without ever implementing
an arbitrary yourself. In cases where 
[constraining default generation through annotations](#constraining-default-generation)
does not cut it, you can use all the mechanisms to configure, (flat-)map, filter and combine
the pre-implemented arbitraries.

However, there are a few circumstances when you should think about rolling your own
implementation. The most important of which are:

- You want to expand the fluent API for configuration purposes.
- The (randomized) generation of values needs different qualities than can easily be
  derived by reusing existing arbitraries.
- Standard shrinking attempts do not come up with simple enough examples.
  
In those - and maybe a few other cases - you can implement your own arbitrary.
To get a feel for what a usable implementation looks like, you might start with
having a look at some of the internal arbitraries:

- [DefaultBigDecimalArbitrary](https://github.com/jlink/jqwik/blob/master/src/main/java/net/jqwik/properties/arbitraries/DefaultBigDecimalArbitrary.java)
- [DefaultStringArbitrary](https://github.com/jlink/jqwik/blob/master/src/main/java/net/jqwik/properties/arbitraries/DefaultStringArbitrary.java) 

Under the hood, most arbitraries use `RandomGenerator`s for the final value generation. Since
[`RandomGenerator`](https://jqwik.net/javadoc/net/jqwik/api/RandomGenerator.html) 
is a SAM type, most implementations are just lambda expression. 
Start with the methods on [`RandomGenerators`]() to figure out how they work.

Since the topic is rather complicated, a detailed example will one day be published 
in a separate article...

## Data-Driven Properties

In addition to the usual randomized generation of property parameters you have also
the possibility to feed a property with preconceived or deterministically generated
parameter sets. Why would you want to do that? One reason might be that you are aware
of some problematic test cases but they are rare enough that _jqwik_'s randomization
strategies don't generate them (often enough). Another reason could be that you'd like
to feed some properties with prerecorded data - maybe even from production.
And last but not least there's a chance that you want to check for a concrete result
given a set of input parameters.

Feeding data into a property is quite simple:

```java
@Data
Iterable<Tuple2<Integer, String>> fizzBuzzExamples() {
    return Table.of(
        Tuple.of(1, "1"),
        Tuple.of(3, "Fizz"),
        Tuple.of(5, "Buzz"),
        Tuple.of(15, "FizzBuzz")
    );
}

@Property
@FromData("fizzBuzzExamples")
void fizzBuzzWorks(@ForAll int index, @ForAll String result) {
    Assertions.assertThat(fizzBuzz(index)).isEqualTo(result);
}
```

All you have to do is annotate the property method with 
`@FromData("dataProviderReference")`. The method you reference must be
annotated with `@Data` and return an object of type `Iterable<? extends Tuple>`.
The [`Table` class](https://jqwik.net/javadoc/net/jqwik/api/Table.html) 
is just a convenient way to create such an object, but you can return
any collection or create an implementation of your own. 

Keep in mind that the `Tuple` subtype you choose must conform to the 
number of `@ForAll` parameters in your property method, e.g. `Tuple.Tuple3` 
for a method with three parameters. Otherwise _jqwik_ will fail the property
and tell you that the provided data is inconsistent with the method's parameters. 

Data points are fed to the property in their provided order.
But unlike parameterized tests in JUnit4 or Jupiter, _jqwik_ will report only the
first falsified data point. Thus, fixing the first failure might lead to another
falsified data point later on. There is also _no shrinking_ being done for data-driven
properties since _jqwik_ has no information about the constraints under which 
the external data was conceived or generated.
  

## Release Notes

### 0.9.0-SNAPSHOT

- Removed deprecated static methods in `Arbitraries`
- Removed deprecated method `ArbitraryProvider.provideFor(TypeUsage targetType, Function<TypeUsage, Optional<Arbitrary<?>>> subtypeProvider)`
- Removed default implementation of `ArbitraryProvider.provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider)`
- Renamed `TypeUsage.getAnnotation(Class annotationClass)` to `findAnnotation`
- Added `TypeUsage.isAnnotated(Class annotationClass)`
- Added [`Arbitrary.unique()`](#creating-unique-values)
- Added constraint [`@Unique`](#unique-values)
- Implementations of `ArbitraryConfigurator` can optionally implement `int order()`
- It's now possible to ["flat combine"](#flat-combination) arbitraries
- Deprecated all types and methods in `net.jqwik.api.Tuples.*`
  in favour of `net.jqwik.api.Tuple.*`
- There are new tuple types `Tuple5` up to `Tuple8`
- [Data-Driven Properties:](#data-driven-properties) Feed data into your properties
  instead of randomized generation

### 0.8.x

##### 0.8.15

- `StringArbitrary.withChars()` now accepts varargs.
- Added `StringArbitrary.whitespace()`
- Added `@Whitespace` annotation
- Improved shrinking of action sequences
- Default String generation does no longer generate Unicode "noncharacters"
  or "private use characters"
- Added `StringArbitrary.all()` for also generating
  Unicode "noncharacters" and "private use characters"

##### 0.8.14

- Some potentially incompatible stuff has changed for
  [default arbitrary providers](#providing-default-arbitraries):
  - Introduced `ArbitraryProvider.priority()`
  - The old `ArbitraryProvider.provideFor(TypeUsage, Function)` is now deprecated, override
    `ArbitraryProvider.provideFor(TypeUsage, SubtypeProvider)` instead
  - If more than one provider fits a given type, one of the will be
    chosen randomly
- `Arbitraries.defaultFor()` will randomly choose one arbitrary if
  there is more than one fitting registered arbitrary provider

##### 0.8.13

Faulty release. Do not use!

##### 0.8.12

- Implemented generic type resolution to enable [contract tests](#contract-tests)
- Renamed `GenericType` to `TypeUsage`
  <p/>_This is an incompatible API change!_

##### 0.8.11

- Reporting with `Reporting.FALSIFIED` now reports much less, and hopefully no wrong values anymore.
- Shrinking with filtered values finds simpler values in some circumstance
- Generation of strings will allow any unicode character by default
- `Combinators.combine()` can now take a list of arbitraries of same return type.
- Generated edge cases are now injected again and again and not only in the beginning.
- Complete re-implementation of shrinking with a few implications:
  - Shrinking should work better and more efficient overall. There might
    be situations, though, in which shrinking does no longer find
    the simplest example.
  - `ShrinkingMode.BOUNDED` might interrupt shrinking with completely different results.
  - When using `Reporting.FALSIFIED` you will see different inbetween
    shrinking steps that before.
  - The public interface of `Shrinkable` has changed in an incompatible way,
    but that shouldn't affect anyone but myself.


##### 0.8.10

- Fixed shrinking bug that could result in integers not being shrunk
  as far as possible
- Integer shrinking should be faster in most cases and cover more cases

##### 0.8.9

- Some minor but potentially incompatible API changes in `GenericType`.
- Tags from parent (e.g. container class) are now also present in children (methods) 
- Renamed `ShrinkingMode.ON` to `ShrinkingMode.FULL`
  <p/>_This is an incompatible API change!_
- Introduced `ShrinkingMode.BOUNDED` and made it the default
- Introduced `ShrinkingMode.FULL`
- Some bounded wildcard types and type variables can be provided automatically

##### 0.8.8

- Added `Arbitraries.lazy()` 
  to allow [recursive value generation](#recursive-arbitraries)
- Added `Arbitrary.fixGenSize()` to enable a fixed genSize when creating random generators
- Added `Arbitrary.sequences()` to create sequences of actions for [stateful testing](#stateful-testing)

##### 0.8.7

- Property methods that also have Jupiter annotations are skipped
- Added `@Label` to allow the [labeling of examples, properties and containers](#labeling-tests)
- Changed license from EPL 1.0 to EPL 2.0
- Added `@Tag` to allow the [tagging of examples, properties and containers](#tagging-tests)
- User guide: Added links to example sources on github
- Added `Arbitraries.frequency()` to enable 
  [choosing values with weighted probabilities](#select-randomly-with-weights)
- Collection and String generation now explores a wider range of sizes and lengths

##### 0.8.6

- BigInteger generation does no longer support `@LongRange` but only `@BigRange`
  <p/>_This is an incompatible API change!_
- BigDecimal generation does no longer support `@DoubleRange` but only `@BigRange`
  <p/>_This is an incompatible API change!_
- BigInteger generation now supports numbers outside long range
- Property.seed is now of type String
  <p/>_This is an incompatible API change!_
- Property methods without @ForAll parameters are now also tried as many times as 
  specified by `tries` parameter.
- Added new method `Arbitraries.constant()`
- Added new method `Arbitraries.defaultFor()`
- `@WithNull.target()` has been removed
  <p/>_This is an incompatible API change!_
- Parameterized types [can now be annotated directly](#constraining-parameterized-types)
- Added `@Size.value()` for fixed size collections
- Added `@StringLength.value()` for fixed size Strings

##### 0.8.5

- All decimal generation (float, double, BigDecimal) now uses BigDecimal under the hood
- All integral generation (int, short, byte, long, BigInteger) now uses BigInteger under the hood
- Numbers are now generated within their full domain (min, max)
- Decimal shrinking improved
- Fixed bug: Reporting.FALSIFIED now also works for falsification through exception
- Added support for running all tests in a module (Java 9 only). I HAVE NOT TESTED IT! 

##### 0.8.4

- Completely rebuild the annotation-based configuration of registered arbitrary providers
- Introduced [fluent configuration interfaces](#fluent-configuration-interfaces)
- Introduced [Arbitrary.list/set/stream/optional/array](#collections-streams-arrays-and-optional)
- Combinators.combine() now allows up to 8 parameters
- Character creation does no longer support `@Chars` but only `@CharRange`
  <p/>_This is an incompatible API change!_
- 'Arbitraries.chars(char[] validChars)' does no longer exist
  <p/>_This is an incompatible API change!_
- Added [`Arbitraries.oneOf`](#randomly-choosing-among-arbitraries)
- `@Char` cannot take `from` and `to` any longer. Replaced by `@CharRange`
- Deprecated many methods in `Arbitraries` class. Replaced by fluent interface methods.
- Deprecated `@Digits` constraint. Replaced by `@NumericChars`.
- Upgrade to JUnit 5.1.0

##### 0.8.3

- Bugfix: Injected empty list samples are now mutable
- Bugfix: Injected empty set samples are now mutable
- Unbound type variables in properties [can now be provided](#providing-variable-types)

##### 0.8.2

- Added support for `java.util.Random` generation.
- Added [Tuple types](#flat-mapping-with-tuple-types) 
  (`Tuple2`, `Tuple3`, `Tuple4`) to use in `Arbitrary.flatMap()`.
- Renamed `ReportingMode` to `Reporting` and removed `Reporting.MINIMAL`.
  <p/>_This is an incompatible API change!_

- Added `Reporting.FALSIFIED`. See [section on optional property parameters](#optional-property-parameters)

##### 0.8.1

- Added support for [default arbitrary providers](#providing-default-arbitraries).
- Added support for `byte` and `Byte` generation.
- Added support for `short` and `Short` generation.

##### 0.8.0

The first release published on maven central.