package cn.onboard.android.app.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.common.base.Strings;
import com.onboard.api.dto.User;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.common.UIHelper;

/**
 * 用户登录页面
 */
public class Login extends BaseActivity {

    private AppContext ac;

    private Button btn_login;
    private AutoCompleteTextView email;
    private EditText password;
    private CheckBox rememberMe;
    private InputMethodManager imm;
    private View login_table;
    private ProgressBar loading;

    private String account;
    private String pwd;
    private Boolean isRememberMe;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ac = (AppContext)getApplication();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        loading = (ProgressBar) findViewById(R.id.loading_progress_bar);
        email = (AutoCompleteTextView) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        rememberMe = (CheckBox) findViewById(R.id.rememberMe);
        login_table = findViewById(R.id.login_table);
        btn_login = (Button) findViewById(R.id.login_submit_button);
        btn_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                account = email.getText().toString();
                pwd = password.getText().toString();
                isRememberMe = rememberMe.isChecked();
                // 判断输入
                if (Strings.isNullOrEmpty(account)) {
                    UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_email_null));
                    return;
                }
                if (Strings.isNullOrEmpty(pwd)) {
                    UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_pwd_null));
                    return;
                }
                login_table.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                new LoginTask().execute();
            }
        });

        // 是否显示登录信息
        User user = ac.getLoginInfo();
        if (user == null)
            return;
        if (!Strings.isNullOrEmpty(user.getEmail())) {
            email.setText(user.getEmail());
            email.selectAll();
        }
        if (!Strings.isNullOrEmpty(user.getPassword())) {
            password.setText(user.getPassword());
        }
        if (Boolean.parseBoolean(ac.getProperty("remember_me")))
            btn_login.performClick();
    }


    public class LoginTask extends AsyncTask<Void, Void, User> {

        @Override
        protected User doInBackground(Void... params) {
            User user = null;
            try {
                user = ac.loginVerify(account, pwd);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                // 清空原先cookie
                ApiClient.cleanCookie();
                user.setPassword(pwd);
                ac.saveLoginInfo(user);
                ac.setProperty("remember_me", isRememberMe.toString());
                // 提示登陆成功
                UIHelper.ToastMessage(Login.this, R.string.msg_login_success);
                Intent intent = new Intent(Login.this, SelectCompany.class);
                intent.putExtra("LOGIN", true);
                startActivity(intent);
                finish();
            } else {
                UIHelper.ToastMessage(Login.this, getString(R.string.msg_login_fail));
                loading.setVisibility(View.GONE);
                login_table.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }
}
