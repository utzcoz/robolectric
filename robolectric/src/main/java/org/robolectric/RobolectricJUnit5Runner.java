package org.robolectric;

/**
 * Alias for {@link RobolectricExtension} to provide a more intuitive name for JUnit5 support.
 * 
 * <p>This class serves as a JUnit5 extension for running Robolectric tests with JUnit Jupiter.
 * Use it with {@code @ExtendWith} annotation on your test class:
 *
 * <pre>
 * {@literal @}ExtendWith(RobolectricJUnit5Runner.class)
 * public class MyTest {
 *   {@literal @}Test
 *   public void testSomething() {
 *     // test code with Android APIs
 *   }
 * }
 * </pre>
 *
 * <p>Note: Despite the name "Runner", this is actually a JUnit5 Extension. JUnit5 uses
 * extensions instead of runners. The name is chosen to align with {@link RobolectricTestRunner}
 * and make the purpose clear.
 *
 * @see RobolectricExtension
 * @see RobolectricTestRunner
 */
public class RobolectricJUnit5Runner extends RobolectricExtension {
  // This class intentionally has no additional logic.
  // It simply extends RobolectricExtension to provide a clearer name.
}
