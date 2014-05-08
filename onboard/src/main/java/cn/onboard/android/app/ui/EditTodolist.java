package cn.onboard.android.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Todolist;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.core.todo.TodolistService;
import cn.onboard.android.app.ui.fragment.CommentListFragment;

/**
 * Created by XingLiang on 14-4-4.
 */

public class EditTodolist extends BaseActivity {

    private Integer todolistId;

    private String todolistName;

    private Integer companyId;

    private Integer projectId;

    private EditText todolistContent;

    private Todolist todolist;

    private TodolistService todolistService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todolist_edit);
        initService();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        companyId = getIntent().getIntExtra("companyId", -1);
        projectId = getIntent().getIntExtra("projectId", -1);
        todolistId = getIntent().getIntExtra("todolistId", -1);
        todolistName = getIntent().getStringExtra("todolistName");

        todolistContent = (EditText) findViewById(R.id.todolist_title);
        todolistContent.setText(todolistName);

        todolist = new Todolist();
        todolist.setName(todolistName);
        todolist.setCompanyId(companyId);
        todolist.setProjectId(projectId);
        todolist.setId(todolistId);

        initView();
    }

    void initService() {
        todolistService = new TodolistService((AppContext)getApplicationContext());
    }

    private final MenuItem.OnMenuItemClickListener saveTodolistListener = new MenuItem.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            new SaveTodolistTask().execute(todolist);
            return true;
        }
    };

    void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CommentListFragment commentList = new CommentListFragment(companyId, projectId, "todolist", todolistId);
        ft.replace(R.id.todolist_comments, commentList).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("保存").setOnMenuItemClickListener(saveTodolistListener).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;

    }

    private class SaveTodolistTask extends AsyncTask<Todolist, Void, Todolist> {

        @Override
        protected Todolist doInBackground(Todolist... todolist) {
            Todolist t = todolist[0];
            t.setName(todolistContent.getText() + "");
            AppContext ac = (AppContext) getApplication();
            try {

                Todolist sample = new Todolist();
                sample.setId(t.getId());
                sample.setName(t.getName());
                sample.setCompanyId(t.getCompanyId());
                sample.setProjectId(t.getProjectId());
                t = todolistService.updateTodolist(sample);
            } catch (RestClientException e) {
                e.printStackTrace();
            }
            return t;
        }

        @Override
        protected void onPostExecute(Todolist todolist) {
            Intent intent = getIntent();
            ObjectMapper m = new ObjectMapper();
            m.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);

            ObjectWriter ow = m.writer().withDefaultPrettyPrinter();
            try {
                String todolistjson = ow.writeValueAsString(todolist);
                intent.putExtra("type", "todolist");
                intent.putExtra("todolist", todolistjson);
                setResult(Activity.RESULT_OK, intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();//结束之后会将结果传回From
        }
    }
}
