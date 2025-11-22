package org.robolectric;

import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.robolectric.junit.RobolectricJUnit5TestRunnerHelper;

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

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    // Validation can be added here if needed
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    RobolectricJUnit5TestRunnerHelper helper = getHelper(context);
    Method testMethod = context.getRequiredTestMethod();
    helper.beforeEach(testMethod);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    RobolectricJUnit5TestRunnerHelper helper = getHelper(context);
    Method testMethod = context.getRequiredTestMethod();
    helper.afterEach(testMethod);
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    // Cleanup if needed
  }

  @Override
  public void interceptBeforeAllMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    RobolectricJUnit5TestRunnerHelper helper = getHelper(extensionContext);
    helper.proceedInvocation(null, invocation);
  }

  @Override
  public void interceptBeforeEachMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    RobolectricJUnit5TestRunnerHelper helper = getHelper(extensionContext);
    helper.proceedInvocation(extensionContext.getRequiredTestMethod(), invocation);
  }

  @Override
  public void interceptTestMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    RobolectricJUnit5TestRunnerHelper helper = getHelper(extensionContext);
    helper.proceedInvocation(extensionContext.getRequiredTestMethod(), invocation);
  }

  @Override
  public void interceptAfterEachMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    RobolectricJUnit5TestRunnerHelper helper = getHelper(extensionContext);
    helper.proceedInvocation(extensionContext.getRequiredTestMethod(), invocation);
  }

  @Override
  public void interceptAfterAllMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    RobolectricJUnit5TestRunnerHelper helper = getHelper(extensionContext);
    helper.proceedInvocation(null, invocation);
  }

  private RobolectricJUnit5TestRunnerHelper getHelper(ExtensionContext context) {
    return RobolectricJUnit5TestRunnerHelper.getInstance(context.getRequiredTestClass());
  }
}
