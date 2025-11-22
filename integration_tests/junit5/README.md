# Robolectric JUnit5 Integration Tests

This module contains integration tests demonstrating how to use Robolectric with JUnit5 (Jupiter).

## Overview

Robolectric now supports JUnit5 through the `RobolectricJUnit5Runner` extension, which provides the same functionality as the JUnit4-based `RobolectricTestRunner` but for JUnit5 tests.

## Usage

To use Robolectric with JUnit5, annotate your test class with `@ExtendWith(RobolectricJUnit5Runner.class)`:

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robolectric.RobolectricJUnit5Runner;
import static com.google.common.truth.Truth.assertThat;

@ExtendWith(RobolectricJUnit5Runner.class)
public class MyJUnit5Test {
  
  @Test
  public void testAndroidAPI() {
    Context context = ApplicationProvider.getApplicationContext();
    assertThat(context).isNotNull();
  }
}
```

## Features Demonstrated

This module includes tests demonstrating:

- **Basic JUnit5 Tests**: Simple test methods with `@Test` annotation
- **Lifecycle Methods**: `@BeforeEach`, `@AfterEach` support
- **Parameterized Tests**: Using `@ParameterizedTest` with various sources
- **Android API Access**: Full access to Android framework APIs in tests

## Dependencies

To use Robolectric with JUnit5 in your project, add these dependencies:

```kotlin
dependencies {
  testImplementation("org.robolectric:robolectric:VERSION")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
  
  // Optional: For parameterized tests
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}
```

## Running Tests

Run tests using Gradle:

```bash
./gradlew :integration_tests:junit5:test
```

## Note

Despite the name "Runner", `RobolectricJUnit5Runner` is actually a JUnit5 Extension. JUnit5 uses extensions instead of runners. The name is chosen to align with `RobolectricTestRunner` and make the purpose clear.
