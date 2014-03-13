package cn.onboard.android.app.ui;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ViewSwitcher;

import com.onboard.domain.model.User;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.common.StringUtils;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.BaseActivity;

/**
 * 用户登录页面
 */
public class Login extends BaseActivity {

    private ViewSwitcher viewSwitcher;
    private Button btn_login;
    private AutoCompleteTextView email;
    private EditText password;
    private AnimationDrawable loadingAnimation;
    private View loginLoading;
    private CheckBox chb_rememberMe;
    private InputMethodManager imm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.logindialog_view_switcher);
        loginLoading = (View) findViewById(R.id.login_loading);
        email = (AutoCompleteTextView) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        chb_rememberMe = (CheckBox) findViewById(R.id.rememberMe);

        btn_login = (Button) findViewById(R.id.login_submit_button);
        btn_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String account = email.getText().toString();
                String pwd = password.getText().toString();
                boolean isRememberMe = chb_rememberMe.isChecked();
                // 判断输入
                if (StringUtils.isEmpty(account)) {
                    UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_email_null));
                    return;
                }
                if (StringUtils.isEmpty(pwd)) {
                    UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_pwd_null));
                    return;

                }
                loadingAnimation = (AnimationDrawable) loginLoading.getBackground();
                loadingAnimation.start();
                viewSwitcher.showNext();

                login(account, pwd, isRememberMe);
            }
        });

        // 是否显示登录信息
        AppContext ac = (AppContext) getApplication();
        User user = ac.getLoginInfo();
        if (user == null)
            return;
        if (!StringUtils.isEmpty(user.getEmail())) {
            email.setText(user.getEmail());
            email.selectAll();
        }
        if (!StringUtils.isEmpty(user.getPassword())) {
            password.setText(user.getPassword());
        }
    }

    // 登录验证
    private void login(final String account, final String pwd, final boolean isRememberMe) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    User user = (User) msg.obj;
                    if (user != null) {
                        // 清空原先cookie
                        ApiClient.cleanCookie();
                        // 提示登陆成功
                        UIHelper.ToastMessage(Login.this, R.string.msg_login_success);
                        Intent intent = new Intent(Login.this, SelectCompany.class);
                        intent.putExtra("LOGIN", true);
                        startActivity(intent);
                        finish();

                    }
                } else if (msg.what == 0) {
                    viewSwitcher.showPrevious();
                    UIHelper.ToastMessage(Login.this, getString(R.string.msg_login_fail));
                } else if (msg.what == -1) {
                    viewSwitcher.showPrevious();
                    ((AppException) msg.obj).makeToast(Login.this);
                }
            }
        };

        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    AppContext ac = (AppContext) getApplication();
                    User user = ac.loginVerify(account, pwd);
                    if (user != null) {
                    	user.setPassword(pwd);
                        ac.saveLoginInfo(user);// 保存登录信息
                        msg.what = 1;// 成功
                        msg.obj = user;
                    } else {
                        ac.cleanLoginInfo();// 清除登录信息
                        msg.what = 0;// 失败
                        // msg.obj = res.getErrorMessage();
                    }
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }
}
