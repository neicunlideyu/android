package cn.onboard.android.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
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

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Discussion;

import java.text.SimpleDateFormat;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;

public class DiscussionDetail extends SherlockFragmentActivity {
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
	private Discussion discussion;

	private int discussionId;
	private int companyId;
	private int projectId;
	private String discussionTitle;

	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	private final static int DATA_LOAD_FAIL = 0x003;


	private GestureDetector gd;
	private boolean isFullScreen;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_detail);

		this.initView();
		this.initData();

		// //加载评论视图&数据
		// this.initCommentView();
		// this.initCommentData();

		// 注册双击全屏事件
		this.regOnDoubleEvent();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setTitle(discussionTitle);
		getSupportActionBar().setIcon(R.drawable.head_back);

	}

//	private OnMenuItemClickListener popupListener = new OnMenuItemClickListener() {
//		@Override
//		public boolean onMenuItemClick(MenuItem item) {
//			Intent intent = new Intent(getApplicationContext(), CommentList.class);
//			CommentList.companyId = discussion.getCompanyId();
//            CommentList.projectId = discussion.getProjectId();
//            CommentList.attachId=discussion.getId();
//            CommentList.attachType = "discussion";
//
//            if(discussion.getComments()!=null)
//				CommentList.comments =discussion.getComments();
//			else {
//				CommentList.comments = new ArrayList<Comment>();
//			}
//			DiscussionDetail.this.startActivity(intent);
//			return true;
//		}
//	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

//		menu.add("评论").setOnMenuItemClickListener(popupListener)
//				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
		discussionId = getIntent().getIntExtra("discussionId", 0);
		companyId = getIntent().getIntExtra("companyId", 0);
		projectId = getIntent().getIntExtra("projectId", 0);
		discussionTitle = getIntent().getStringExtra("discussionTitle");
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

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CommentList commentList = new CommentList(companyId,projectId,"discussions",discussionId);
        ft.replace(R.id.comment_list, commentList).commit();

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
					mAuthor.setText(discussion.getCreatorName());
					mPubDate.setText(new SimpleDateFormat("yyyy-MM-dd")
							.format(discussion.getCreated()));
					mCommentCount.setText((discussion.getComments()==null?0:discussion.getComments().size())+"");

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

					String body = UIHelper.WEB_STYLE + discussion.getContent()
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

		initData(discussionId, false);
	}

	private void initData(final int blog_id, final boolean isRefresh) {
		// headButtonSwitch(DATA_LOAD_ING);

		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					discussion = ((AppContext) getApplication())
							.getDiscussionById(companyId, projectId,
									discussionId);
					msg.what = (discussion != null && discussion.getId() > 0) ? 1
							: 0;
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

	/**
	 * 底部栏切换
	 * 
	 * @param type
	 */
	// private void viewSwitch(int type) {
	// switch (type) {
	// case VIEWSWITCH_TYPE_DETAIL:
	// mDetail.setEnabled(false);
	// mCommentList.setEnabled(true);
	// mHeadTitle.setText(R.string.blog_detail_head_title);
	// mViewSwitcher.setDisplayedChild(0);
	// break;
	// case VIEWSWITCH_TYPE_COMMENTS:
	// mDetail.setEnabled(true);
	// mCommentList.setEnabled(false);
	// mHeadTitle.setText(R.string.comment_list_head_title);
	// mViewSwitcher.setDisplayedChild(1);
	// break;
	// }
	// }

	/**
	 * 头部按钮展示
	 * 
	 * @param type
	 */
	private void headButtonSwitch(int type) {
		switch (type) {
		case DATA_LOAD_ING:
			mScrollView.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.VISIBLE);
			mRefresh.setVisibility(View.GONE);
			break;
		case DATA_LOAD_COMPLETE:
			mScrollView.setVisibility(View.VISIBLE);
			mProgressbar.setVisibility(View.GONE);
			mRefresh.setVisibility(View.VISIBLE);
			break;
		case DATA_LOAD_FAIL:
			mScrollView.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.GONE);
			mRefresh.setVisibility(View.VISIBLE);
			break;
		}
	}

	// private View.OnClickListener refreshClickListener = new
	// View.OnClickListener() {
	// public void onClick(View v) {
	// initData(blogId, true);
	// loadLvCommentData(curId,0,mCommentHandler,UIHelper.LISTVIEW_ACTION_REFRESH);
	// }
	// };

	// private View.OnClickListener authorClickListener = new
	// View.OnClickListener() {
	// public void onClick(View v) {
	// UIHelper.showUserCenter(v.getContext(), discussion.getAuthorId(),
	// discussion.getAuthor());
	// }
	// };

	// private View.OnClickListener shareClickListener = new
	// View.OnClickListener() {
	// public void onClick(View v) {
	// if(discussion == null){
	// UIHelper.ToastMessage(v.getContext(), R.string.msg_read_detail_fail);
	// return;
	// }
	// //分享到
	// UIHelper.showShareDialog(BlogDetail.this, discussion.getTitle(),
	// discussion.getUrl());
	// }
	// };

	// private View.OnClickListener detailClickListener = new
	// View.OnClickListener() {
	// public void onClick(View v) {
	// if(blogId == 0){
	// return;
	// }
	// //切换到详情
	// viewSwitch(VIEWSWITCH_TYPE_DETAIL);
	// }
	// };

	// private View.OnClickListener commentlistClickListener = new
	// View.OnClickListener() {
	// public void onClick(View v) {
	// if(blogId == 0){
	// return;
	// }
	// //切换到评论
	// viewSwitch(VIEWSWITCH_TYPE_COMMENTS);
	// }
	// };

	// private View.OnClickListener favoriteClickListener = new
	// View.OnClickListener() {
	// public void onClick(View v) {
	// if(blogId == 0 || discussion == null){
	// return;
	// }
	//
	// final AppContext ac = (AppContext)getApplication();
	// if(!ac.isLogin()){
	// UIHelper.showLoginDialog(BlogDetail.this);
	// return;
	// }
	// final int uid = ac.getLoginUid();
	//
	// final Handler handler = new Handler(){
	// public void handleMessage(Message msg) {
	// if(msg.what == 1){
	// Result res = (Result)msg.obj;
	// if(res.OK()){
	// if(discussion.getFavorite() == 1){
	// discussion.setFavorite(0);
	// mFavorite.setImageResource(R.drawable.widget_bar_favorite);
	// }else{
	// discussion.setFavorite(1);
	// mFavorite.setImageResource(R.drawable.widget_bar_favorite2);
	// }
	// //重新保存缓存
	// ac.saveObject(discussion, discussion.getCacheKey());
	// }
	// UIHelper.ToastMessage(BlogDetail.this, res.getErrorMessage());
	// }else{
	// ((AppException)msg.obj).makeToast(BlogDetail.this);
	// }
	// }
	// };
	// new Thread(){
	// public void run() {
	// Message msg = new Message();
	// Result res = null;
	// try {
	// if(discussion.getFavorite() == 1){
	// res = ac.delFavorite(uid, blogId, FavoriteList.TYPE_BLOG);
	// }else{
	// res = ac.addFavorite(uid, blogId, FavoriteList.TYPE_BLOG);
	// }
	// msg.what = 1;
	// msg.obj = res;
	// } catch (AppException e) {
	// e.printStackTrace();
	// msg.what = -1;
	// msg.obj = e;
	// }
	// handler.sendMessage(msg);
	// }
	// }.start();
	// }
	// };

	// 初始化视图控件
	/*
	 * private void initCommentView() { lvComment_footer =
	 * getLayoutInflater().inflate(R.layout.listview_footer, null);
	 * lvComment_foot_more =
	 * (TextView)lvComment_footer.findViewById(R.id.listview_foot_more);
	 * lvComment_foot_progress =
	 * (ProgressBar)lvComment_footer.findViewById(R.id.listview_foot_progress);
	 * 
	 * lvCommentAdapter = new ListViewCommentAdapter(this, lvCommentData,
	 * R.layout.comment_listitem); mLvComment =
	 * (PullToRefreshListView)findViewById(R.id.comment_list_listview);
	 * 
	 * mLvComment.addFooterView(lvComment_footer);//添加底部视图 必须在setAdapter前
	 * mLvComment.setAdapter(lvCommentAdapter);
	 * mLvComment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	 * public void onItemClick(AdapterView<?> parent, View view, int position,
	 * long id) { //点击头部、底部栏无效 if(position == 0 || view == lvComment_footer)
	 * return;
	 * 
	 * Comment com = null; //判断是否是TextView if(view instanceof TextView){ com =
	 * (Comment)view.getTag(); }else{ ImageView img =
	 * (ImageView)view.findViewById(R.id.comment_listitem_userface); com =
	 * (Comment)img.getTag(); } if(com == null) return;
	 * 
	 * //跳转--回复评论界面 UIHelper.showCommentReply(BlogDetail.this,curId, curCatalog,
	 * com.getId(), com.getAuthorId(), com.getAuthor(), com.getContent()); } });
	 * mLvComment.setOnScrollListener(new AbsListView.OnScrollListener() {
	 * public void onScrollStateChanged(AbsListView view, int scrollState) {
	 * mLvComment.onScrollStateChanged(view, scrollState);
	 * 
	 * //数据为空--不用继续下面代码了 if(lvCommentData.size() == 0) return;
	 * 
	 * //判断是否滚动到底部 boolean scrollEnd = false; try {
	 * if(view.getPositionForView(lvComment_footer) ==
	 * view.getLastVisiblePosition()) scrollEnd = true; } catch (Exception e) {
	 * scrollEnd = false; }
	 * 
	 * if(scrollEnd && curLvDataState==UIHelper.LISTVIEW_DATA_MORE) {
	 * mLvComment.setTag(UIHelper.LISTVIEW_DATA_LOADING);
	 * lvComment_foot_more.setText(R.string.load_ing);
	 * lvComment_foot_progress.setVisibility(View.VISIBLE); //当前pageIndex int
	 * pageIndex = lvSumData/20; loadLvCommentData(curId, pageIndex,
	 * mCommentHandler, UIHelper.LISTVIEW_ACTION_SCROLL); } } public void
	 * onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int
	 * totalItemCount) { mLvComment.onScroll(view, firstVisibleItem,
	 * visibleItemCount, totalItemCount); } });
	 * mLvComment.setOnItemLongClickListener(new
	 * AdapterView.OnItemLongClickListener() { public boolean
	 * onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	 * { //点击头部、底部栏无效 if(position == 0 || view == lvComment_footer) return
	 * false;
	 * 
	 * Comment _com = null; //判断是否是TextView if(view instanceof TextView){ _com =
	 * (Comment)view.getTag(); }else{ ImageView img =
	 * (ImageView)view.findViewById(R.id.comment_listitem_userface); _com =
	 * (Comment)img.getTag(); } if(_com == null) return false;
	 * 
	 * final Comment com = _com;
	 * 
	 * curLvPosition = lvCommentData.indexOf(com);
	 * 
	 * final AppContext ac = (AppContext)getApplication(); //操作--回复 & 删除 final
	 * int uid = ac.getLoginUid(); //判断当前登录用户是否是博主 或者
	 * 该评论是否是当前登录用户发表的：true--有删除操作 false--没有删除操作 if(uid == com.getAuthorId() ||
	 * (blogDetail != null && uid == blogDetail.getAuthorId())) { final Handler
	 * handler = new Handler(){ public void handleMessage(Message msg) {
	 * if(msg.what == 1){ Result res = (Result)msg.obj; if(res.OK()){
	 * lvSumData--; bv_comment.setText(lvSumData+""); bv_comment.show();
	 * lvCommentData.remove(com); lvCommentAdapter.notifyDataSetChanged(); }
	 * UIHelper.ToastMessage(BlogDetail.this, res.getErrorMessage()); }else{
	 * ((AppException)msg.obj).makeToast(BlogDetail.this); } } }; final Thread
	 * thread = new Thread(){ public void run() { Message msg = new Message();
	 * try { Result res = ac.delBlogComment(uid, blogId, com.getId(),
	 * com.getAuthorId(), blogDetail.getAuthorId()); msg.what = 1; msg.obj =
	 * res; } catch (AppException e) { e.printStackTrace(); msg.what = -1;
	 * msg.obj = e; } handler.sendMessage(msg); } };
	 * UIHelper.showCommentOptionDialog(BlogDetail.this, curId, curCatalog, com,
	 * thread); } else { UIHelper.showCommentOptionDialog(BlogDetail.this,
	 * curId, curCatalog, com, null); } return true; } });
	 * mLvComment.setOnRefreshListener(new
	 * PullToRefreshListView.OnRefreshListener() { public void onRefresh() {
	 * loadLvCommentData(curId, 0, mCommentHandler,
	 * UIHelper.LISTVIEW_ACTION_REFRESH); } }); }
	 * 
	 * //初始化评论数据 private void initCommentData() { curId = blogId; curCatalog =
	 * CommentPub.CATALOG_BLOG;
	 * 
	 * mCommentHandler = new Handler() { public void handleMessage(Message msg)
	 * { if(msg.what >= 0){ BlogCommentList list = (BlogCommentList)msg.obj;
	 * Notice notice = list.getNotice(); //处理listview数据 switch (msg.arg1) { case
	 * UIHelper.LISTVIEW_ACTION_INIT: case UIHelper.LISTVIEW_ACTION_REFRESH:
	 * lvSumData = msg.what; lvCommentData.clear();//先清除原有数据
	 * lvCommentData.addAll(list.getCommentlist()); break; case
	 * UIHelper.LISTVIEW_ACTION_SCROLL: lvSumData += msg.what;
	 * if(lvCommentData.size() > 0){ for(Comment com1 : list.getCommentlist()){
	 * boolean b = false; for(Comment com2 : lvCommentData){ if(com1.getId() ==
	 * com2.getId() && com1.getAuthorId() == com2.getAuthorId()){ b = true;
	 * break; } } if(!b) lvCommentData.add(com1); } }else{
	 * lvCommentData.addAll(list.getCommentlist()); } break; }
	 * 
	 * //评论数更新 if(blogDetail != null && lvCommentData.size() >
	 * blogDetail.getCommentCount()){
	 * blogDetail.setCommentCount(lvCommentData.size());
	 * bv_comment.setText(lvCommentData.size()+""); bv_comment.show(); }
	 * 
	 * if(msg.what < 20){ curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
	 * lvCommentAdapter.notifyDataSetChanged();
	 * lvComment_foot_more.setText(R.string.load_full); }else if(msg.what ==
	 * 20){ curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
	 * lvCommentAdapter.notifyDataSetChanged();
	 * lvComment_foot_more.setText(R.string.load_more); } //发送通知广播 if(notice !=
	 * null){ UIHelper.sendBroadCast(BlogDetail.this, notice); } } else
	 * if(msg.what == -1){ //有异常--显示加载出错 & 弹出错误消息 curLvDataState =
	 * UIHelper.LISTVIEW_DATA_MORE;
	 * lvComment_foot_more.setText(R.string.load_error);
	 * ((AppException)msg.obj).makeToast(BlogDetail.this); }
	 * if(lvCommentData.size()==0){ curLvDataState =
	 * UIHelper.LISTVIEW_DATA_EMPTY;
	 * lvComment_foot_more.setText(R.string.load_empty); }
	 * lvComment_foot_progress.setVisibility(View.GONE); if(msg.arg1 ==
	 * UIHelper.LISTVIEW_ACTION_REFRESH){
	 * mLvComment.onRefreshComplete(getString(R.string.pull_to_refresh_update) +
	 * new Date().toLocaleString()); mLvComment.setSelection(0); } } };
	 * this.loadLvCommentData
	 * (curId,0,mCommentHandler,UIHelper.LISTVIEW_ACTION_INIT); }
	 */
	/**
	 * 线程加载评论数据
	 * 
	 * @param id
	 *            当前文章id
	 * @param pageIndex
	 *            当前页数
	 * @param handler
	 *            处理器
	 * @param action
	 *            动作标识
	 */
	/*
	 * private void loadLvCommentData(final int id,final int pageIndex,final
	 * Handler handler,final int action){ new Thread(){ public void run() {
	 * Message msg = new Message(); boolean isRefresh = false; if(action ==
	 * UIHelper.LISTVIEW_ACTION_REFRESH || action ==
	 * UIHelper.LISTVIEW_ACTION_SCROLL) isRefresh = true; try { BlogCommentList
	 * commentlist = ((AppContext)getApplication()).getBlogCommentList(id,
	 * pageIndex, isRefresh); msg.what = commentlist.getPageSize(); msg.obj =
	 * commentlist; } catch (AppException e) { e.printStackTrace(); msg.what =
	 * -1; msg.obj = e; } msg.arg1 = action;//告知handler当前action
	 * handler.sendMessage(msg); } }.start(); }
	 */
	/*
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { if (resultCode != RESULT_OK) return; if (data
	 * == null) return;
	 * 
	 * viewSwitch(VIEWSWITCH_TYPE_COMMENTS);//跳到评论列表
	 * 
	 * if (requestCode == UIHelper.REQUEST_CODE_FOR_RESULT) { Comment comm =
	 * (Comment)data.getSerializableExtra("COMMENT_SERIALIZABLE");
	 * lvCommentData.add(0,comm); lvCommentAdapter.notifyDataSetChanged();
	 * mLvComment.setSelection(0); //显示评论数 int count =
	 * blogDetail.getCommentCount() + 1; blogDetail.setCommentCount(count);
	 * bv_comment.setText(count+""); bv_comment.show(); } else if (requestCode
	 * == UIHelper.REQUEST_CODE_FOR_REPLY) { Comment comm =
	 * (Comment)data.getSerializableExtra("COMMENT_SERIALIZABLE");
	 * lvCommentData.set(curLvPosition, comm);
	 * lvCommentAdapter.notifyDataSetChanged(); } }
	 * 
	 * private View.OnClickListener commentpubClickListener = new
	 * View.OnClickListener() { public void onClick(View v) { _id = curId;
	 * 
	 * if(curId == 0){ return; }
	 * 
	 * _content = mFootEditer.getText().toString();
	 * if(StringUtils.isEmpty(_content)){ UIHelper.ToastMessage(v.getContext(),
	 * "请输入评论内容"); return; }
	 * 
	 * final AppContext ac = (AppContext)getApplication(); if(!ac.isLogin()){
	 * UIHelper.showLoginDialog(BlogDetail.this); return; }
	 * 
	 * _uid = ac.getLoginUid();
	 * 
	 * mProgress = ProgressDialog.show(v.getContext(), null,
	 * "发表中···",true,true);
	 * 
	 * final Handler handler = new Handler(){ public void handleMessage(Message
	 * msg) {
	 * 
	 * if(mProgress!=null)mProgress.dismiss();
	 * 
	 * if(msg.what == 1){ Result res = (Result)msg.obj;
	 * UIHelper.ToastMessage(BlogDetail.this, res.getErrorMessage());
	 * if(res.OK()){ //发送通知广播 if(res.getNotice() != null){
	 * UIHelper.sendBroadCast(BlogDetail.this, res.getNotice()); } //恢复初始底部栏
	 * mFootViewSwitcher.setDisplayedChild(0); mFootEditer.clearFocus();
	 * mFootEditer.setText(""); mFootEditer.setVisibility(View.GONE); //跳到评论列表
	 * viewSwitch(VIEWSWITCH_TYPE_COMMENTS); //更新评论列表
	 * lvCommentData.add(0,res.getComment());
	 * lvCommentAdapter.notifyDataSetChanged(); mLvComment.setSelection(0);
	 * //显示评论数 int count = blogDetail.getCommentCount() + 1;
	 * blogDetail.setCommentCount(count); bv_comment.setText(count+"");
	 * bv_comment.show(); //清除之前保存的编辑内容 ac.removeProperty(tempCommentKey); } }
	 * else { ((AppException)msg.obj).makeToast(BlogDetail.this); } } }; new
	 * Thread(){ public void run() { Message msg = new Message(); Result res =
	 * new Result(); try { //发表评论 res = ac.pubBlogComment(_id, _uid, _content);
	 * msg.what = 1; msg.obj = res; } catch (AppException e) {
	 * e.printStackTrace(); msg.what = -1; msg.obj = e; }
	 * handler.sendMessage(msg); } }.start(); } };
	 */
	/**
	 * 注册双击全屏事件
	 */
	private void regOnDoubleEvent() {
		gd = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						isFullScreen = !isFullScreen;
						if (!isFullScreen) {
							WindowManager.LayoutParams params = getWindow()
									.getAttributes();
							params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
							getWindow().setAttributes(params);
							getWindow()
									.clearFlags(
											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
							mHeader.setVisibility(View.VISIBLE);
							mFooter.setVisibility(View.VISIBLE);
						} else {
							WindowManager.LayoutParams params = getWindow()
									.getAttributes();
							params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
							getWindow().setAttributes(params);
							getWindow()
									.addFlags(
											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
							mHeader.setVisibility(View.GONE);
							mFooter.setVisibility(View.GONE);
						}
						return true;
					}
				});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		gd.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}
}
