package org.robolectric.integrationtests.junit5;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.robolectric.RobolectricJUnit5Runner;

/** Parameterized test example using JUnit5 and Robolectric. */
@ExtendWith(RobolectricJUnit5Runner.class)
public class ParameterizedJUnit5Test {

  @ParameterizedTest
  @ValueSource(strings = {"Hello", "JUnit5", "Robolectric"})
  public void testWithValueSource(String text) {
    Context context = ApplicationProvider.getApplicationContext();
    assertThat(context).isNotNull();
    assertThat(text).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("provideTestData")
  public void testWithMethodSource(String input, int expectedLength) {
    assertThat(input).hasLength(expectedLength);
    Context context = ApplicationProvider.getApplicationContext();
    assertThat(context).isNotNull();
  }

  private static Stream<Arguments> provideTestData() {
    return Stream.of(
        Arguments.of("test", 4),
        Arguments.of("junit5", 6),
        Arguments.of("robolectric", 11));
  }
}
