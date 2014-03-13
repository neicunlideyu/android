package cn.onboard.android.app.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.onboard.domain.model.Comment;
import com.onboard.plugin.wiki.model.Document;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;

public class DocumentDetail extends SherlockActivity {
	private FrameLayout mHeader;
	private LinearLayout mFooter;
	private ImageView mRefresh;
	private ProgressBar mProgressbar;
	private ScrollView mScrollView;


	private TextView mAuthor;
	private TextView mPubDate;
	private TextView mCommentCount;

	private WebView mWebView;
	private Handler mHandler;
	private Document document;

	private int documentId;
	private int companyId;
	private int projectId;
	private String documentTitle;


	private GestureDetector gd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_detail);

		this.initView();
		this.initData();

		// 注册双击全屏事件
//		this.regOnDoubleEvent();
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setTitle(documentTitle);
		getSupportActionBar().setIcon(R.drawable.head_back);

	}

	private OnMenuItemClickListener popupListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			Intent intent = new Intent(getApplicationContext(), CommentList.class);
			CommentList.identifiable = document;
			if(document.getComments()!=null)
				CommentList.comments =document.getComments();
			else {
				CommentList.comments = new ArrayList<Comment>();
			}
			DocumentDetail.this.startActivity(intent);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}

	private void initView() {
		documentId = getIntent().getIntExtra("documentId", 0);
		companyId = getIntent().getIntExtra("companyId", 0);
		projectId = getIntent().getIntExtra("projectId", 0);
		documentTitle = getIntent().getStringExtra("documentTitle");
		mScrollView = (ScrollView) findViewById(R.id.blog_detail_scrollview);

		mAuthor = (TextView) findViewById(R.id.blog_detail_author);
		mPubDate = (TextView) findViewById(R.id.blog_detail_date);
		mCommentCount = (TextView) findViewById(R.id.blog_detail_commentcount);

		// mDetail.setEnabled(false);

		mWebView = (WebView) findViewById(R.id.blog_detail_webview);
		mWebView.getSettings().setJavaScriptEnabled(false);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDefaultFontSize(15);
	}

	// 初始化控件数据
	private void initData() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					// headButtonSwitch(DATA_LOAD_COMPLETE);

					// int docType = discussion.getDocumentType();
					// if(docType == Blog.DOC_TYPE_ORIGINAL){
					// mDocTYpe.setImageResource(R.drawable.widget_original_icon);
					// }else if(docType == Blog.DOC_TYPE_REPASTE){
					// mDocTYpe.setImageResource(R.drawable.widget_repaste_icon);
					// }
					mAuthor.setText(document.getCreatorName());
					mPubDate.setText(new SimpleDateFormat("yyyy-MM-dd")
							.format(document.getCreated()));
					mCommentCount.setText((document.getComments()==null?0:document.getComments().size())+"");

					// //是否收藏
					// if(discussion.getFavorite() == 1)
					// mFavorite.setImageResource(R.drawable.widget_bar_favorite2);
					// else
					// mFavorite.setImageResource(R.drawable.widget_bar_favorite);

					// 显示评论数
					// if(discussion.getCommentCount() > 0){
					// bv_comment.setText(discussion.getCommentCount()+"");
					// bv_comment.show();
					// }else{
					// bv_comment.setText("");
					// bv_comment.hide();
					// }

					String body = UIHelper.WEB_STYLE + document.getContent()
							+ "<div style=\"margin-bottom: 80px\" />";
					// 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
					boolean isLoadImage;
					AppContext ac = (AppContext) getApplication();
					if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
						isLoadImage = true;
					} else {
						isLoadImage = ac.isLoadImage();
					}
					if (isLoadImage) {
						body = body.replaceAll(
								"(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
						body = body.replaceAll(
								"(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");
					} else {
						body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
					}

					mWebView.loadDataWithBaseURL(null, body, "text/html",
							"utf-8", null);
					mWebView.setWebViewClient(UIHelper.getWebViewClient());

					// //发送通知广播
					// if(msg.obj != null){
					// UIHelper.sendBroadCast(BlogDetail.this, (Notice)msg.obj);
					// }
				} else if (msg.what == 0) {
					// headButtonSwitch(DATA_LOAD_FAIL);
					//
					// UIHelper.ToastMessage(BlogDetail.this,
					// R.string.msg_load_is_null);
				} else if (msg.what == -1 && msg.obj != null) {
					// headButtonSwitch(DATA_LOAD_FAIL);
					//
					// ((AppException)msg.obj).makeToast(BlogDetail.this);
				}
			}
		};

		initData(documentId, false);
	}

	private void initData(final int blog_id, final boolean isRefresh) {
		// headButtonSwitch(DATA_LOAD_ING);

		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					document = ((AppContext) getApplication())
							.getDocumentById(companyId, projectId,
									documentId);
					msg.what = (document != null && document.getId() > 0) ? 1
							: 0;
					Log.i("document", document.getContent());
					// msg.obj = (discussion!=null) ? discussion.getNotice() :
					// null;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				mHandler.sendMessage(msg);
			}
		}.start();
	}
	
//	private void regOnDoubleEvent() {
//		gd = new GestureDetector(this,
//				new GestureDetector.SimpleOnGestureListener() {
//					@Override
//					public boolean onDoubleTap(MotionEvent e) {
//						isFullScreen = !isFullScreen;
//						if (!isFullScreen) {
//							WindowManager.LayoutParams params = getWindow()
//									.getAttributes();
//							params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
//							getWindow().setAttributes(params);
//							getWindow()
//									.clearFlags(
//											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//							mHeader.setVisibility(View.VISIBLE);
//							mFooter.setVisibility(View.VISIBLE);
//						} else {
//							WindowManager.LayoutParams params = getWindow()
//									.getAttributes();
//							params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//							getWindow().setAttributes(params);
//							getWindow()
//									.addFlags(
//											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//							mHeader.setVisibility(View.GONE);
//							mFooter.setVisibility(View.GONE);
//						}
//						return true;
//					}
//				});
//	}
//
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent event) {
//		gd.onTouchEvent(event);
//		return super.dispatchTouchEvent(event);
//	}
}
