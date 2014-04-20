package cn.onboard.android.app.ui;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
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
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.onboard.api.dto.User;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.bean.URLs;
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
    private AlertDialog updateNotificationDialog;

    private String account;
    private String pwd;
    private Boolean isRememberMe;
    private long apkDownloadReference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ac = (AppContext) getApplication();
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
        if (ac.getProperty("remember_me") != null && Boolean.parseBoolean(ac.getProperty("remember_me")))
            btn_login.performClick();
        checkVersion();
    }

    private void checkVersion() {
        DialogInterface.OnClickListener toUpdateListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadNewVersion();
            }
        };

        DialogInterface.OnClickListener notUpdateListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        updateNotificationDialog = new AlertDialog.Builder(Login.this)
                .setTitle("软件更新")
                .setMessage("有新版本是否更新")
                .setPositiveButton("确定", toUpdateListener)
                .setNegativeButton("暂不更新", notUpdateListener).create();

        new CheckVersionTask().execute();
    }

    private void downloadNewVersion() {
        final AppContext appContext = (AppContext) getApplication();
        final DownloadManager downloadManager = (DownloadManager) appContext.getSystemService(Context.DOWNLOAD_SERVICE);
        String newUrl = URLs.LATEST_VERSION_DOWNLOAD_HTTP;

        Uri uri = Uri.parse(newUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("onboard更新");
        request.setDestinationInExternalFilesDir(appContext, null, "onboard.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        apkDownloadReference = downloadManager.enqueue(request);
        UIHelper.ToastMessage(appContext, "已进入后台下载");

        checkIfdownloaded(downloadManager);


    }

    private void checkIfdownloaded(final DownloadManager downloadManager) {

        final DialogInterface.OnClickListener toInstallListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installNewVersion(downloadManager);
            }
        };

        final DialogInterface.OnClickListener notInstallListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        final AlertDialog installNotificationDialog = new AlertDialog.Builder(Login.this)
                .setTitle("安装")
                .setMessage("已下载完成是否更新")
                .setPositiveButton("确定", toInstallListener)
                .setNegativeButton("暂不更新", notInstallListener).create();

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (reference == apkDownloadReference) {
                    installNotificationDialog.show();

                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private void installNewVersion(DownloadManager downloadManager) {
        DownloadManager.Query updateDownloadQuery = new DownloadManager.Query();
        updateDownloadQuery.setFilterById(apkDownloadReference);
        Cursor thisDownload = downloadManager.query(updateDownloadQuery);
        String fileName = "onboard.apk";
        if (thisDownload.moveToFirst()) {
            int fileUriIndex = thisDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            fileName = thisDownload.getString(fileUriIndex);
        }
        Intent install = new Intent();
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.setAction(Intent.ACTION_VIEW);
        install.setDataAndType(Uri.parse(fileName),
                "application/vnd.android.package-archive");
        startActivity(install);
    }

    private class LoginTask extends AsyncTask<Void, Void, User> {

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

    private class CheckVersionTask extends AsyncTask<String, Integer, String> {

        AppContext appContext;

        @Override
        protected String doInBackground(String... params) {
            appContext = (AppContext) getApplication();
            apkDownloadReference = 0;
            int currentVersionCode = appContext.getPackageInfo().versionCode;
            Integer lastestVersionCode = currentVersionCode;
            try {
                lastestVersionCode = appContext.getLatestVersionCode();
            }
            catch (AppException e) {
                e.printStackTrace();
            }
            return lastestVersionCode > currentVersionCode ? "yes" : "no";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.contains("yes")) {
                updateNotificationDialog.show();
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
