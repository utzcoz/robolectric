package org.robolectric.junit;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.runners.model.FrameworkMethod;
import org.robolectric.internal.bytecode.Sandbox;

/**
 * Helper class that manages the Robolectric test runner for JUnit5 tests.
 * This class is responsible for:
 * - Creating and caching test runner instances per test class
 * - Managing the sandbox environment and classloader
 * - Coordinating test lifecycle with Robolectric
 */
public class RobolectricJUnit5TestRunnerHelper {
  private static final ConcurrentHashMap<String, RobolectricJUnit5TestRunnerHelper> helperCache =
      new ConcurrentHashMap<>();

  private final JUnit5RobolectricTestRunner testRunner;

  private RobolectricJUnit5TestRunnerHelper(Class<?> testClass) {
    try {
      this.testRunner = new JUnit5RobolectricTestRunner(testClass);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create RobolectricTestRunner for " + testClass, e);
    }
  }

  /**
   * Gets or creates a helper instance for the given test class.
   */
  public static RobolectricJUnit5TestRunnerHelper getInstance(Class<?> testClass) {
    return helperCache.computeIfAbsent(
        testClass.getName(), k -> new RobolectricJUnit5TestRunnerHelper(testClass));
  }

  /**
   * Called before each test method to set up the Robolectric environment.
   */
  public void beforeEach(Method testMethod) {
    FrameworkMethod frameworkMethod = testRunner.frameworkMethod(testMethod);
    Sandbox sandbox = testRunner.sdkEnvironment(frameworkMethod);
    
    runOnMainThreadWithRobolectric(
        sandbox,
        () -> {
          testRunner.runBeforeTest(sandbox, frameworkMethod, testMethod);
          return null;
        });
  }

  /**
   * Called after each test method to tear down the Robolectric environment.
   */
  public void afterEach(Method testMethod) {
    FrameworkMethod frameworkMethod = testRunner.frameworkMethod(testMethod);
    Sandbox sandbox = testRunner.getSdkEnvironmentForMethod(testMethod);
    
    try {
      runOnMainThread(
          sandbox,
          () -> {
            runWithRobolectric(sandbox, () -> {
              testRunner.runAfterTest(frameworkMethod, testMethod);
              return null;
            });
            testRunner.runFinallyAfterTest(sandbox, frameworkMethod);
            return null;
          });
    } finally {
      clearSandboxState(sandbox);
    }
  }

  /**
   * Proceeds with invocation in the Robolectric context.
   */
  public void proceedInvocation(Method testMethod, InvocationInterceptor.Invocation<Void> invocation) {
    Sandbox sandbox;
    if (testMethod == null) {
      // For @BeforeAll/@AfterAll - use bootstrap environment
      sandbox = testRunner.bootstrapSdkEnvironment();
    } else {
      sandbox = testRunner.getSdkEnvironmentForMethod(testMethod);
    }
    
    runOnMainThreadWithRobolectric(sandbox, () -> {
      try {
        invocation.proceed();
      } catch (Throwable t) {
        if (t instanceof RuntimeException) {
          throw (RuntimeException) t;
        } else if (t instanceof Error) {
          throw (Error) t;
        } else {
          throw new RuntimeException(t);
        }
      }
      return null;
    });
  }

  private <T> T runOnMainThreadWithRobolectric(Sandbox sandbox, Callable<T> action) {
    return runOnMainThread(sandbox, () -> runWithRobolectric(sandbox, action));
  }

  private <T> T runOnMainThread(Sandbox sandbox, Callable<T> action) {
    return sandbox.runOnMainThread(action);
  }

  private <T> T runWithRobolectric(Sandbox sandbox, Callable<T> action) {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());
      return action.call();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
  }

  private void clearSandboxState(Sandbox sandbox) {
    try {
      // Clear shadow looper cache
      ClassLoader roboClassLoader = sandbox.getRobolectricClassLoader();
      Class<?> shadowLooperClass = roboClassLoader.loadClass("org.robolectric.shadows.ShadowLooper");
      Method clearMethod = shadowLooperClass.getDeclaredMethod("clearLooperMode");
      clearMethod.invoke(null);
      
      // Reset loopers
      resetMainLooper(roboClassLoader);
      resetMyLooper(roboClassLoader);
    } catch (Exception e) {
      // Log but don't fail the test
      System.err.println("Warning: Failed to clear sandbox state: " + e.getMessage());
    }
  }

  private void resetMainLooper(ClassLoader roboClassLoader) throws Exception {
    Class<?> looperClass = roboClassLoader.loadClass("android.os.Looper");
    java.lang.reflect.Field sMainLooperField = looperClass.getDeclaredField("sMainLooper");
    sMainLooperField.setAccessible(true);
    sMainLooperField.set(null, null);
    sMainLooperField.setAccessible(false);
  }

  private void resetMyLooper(ClassLoader roboClassLoader) throws Exception {
    Class<?> looperClass = roboClassLoader.loadClass("android.os.Looper");
    java.lang.reflect.Field sThreadLocalField = looperClass.getDeclaredField("sThreadLocal");
    sThreadLocalField.setAccessible(true);
    ThreadLocal<?> threadLocal = (ThreadLocal<?>) sThreadLocalField.get(null);
    threadLocal.remove();
    sThreadLocalField.setAccessible(false);
  }
}
