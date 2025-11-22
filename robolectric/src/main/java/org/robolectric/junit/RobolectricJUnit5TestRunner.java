package org.robolectric.junit;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.Sandbox;

/**
 * Internal helper class for running JUnit5 tests with Robolectric.
 * This class adapts the JUnit4-based RobolectricTestRunner to work with JUnit5.
 */
public class RobolectricJUnit5TestRunner {
  private final RobolectricTestRunner delegate;
  private final Class<?> testClass;
  private FrameworkMethod currentFrameworkMethod;
  private Sandbox currentSandbox;

  public RobolectricJUnit5TestRunner(Class<?> testClass) throws InitializationError {
    this.testClass = testClass;
    this.delegate = new RobolectricTestRunner(testClass);
  }

  /**
   * Called before each test method.
   */
  public void beforeTest(Method testMethod) throws Throwable {
    // Find the corresponding FrameworkMethod
    currentFrameworkMethod = findFrameworkMethod(testMethod);
    if (currentFrameworkMethod == null) {
      throw new IllegalStateException("Could not find FrameworkMethod for " + testMethod);
    }

    // Get the sandbox for this test
    currentSandbox = delegate.getSandbox(currentFrameworkMethod);
    delegate.configureSandbox(currentSandbox, currentFrameworkMethod);

    // Get the bootstrapped method and run beforeTest
    Method bootstrappedMethod = getBootstrappedMethod(testMethod);
    delegate.beforeTest(currentSandbox, currentFrameworkMethod, bootstrappedMethod);
  }

  /**
   * Called after each test method.
   */
  public void afterTest(Method testMethod) throws Throwable {
    if (currentFrameworkMethod == null) {
      return;
    }

    try {
      Method bootstrappedMethod = getBootstrappedMethod(testMethod);
      delegate.afterTest(currentFrameworkMethod, bootstrappedMethod);
    } finally {
      delegate.finallyAfterTest(currentFrameworkMethod);
      currentFrameworkMethod = null;
      currentSandbox = null;
    }
  }

  /**
   * Runs the test method inside the Robolectric sandbox.
   */
  public void runTestMethod(InvocationInterceptor.Invocation<Void> invocation, Method testMethod)
      throws Throwable {
    if (currentSandbox == null) {
      throw new IllegalStateException("Sandbox not initialized. beforeTest() must be called first.");
    }

    // Run the test in the sandbox
    currentSandbox.runOnMainThread(
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            try {
              invocation.proceed();
              return null;
            } catch (Throwable t) {
              if (t instanceof Exception) {
                throw (Exception) t;
              } else if (t instanceof Error) {
                throw (Error) t;
              } else {
                throw new RuntimeException(t);
              }
            }
          }
        });
  }

  private FrameworkMethod findFrameworkMethod(Method method) {
    for (FrameworkMethod fm : delegate.getChildren()) {
      if (fm.getMethod().equals(method)) {
        return fm;
      }
    }
    return null;
  }

  private Method getBootstrappedMethod(Method method) throws ClassNotFoundException, NoSuchMethodException {
    if (currentSandbox == null) {
      throw new IllegalStateException("Sandbox not initialized");
    }

    Class<?> bootstrappedClass =
        currentSandbox.bootstrappedClass(method.getDeclaringClass());
    return bootstrappedClass.getMethod(method.getName(), method.getParameterTypes());
  }
}
