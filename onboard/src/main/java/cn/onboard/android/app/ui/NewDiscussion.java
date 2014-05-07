package cn.onboard.android.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.common.base.Strings;
import com.onboard.api.dto.Discussion;
import com.onboard.api.dto.User;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.core.discussion.DiscussionService;
import cn.onboard.android.app.core.user.UserService;

public class NewDiscussion extends BaseActivity {
    private TextView discussionTitleTextView;
    private TextView discussionContentTextView;
    private int companyId;
    private int projectId;
    private Discussion discussion;
    private List<User> assigneesList;
    private boolean[] ifAssigned;
    private final static int SELECT_ALL_POS = 0;
    private ListView AlertMultiView = null;
    private DiscussionService discussionService;
    private UserService userService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussion_edit);
        findViewById(R.id.discussion_assignee_name).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DiscussionAssigneeDialogFragment();
                newFragment.show(getFragmentManager(), "assignees");
            }

        });
        initService();
        initView();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("新建讨论");
    }

    private void initService() {
        discussionService = new DiscussionService();
        userService = new UserService();
    }

    private String[] getAssigneeNameList() {
        List<String> names = new ArrayList<String>();
        names.add("全选");
        for (User user : assigneesList) {
            names.add(user.getName());
        }
        String[] array = new String[names.size()];
        names.toArray(array);
        return array;

    }

    private void initView() {
        discussionTitleTextView = (TextView) findViewById(R.id.discussion_title);
        discussionContentTextView = (TextView) findViewById(R.id.discussion_content);
        companyId = getIntent().getIntExtra("companyId", 0);
        projectId = getIntent().getIntExtra("projectId", 0);
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

    void getAssigeesList() {
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    assigneesList = userService.getUsersByProjectId(companyId, projectId);
                    msg.what = 1;
                    ifAssigned = new boolean[assigneesList.size() + 1];
                    for (int i = 0; i < assigneesList.size(); i++) {
                        ifAssigned[i] = false;
                    }
                } catch (RestClientException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }

            }
        }.start();
    }

    private String getAssignNameListString() {
        String result = "";
        int i = SELECT_ALL_POS + 1;
        for (User assignee : this.assigneesList) {
            if (ifAssigned[i++]) {
                result += (assignee.getName() + ',');
            }
        }
        return result;
    }

    private final OnMenuItemClickListener popupListener = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // View v=item.get
            // 隐藏软键盘
            setSupportProgressBarIndeterminateVisibility(true);
            String discussionTitle = discussionTitleTextView.getText().toString();
            String discussionContent = discussionContentTextView.getText().toString();
            if (Strings.isNullOrEmpty(discussionTitle)) {
                UIHelper.ToastMessage(getApplicationContext(), "请输入讨论主题");
                return false;
            }
            if (Strings.isNullOrEmpty(discussionContent)) {
                UIHelper.ToastMessage(getApplicationContext(), "请输入讨论内容");
                return false;
            }

            final AppContext ac = (AppContext) getApplication();

            discussion = new Discussion();
            discussion.setSubject(discussionTitle);
            discussion.setContent(discussionContent);
            discussion.setCompanyId(companyId);
            discussion.setProjectId(projectId);
            discussion.setSubscribers(getSubscribes());

            // comment.setCompanyId(companyId);
            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {

                    setSupportProgressBarIndeterminateVisibility(false);
                    if (msg.what == 1) {
                        Intent intent = getIntent();
                        ObjectMapper m = new ObjectMapper();
                        m.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
                        ObjectWriter ow = m.writer().withDefaultPrettyPrinter();
                        try {
                            String discussionJson = ow.writeValueAsString(discussion);
                            intent.putExtra("discussion", discussionJson);
                            setResult(Activity.RESULT_OK, intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finish();//结束之后会将结果传回From
                        return;
                    } else {
                        UIHelper.ToastMessage(NewDiscussion.this, "创建讨论失败");
                    }
                }
            };
            new Thread() {
                public void run() {
                    Message msg = new Message();

                    try {
                        discussion = discussionService.createDiscussion(discussion);
                        msg.what = 1;
                    } catch (RestClientException e) {
                        e.printStackTrace();
                        msg.what = -1;
                        msg.obj = e;
                    }
                    handler.sendMessage(msg);
                }
            }.start();
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("发表").setOnMenuItemClickListener(popupListener).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }

    private class DiscussionAssigneeDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String item[] = getAssigneeNameList();
            final AlertDialog thisDialog = builder.setTitle("选择要通知的人")
                    .setMultiChoiceItems(item, ifAssigned, new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (SELECT_ALL_POS == which) {
                                for (int i = 0; i < assigneesList.size() + 1; i++) {
                                    AlertMultiView.setItemChecked(i, isChecked);
                                    ifAssigned[i] = isChecked;

                                }
                            } else {
                                ifAssigned[which] = isChecked;
                            }
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TextView assignTextView = (TextView) findViewById(R.id.discussion_assignee_name);
                            assignTextView.setText(getAssignNameListString());

                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    }).create();
            AlertMultiView = thisDialog.getListView();
            return thisDialog;
        }
    }

    private List<User> getSubscribes() {
        List<User> subscribes = new ArrayList<User>();
        for (int i = 1; i < assigneesList.size() + 1; i++) {
            if (ifAssigned[i]) {
                subscribes.add(assigneesList.get(i - 1));
            }
        }
        return subscribes;
    }

}