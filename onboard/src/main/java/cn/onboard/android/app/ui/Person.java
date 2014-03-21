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
        getSupportFragmentManager().beginTransaction().replace(R.id.me,new ActivityFragment(1234252,0)).commit();
        meActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me,new ActivityFragment(1234252,0)).commit();
            }
        });
        meTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new TodoFragment(1234252, 5532506)).commit();
            }
        });
        meFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new UploadFragment(1234252, 5532506)).commit();
            }
        });
    }
}
