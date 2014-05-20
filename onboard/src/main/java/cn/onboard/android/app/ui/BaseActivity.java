package cn.onboard.android.app.ui;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;
import com.onboard.api.dto.User;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.AppManager;
import cn.onboard.android.app.R;
import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.common.UIHelper;

public class BaseActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
        setUpWebSocket();
    }

    private void setUpWebSocket() {

        AsyncHttpGet req = new AsyncHttpGet("http://192.168.100.37:8080/api/websocket");
        req.setHeader("Cookie", ApiClient.getCookie((AppContext) getApplication()));
        AsyncHttpClient.getDefaultInstance().websocket(req, "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String activity) {
                        Intent intent = new Intent("cn.onboard.android.app.action.ACTIVITY_MESSAGE");
                        intent.putExtra("activity", activity);
                        sendBroadcast(intent);
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //结束Activity&从堆栈中移除
        AppManager.getAppManager().finishActivity(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
//                Intent intent = new Intent(this, Setting.class);
//                startActivity(intent);
                return true;
            case R.id.log_out:
                new GetProjectListTask().execute();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetProjectListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            AppContext ac = (AppContext) getApplication();
            try {
                User user = ac.logOut();
                if (user != null) {
                    ac.setProperty("remember_me", "false");
                    Intent intent = new Intent(BaseActivity.this, Login.class);
                    startActivity(intent);
                }
            } catch (AppException e) {
                UIHelper.ToastMessage(BaseActivity.this, "注销失败");
            }
            return null;
        }
    }
}
