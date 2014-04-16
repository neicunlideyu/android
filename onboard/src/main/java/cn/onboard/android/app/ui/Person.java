package cn.onboard.android.app.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.onboard.api.dto.User;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.ui.fragment.ActivityFragment;
import cn.onboard.android.app.ui.fragment.AttachmentFragment;
import cn.onboard.android.app.ui.fragment.TodoFragment;

/**
 * 用户登录页面
 */
public class Person extends SherlockFragmentActivity {


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
        String userName = getIntent().getStringExtra("userName");
        getSupportFragmentManager().beginTransaction().replace(R.id.me, new ActivityFragment(companyId, userId)).commit();
        meActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new ActivityFragment(companyId, userId)).commit();
            }
        });
        meTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new TodoFragment(companyId, null, userId, null)).commit();
            }
        });
        meFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.me, new AttachmentFragment(companyId, null, userId)).commit();
            }
        });
        new GetUserTask().execute(userId);
    }

    private class GetUserTask extends AsyncTask<Integer, Void, User> {

        @Override
        protected User doInBackground(Integer... userId) {
            AppContext ac = (AppContext) getApplication();
            User user=new User();
            try {
                user = ac.getUserById(userId[0]);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            getSupportActionBar().setTitle("成员/"+user.getName());
//            String faceURL = URLs.USER_FACE_HTTP + user.getAvatar();
//            bitmapManager.loadBitmap(faceURL, (ImageView) findViewById(R.id.homeAsUp));
        }
    }

}
