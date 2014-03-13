package cn.onboard.android.app.ui;

import android.os.Bundle;

import cn.onboard.android.app.R;

/**
 * 应用程序Activity的基类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-9-18
 */
public class CommentList extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

}
