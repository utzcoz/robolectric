package org.robolectric.integrationtests.junit5;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricJUnit5Runner;

/** Basic JUnit5 integration test using {@link RobolectricJUnit5Runner}. */
@ExtendWith(RobolectricJUnit5Runner.class)
public class BasicJUnit5Test {

  private Context context;

  @BeforeEach
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @AfterEach
  public void tearDown() {
    // Cleanup if needed
  }

  @Test
  public void testApplicationContext() {
    Application application = ApplicationProvider.getApplicationContext();
    assertThat(application).isNotNull();
    assertThat(application.getPackageName()).isEqualTo("org.robolectric.integrationtests.junit5");
  }

  @Test
  public void testSdkVersion() {
    // Default SDK should be available
    assertThat(Build.VERSION.SDK_INT).isGreaterThan(0);
  }

  @Test
  public void testContextOperations() {
    assertThat(context).isNotNull();
    assertThat(context.getPackageName()).isNotNull();
    assertThat(context.getResources()).isNotNull();
  }

  @Test
  public void testCreateActivity() {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    assertThat(activity).isNotNull();
    assertThat(activity.isFinishing()).isFalse();
  }

  @Test
  public void testTextView() {
    TextView textView = new TextView(context);
    textView.setText("Hello JUnit5!");
    assertThat(textView.getText().toString()).isEqualTo("Hello JUnit5!");
  }
}
