package cn.onboard.android.app.intergrationtest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.spoon.Spoon;

import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.Login;
import cn.onboard.android.app.ui.SelectCompany;

import static org.fest.assertions.api.ANDROID.assertThat;



/**
 * Tests for MainActivity.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<Login> {


    private Instrumentation instrumentation;
    private Login activity;

    private EditText username;
    private EditText password;
    private Button login;

    public LoginActivityTest() {
        super(Login.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        instrumentation = getInstrumentation();
        activity = getActivity();

        username = (EditText) activity.findViewById(R.id.userName);
        password = (EditText) activity.findViewById(R.id.password);
        login = (Button) activity.findViewById(R.id.login_submit_button);
    }

    public void testEmptyForm_ShowsBothErrors() {

        Spoon.screenshot(activity, "initial_state");

        // Make sure the initial state does not show any errors.
        assertThat(username).hasNoError();
        assertThat(password).hasNoError();

        instrumentation.runOnMainSync(new Runnable() {
            @Override public void run() {
                username.setText("");
                password.setText("");
            }
        });

        // Click the "login" button.
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                login.performClick();
            }
        });
        instrumentation.waitForIdleSync();
        instrumentation.callActivityOnRestart(activity);

        View view = activity.findViewById(R.id.login_user_table);
        assertThat(view).isNotNull();

        Spoon.screenshot(activity, "login_clicked");

    }

    public void testValidValues_StartsNewActivity() throws InterruptedException {
        IntentFilter filter = new IntentFilter();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(filter, null, false);

        Spoon.screenshot(activity, "initial_state");

        // Make sure the initial state does not show any errors.
        assertThat(username).hasNoError();
        assertThat(password).hasNoError();

        // Type a value into the username and password field.
        instrumentation.runOnMainSync(new Runnable() {
            @Override public void run() {
                username.setText("xuchen109@gmail.com");
                password.setText("12345678");
            }
        });
        Spoon.screenshot(activity, "values_entered");

        // Click the "login" button.
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                login.performClick();
            }
        });

        instrumentation.removeMonitor(monitor);
        monitor = instrumentation.addMonitor(SelectCompany.class.getName(), null, false);

        Activity currentActivity = getInstrumentation().waitForMonitorWithTimeout(monitor, 5000);

        Spoon.screenshot(currentActivity, "next_activity_shown");
        View currentView = currentActivity.findViewById(cn.onboard.android.app.R.id.company_list);
        assertThat(currentView).isNotNull();
    }

}
