package cn.onboard.android.app.ui;


import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppManager;
import cn.onboard.android.app.api.ApiClient;


class BaseActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
        setUpWebSocket();
    }

    private void setUpWebSocket() {

        AsyncHttpGet req=new AsyncHttpGet("http://192.168.100.37:8080/api/websocket");
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

}
