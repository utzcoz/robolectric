package org.robolectric;

import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.junit.RobolectricJUnit5TestRunner;

/**
 * JUnit5 extension for Robolectric tests. This extension provides the same functionality as
 * {@link RobolectricTestRunner} but for JUnit5 tests.
 *
 * <p>Usage:
 * <pre>
 * {@literal @}ExtendWith(RobolectricExtension.class)
 * public class MyTest {
 *   {@literal @}Test
 *   public void testSomething() {
 *     // test code
 *   }
 * }
 * </pre>
 */
public class RobolectricExtension
    implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback,
        InvocationInterceptor {

  private static final String TEST_RUNNER_KEY = "robolectric.test.runner";

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    Class<?> testClass = context.getRequiredTestClass();
    RobolectricJUnit5TestRunner runner = new RobolectricJUnit5TestRunner(testClass);
    context.getStore(ExtensionContext.Namespace.GLOBAL).put(TEST_RUNNER_KEY + ":" + testClass.getName(), runner);
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    RobolectricJUnit5TestRunner runner = getRunner(context);
    Method testMethod = context.getRequiredTestMethod();
    runner.beforeTest(testMethod);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    RobolectricJUnit5TestRunner runner = getRunner(context);
    Method testMethod = context.getRequiredTestMethod();
    runner.afterTest(testMethod);
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    // Cleanup if needed
  }

  @Override
  public void interceptTestMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    RobolectricJUnit5TestRunner runner = getRunner(extensionContext);
    runner.runTestMethod(invocation, extensionContext.getRequiredTestMethod());
  }

  private RobolectricJUnit5TestRunner getRunner(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    String key = TEST_RUNNER_KEY + ":" + testClass.getName();
    return context
        .getStore(ExtensionContext.Namespace.GLOBAL)
        .get(key, RobolectricJUnit5TestRunner.class);
  }
}
