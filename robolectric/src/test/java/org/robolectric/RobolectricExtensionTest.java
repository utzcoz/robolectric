package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Test for {@link RobolectricExtension} and {@link RobolectricJUnit5Runner}. */
@ExtendWith(RobolectricJUnit5Runner.class)
public class RobolectricExtensionTest {

  @Test
  public void testApplicationContext() {
    Application application = ApplicationProvider.getApplicationContext();
    assertThat(application).isNotNull();
  }

  @Test
  public void testSdkVersion() {
    // Default SDK should be available
    assertThat(Build.VERSION.SDK_INT).isGreaterThan(0);
  }

  @Test
  public void testContextOperations() {
    Context context = ApplicationProvider.getApplicationContext();
    assertThat(context.getPackageName()).isNotNull();
  }
}
