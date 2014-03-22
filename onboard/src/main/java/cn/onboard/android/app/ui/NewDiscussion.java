package cn.onboard.android.app.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.onboard.api.dto.Discussion;
import com.onboard.api.dto.User;

import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.StringUtils;
import cn.onboard.android.app.common.UIHelper;

public class NewDiscussion extends SherlockActivity {
    private InputMethodManager imm;
    private TextView discussionTitleTextView;
    private TextView discussionContentTextView;
    private int companyId;
    private int projectId;
    private Discussion discussion;
    private List<User> assigneesList;
    private boolean[] ifAssigned;
    private final static int SELECT_ALL_POS = 0;
    private ListView AlertMultiView = null;

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
        initView();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("新建讨论");
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
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
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
                    final AppContext ac = (AppContext) getApplication();
                    assigneesList = ac.getUsersByProjectId(companyId, projectId);
                    msg.what = 1;
                    ifAssigned = new boolean[assigneesList.size() + 1];
                    for (int i = 0; i < assigneesList.size(); i++) {
                        ifAssigned[i] = false;
                    }
                } catch (AppException e) {
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

    private OnMenuItemClickListener popupListener = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // View v=item.get
            // 隐藏软键盘
            setSupportProgressBarIndeterminateVisibility(true);
            String discussionTitle = discussionTitleTextView.getText().toString();
            String discussionContent = discussionContentTextView.getText().toString();
            if (StringUtils.isEmpty(discussionTitle)) {
                UIHelper.ToastMessage(getApplicationContext(), "请输入讨论主题");
                return false;
            }
            if (StringUtils.isEmpty(discussionContent)) {
                UIHelper.ToastMessage(getApplicationContext(), "请输入讨论内容");
                return false;
            }

            final AppContext ac = (AppContext) getApplication();

            discussion = new Discussion();
            discussion.setSubject(discussionTitle);
            discussion.setContent(discussionContent);
            discussion.setCompanyId(companyId);
            discussion.setProjectId(projectId);

            // comment.setCompanyId(companyId);
            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {

                    setSupportProgressBarIndeterminateVisibility(false);
                    if (msg.what == 1) {
                        /*
                         * Context context = getApplicationContext(); Intent intent = new Intent(context,
                         * com.onboard.app.ui.DiscussionDetail.class); intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         * intent.putExtra("discussionId", discussion.getId()); intent.putExtra("companyId", companyId);
                         * intent.putExtra("projectId", projectId); intent.putExtra("discussionTitle", discussion.getSubject());
                         * context.startActivity(intent);
                         */
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
                        discussion = ac.createDiscussion(discussion);
                        msg.what = 1;
                    } catch (AppException e) {
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

}