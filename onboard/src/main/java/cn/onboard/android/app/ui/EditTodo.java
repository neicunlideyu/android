package cn.onboard.android.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Todo;
import com.onboard.api.dto.Todolist;
import com.onboard.api.dto.User;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.fragment.CommentListFragment;

public class EditTodo extends SherlockFragmentActivity {

    private Todolist todolist;
    private Todo todo;
    private int todolistId;
    private EditType editType;
    private int todoId;
    private int companyId;
    private int projectId;

    private List<User> assigneesList;
    private TextView assigneeText;
    private TextView assigneeDateText;
    private EditText todoContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        assigneeText = (TextView) findViewById(R.id.tv_assignee_name);
        assigneeDateText = (TextView) findViewById(R.id.tv_assignee_date);
        todoContent = (EditText) findViewById(R.id.todo_title);
        companyId = getIntent().getIntExtra("companyId", -1);
        projectId = getIntent().getIntExtra("projectId", -1);
        todolistId = getIntent().getIntExtra("todolistId", -1);
        todoId = getIntent().getIntExtra("todoId", -1);
        editType = EditType.valueOf(getIntent().getIntExtra("editType", EditType.CREATE.value));
        initView();
        initData();

    }

    private final MenuItem.OnMenuItemClickListener saveTodoListener = new MenuItem.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            new SaveTodoaTask().execute(todo);
            return true;
        }
    };

    void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //指派人text框初始化
        findViewById(R.id.ll_assignee).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new AssigneeDialogFragment();
                        newFragment.show(getFragmentManager(), "assignees");
                    }
                });
        //截止日期text初始化
        findViewById(R.id.ll_assigndate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                if (todo.getDueDate() != null)
                    c.setTime(todo.getDueDate());
                final DatePickerDialog dateDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                        DateTime date = new DateTime().withDate(year, monthOfYear + 1, dayOfMonth);
                        assigneeDateText.setText(DateTimeFormat.forPattern("yyyy-MM-dd").print(date.getMillis()));
                        todo.setDueDate(date.toDate());
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dateDialog.show();
            }
        });

        //如果是编辑todo，显示评论列表
        if (editType.equals(EditType.UPDATE)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            CommentListFragment commentList = new CommentListFragment(companyId, projectId, "todo", todoId);
            ft.replace(R.id.todo_comments, commentList).commit();
        } else
            findViewById(R.id.comment_footer).setVisibility(View.GONE);
    }

    void initData() {
        todo = new Todo();
        getAssigeesList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("保存").setOnMenuItemClickListener(saveTodoListener).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    String[] getAssigneeNameList() {
        List<String> names = new ArrayList<String>();
        for (User user : assigneesList) {
            names.add(user.getName());
        }
        names.add("不指定");
        String[] array = new String[names.size()];
        names.toArray(array); // fill the array
        return array;

    }

    void getAssigeesList() {
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    final AppContext ac = (AppContext) getApplication();
                    assigneesList = ac.getUsersByProjectId(companyId, projectId);
                    if (editType.equals(EditType.UPDATE))
                        new GetTodoTask().execute();
                    if (editType.equals(EditType.CREATE))
                        new GetTodolistTask().execute();
                    msg.what = 1;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }

            }
        }.start();
    }

    public enum EditType {
        CREATE(0), UPDATE(1);

        private int value = 0;

        private EditType(int value) {
            this.value = value;
        }

        public static EditType valueOf(int value) {
            switch (value) {
                case 0:
                    return CREATE;
                case 1:
                    return UPDATE;
                default:
            }
            return null;
        }

        public int value() {
            return this.value;
        }
    }

    private class SaveTodoaTask extends AsyncTask<Todo, Void, Todo> {

        @Override
        protected Todo doInBackground(Todo... todo) {
            Todo t = todo[0];
            t.setContent(todoContent.getText() + "");
            AppContext ac = (AppContext) getApplication();
            try {
                if (editType.equals(EditType.CREATE))
                    t = ac.createTodo(t);
                else {
                    Todo sample = new Todo();
                    sample.setId(t.getId());
                    sample.setContent(t.getContent());
                    sample.setAssigneeId(t.getAssigneeId());
                    sample.setDueDate(t.getDueDate());
                    sample.setCompanyId(t.getCompanyId());
                    sample.setProjectId(t.getProjectId());
                    sample.setTodolistId(t.getTodolistId());
                    t = ac.updateTodo(sample);
                }
            } catch (AppException e) {
                e.printStackTrace();
            }
            return t;
        }

        @Override
        protected void onPostExecute(Todo todo) {
            Intent intent = getIntent();
            ObjectMapper m = new ObjectMapper();
            m.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);

            ObjectWriter ow = m.writer().withDefaultPrettyPrinter();
            try {
                String todojson = ow.writeValueAsString(todo);
                intent.putExtra("todo", todojson);
                setResult(Activity.RESULT_OK, intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();//结束之后会将结果传回From
        }
    }

    private class GetTodolistTask extends AsyncTask<Void, Void, Todolist> {

        @Override
        protected Todolist doInBackground(Void... params) {
            Todolist t = new Todolist();
            AppContext ac = (AppContext) getApplication();
            try {
                t = ac.getTodolistById(companyId, projectId, todolistId);
                todo.setProjectId(t.getProjectId());
                todo.setCompanyId(t.getCompanyId());
                todo.setTodolistId(t.getId());
                todo.setDeleted(false);
                todo.setPosition((double) t.getTodos().size() * 100);

            } catch (AppException e) {
                e.printStackTrace();
            }
            return t;
        }

        @Override
        protected void onPostExecute(Todolist t) {
            todolist = t;
            getSupportActionBar().setTitle("任务列表/" + todolist.getName());
        }
    }


    private class GetTodoTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Todo t = new Todo();
            AppContext ac = (AppContext) getApplication();
            try {
                t = ac.getTodoById(companyId, projectId, todoId);
                todo = t;
                todolistId = todo.getTodolistId();
            } catch (AppException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
           // getSupportActionBar().setTitle("任务列表/" + todo.getTodolistName());
            if (todo.getDueDate() != null)
                assigneeDateText.setText(DateTimeFormat.forPattern("yyyy-MM-dd").print(todo.getDueDate().getTime()));
            if (todo.getContent() != null)
                todoContent.setText(todo.getContent());
            if (todo.getAssigneeId() != null) {
                for (User user : assigneesList) {
                    if (user.getId().equals(todo.getAssigneeId()))
                        assigneeText.setText(user.getName());
                }
            }

        }
    }

    private class AssigneeDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //没有指定负责人,默认选为不指定
            int selectedPosition = assigneesList.size();

            //获得初始负责人的位置，set radio checked
            for (User user : assigneesList) {
                if (user.getId().equals(todo.getAssigneeId()))
                    selectedPosition = assigneesList.indexOf(user);
            }
            builder.setTitle("选择负责人")
                    .setSingleChoiceItems(getAssigneeNameList(), selectedPosition, null)
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                    if (selectedPosition < assigneesList.size()) {
                                        assigneeText.setText(assigneesList.get(selectedPosition).getName());
                                        todo.setAssigneeId(assigneesList.get(selectedPosition).getId());
                                    } else {
                                        assigneeText.setText("无");
                                        todo.setAssigneeId(null);
                                    }
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                }
                            });

            return builder.create();
        }
    }


}
