package cn.onboard.android.app.ui;


import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import cn.onboard.android.app.AppManager;


class BaseActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //结束Activity&从堆栈中移除
        AppManager.getAppManager().finishActivity(this);
    }

}
