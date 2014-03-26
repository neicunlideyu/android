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
import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Todo;

import java.text.SimpleDateFormat;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;

public class TodoDetail extends SherlockFragmentActivity {
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
    private Todo todo;

    private int todoId;
    private int companyId;
    private int projectId;
    private String todoTitle;

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
        getSupportActionBar().setTitle(todoTitle);
        getSupportActionBar().setIcon(R.drawable.head_back);

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
        todoId = getIntent().getIntExtra("todoId", 0);
        companyId = getIntent().getIntExtra("companyId", 0);
        projectId = getIntent().getIntExtra("projectId", 0);
        todoTitle = getIntent().getStringExtra("todoTitle");
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
        CommentList commentList = new CommentList(companyId,projectId,"todos",todoId);
        ft.replace(R.id.comment_list, commentList).commit();

    }

    // 初始化控件数据
    private void initData() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    mAuthor.setText(todo.getCreatorName());
                    mPubDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(todo.getCreated()));
                    mCommentCount.setText((todo.getComments() == null ? 0 : todo.getComments().size()) + "");

                    String body = UIHelper.WEB_STYLE + todo.getContent() + "<div style=\"margin-bottom: 80px\" />";
                    // 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
                    boolean isLoadImage;
                    AppContext ac = (AppContext) getApplication();
                    if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
                        isLoadImage = true;
                    } else {
                        isLoadImage = ac.isLoadImage();
                    }
                    if (isLoadImage) {
                        body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
                        body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");
                    } else {
                        body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
                    }

                    mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
                    mWebView.setWebViewClient(UIHelper.getWebViewClient());

                } else if (msg.what == 0) {
                } else if (msg.what == -1 && msg.obj != null) {
                }
            }
        };

        initData(todoId, false);
    }

    private void initData(final int blog_id, final boolean isRefresh) {
        // headButtonSwitch(DATA_LOAD_ING);

        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    todo = ((AppContext) getApplication()).getTodoById(companyId, projectId, todoId);
                    msg.what = (todo != null && todo.getId() > 0) ? 1 : 0;
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

    /**
     * 注册双击全屏事件
     */
    private void regOnDoubleEvent() {
        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                isFullScreen = !isFullScreen;
                if (!isFullScreen) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().setAttributes(params);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    mHeader.setVisibility(View.VISIBLE);
                    mFooter.setVisibility(View.VISIBLE);
                } else {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    getWindow().setAttributes(params);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
