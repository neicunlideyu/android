package cn.onboard.android.app.intergrationtest;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.squareup.spoon.Spoon;

import cn.onboard.android.app.ui.Login;


/**
 * Tests for MainActivity.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<Login> {


  public LoginActivityTest() {
    super(Login.class);
  }

  @UiThreadTest
  public void testSetText() throws Throwable {
    final Login act = getActivity();
    Spoon.screenshot(act, "startup");

  }

}
