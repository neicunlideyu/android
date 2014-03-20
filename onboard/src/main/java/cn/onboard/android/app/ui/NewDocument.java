package cn.onboard.android.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.onboard.api.dto.Document;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.StringUtils;
import cn.onboard.android.app.common.UIHelper;

public class NewDocument extends SherlockActivity {
	private InputMethodManager imm;
	private TextView discussionTitleTextView;
	private TextView discussionContentTextView;
	private int companyId;
	private int projectId;
	private Document document;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussion_edit);
		initView();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("新建文档");
	}

	void initView() {
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		discussionTitleTextView = (TextView) findViewById(R.id.discussion_title);
		discussionContentTextView = (TextView) findViewById(R.id.discussion_content);
		companyId = getIntent().getIntExtra("companyId", 0);
		projectId = getIntent().getIntExtra("projectId", 0);
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

	private OnMenuItemClickListener popupListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			//View v=item.get
			// 隐藏软键盘
			setSupportProgressBarIndeterminateVisibility(true);
			String discussionTitle = discussionTitleTextView.getText()
					.toString();
			String discussionContent = discussionContentTextView.getText()
					.toString();
			if (StringUtils.isEmpty(discussionTitle)) {
				UIHelper.ToastMessage(getApplicationContext(), "请输入讨论主题");
				return false;
			}
			if (StringUtils.isEmpty(discussionContent)) {
				UIHelper.ToastMessage(getApplicationContext(), "请输入讨论内容");
				return false;
			}

			final AppContext ac = (AppContext) getApplication();

			document = new Document();
			document.setTitle(discussionTitle);
			document.setContent(discussionContent);
			document.setCompanyId(companyId);
			document.setProjectId(projectId);

			// comment.setCompanyId(companyId);
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {

					setSupportProgressBarIndeterminateVisibility(false);
					if (msg.what == 1) {
						Context context = getApplicationContext();
						Intent intent = new Intent(context,
								DocumentDetail.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("documentId", document.getId());
						intent.putExtra("companyId", companyId);
						intent.putExtra("projectId", projectId);
						intent.putExtra("discussionTitle",
								document.getTitle());
						context.startActivity(intent);
						return;
					} else {
						UIHelper.ToastMessage(NewDocument.this, "创建讨论失败");
					}
				}
			};
			new Thread() {
				public void run() {
					Message msg = new Message();
					try {
						document=ac.createDocument(document);
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

		menu.add("评论").setOnMenuItemClickListener(popupListener)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }

}
