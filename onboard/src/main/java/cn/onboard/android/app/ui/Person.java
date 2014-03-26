package cn.onboard.android.app.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.fragment.ActivityFragment;
import cn.onboard.android.app.ui.fragment.TodoFragment;
import cn.onboard.android.app.ui.fragment.UploadFragment;

/**
 * 用户登录页面
 */
public class Person extends FragmentActivity {


    private final int INVALID_USER_ID = -1;
    private Button meActivityButton;
    private Button meTodoButton;
    private Button meFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person);
        meActivityButton = (Button) findViewById(R.id.frame_btn_me_acivities);
        meTodoButton = (Button) findViewById(R.id.frame_btn_me_todos);
        meFileButton = (Button) findViewById(R.id.frame_btn_me_files);
        final Integer companyId = getIntent().getIntExtra("companyId", 0);
        final Integer userId = getIntent().getIntExtra("userId", 0);
        getSupportFragmentManager().beginTransaction().replace(R.id.me, new ActivityFragment(null, userId)).commit();
        meActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new ActivityFragment(null, userId)).commit();
            }
        });
        meTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new TodoFragment(companyId, null, userId)).commit();
            }
        });
        meFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new UploadFragment(companyId, null, userId)).commit();
            }
        });
    }
}
