package org.robolectric.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;

/**
 * Internal test runner that extends RobolectricTestRunner to work with JUnit5.
 * This class adapts the JUnit4 runner infrastructure to support JUnit5 tests.
 */
class JUnit5RobolectricTestRunner extends RobolectricTestRunner {
  private final List<FrameworkMethod> childrenCache = new ArrayList<>();
  private final ThreadLocal<Sandbox> currentSandbox = new ThreadLocal<>();

  public JUnit5RobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    if (childrenCache.isEmpty()) {
      synchronized (childrenCache) {
        if (childrenCache.isEmpty()) {
          childrenCache.addAll(super.getChildren());
        }
      }
    }
    return childrenCache;
  }

  /**
   * Find the FrameworkMethod that corresponds to the given test method.
   */
  public FrameworkMethod frameworkMethod(Method method) {
    for (FrameworkMethod fm : getChildren()) {
      if (fm.getMethod().getName().equals(method.getName())
          && fm.getMethod().getDeclaringClass().equals(method.getDeclaringClass())
          && hasTheSameParameterTypes(fm.getMethod(), method)) {
        return fm;
      }
    }
    throw new IllegalArgumentException("Could not find FrameworkMethod for " + method);
  }

  /**
   * Get the sandbox environment for a specific framework method.
   */
  public Sandbox sdkEnvironment(FrameworkMethod frameworkMethod) {
    Sandbox sandbox = getSandbox(frameworkMethod);
    configureSandbox(sandbox, frameworkMethod);
    currentSandbox.set(sandbox);
    return sandbox;
  }

  /**
   * Get the bootstrap SDK environment (for @BeforeAll/@AfterAll).
   */
  public Sandbox bootstrapSdkEnvironment() {
    if (!getChildren().isEmpty()) {
      return sdkEnvironment(getChildren().get(0));
    }
    throw new IllegalStateException("No test methods found");
  }

  /**
   * Get the sandbox for the given test method.
   */
  public Sandbox getSdkEnvironmentForMethod(Method testMethod) {
    Sandbox sandbox = currentSandbox.get();
    if (sandbox == null) {
      FrameworkMethod fm = frameworkMethod(testMethod);
      sandbox = sdkEnvironment(fm);
    }
    return sandbox;
  }

  /**
   * Run before test lifecycle.
   */
  public void runBeforeTest(Sandbox sandbox, FrameworkMethod frameworkMethod, Method testMethod) {
    try {
      Method bootstrappedMethod = getBootstrappedMethod(sandbox, testMethod);
      beforeTest(sandbox, frameworkMethod, bootstrappedMethod);
    } catch (Throwable t) {
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      } else if (t instanceof Error) {
        throw (Error) t;
      } else {
        throw new RuntimeException(t);
      }
    }
  }

  /**
   * Run after test lifecycle.
   */
  public void runAfterTest(FrameworkMethod frameworkMethod, Method testMethod) {
    try {
      Sandbox sandbox = currentSandbox.get();
      if (sandbox != null) {
        Method bootstrappedMethod = getBootstrappedMethod(sandbox, testMethod);
        afterTest(frameworkMethod, bootstrappedMethod);
      }
    } catch (Throwable t) {
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      } else if (t instanceof Error) {
        throw (Error) t;
      } else {
        throw new RuntimeException(t);
      }
    }
  }

  /**
   * Run finally after test lifecycle.
   */
  public void runFinallyAfterTest(Sandbox sandbox, FrameworkMethod frameworkMethod) {
    try {
      finallyAfterTest(frameworkMethod);
    } finally {
      currentSandbox.remove();
    }
  }

  @Override
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    InstrumentationConfiguration.Builder builder =
        new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method));
    // Exclude JUnit5 and this extension from instrumentation
    builder.doNotAcquirePackage("org.junit.jupiter.");
    builder.doNotAcquirePackage("org.robolectric.junit.");
    return builder.build();
  }

  @Override
  protected void validateNoNonStaticInnerClass(List<Throwable> errors) {
    // JUnit5 handles this validation
  }

  @Override
  protected boolean isIgnored(FrameworkMethod child) {
    // JUnit5 handles @Disabled annotation
    return false;
  }

  private Method getBootstrappedMethod(Sandbox sandbox, Method method)
      throws ClassNotFoundException, NoSuchMethodException {
    Class<?> bootstrappedClass = sandbox.bootstrappedClass(method.getDeclaringClass());
    return bootstrappedClass.getMethod(method.getName(), method.getParameterTypes());
  }

  private boolean hasTheSameParameterTypes(Method m1, Method m2) {
    Class<?>[] params1 = m1.getParameterTypes();
    Class<?>[] params2 = m2.getParameterTypes();
    if (params1.length != params2.length) {
      return false;
    }
    for (int i = 0; i < params1.length; i++) {
      if (!params1[i].equals(params2[i])) {
        return false;
      }
    }
    return true;
  }
}
