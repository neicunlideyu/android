package cn.onboard.android.app.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Discussion;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Locale;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.fragment.CommentListFragment;

public class DiscussionDetail extends BaseActivity {


    private TextView mAuthor;
    private TextView mPubDate;
    private TextView mCommentCount;

    private WebView mWebView;
    private Handler mHandler;
    private Discussion discussion;

    private int discussionId;
    private int companyId;
    private int projectId;


    private GestureDetector gd;
    private boolean isFullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussion_detail);

        this.initView();
        new GetDiscussionTask().execute();
        // 注册双击全屏事件
        this.regOnDoubleEvent();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        mAuthor = (TextView) findViewById(R.id.discussion_creator);
        mPubDate = (TextView) findViewById(R.id.discussion_created_date);
        mCommentCount = (TextView) findViewById(R.id.blog_detail_commentcount);

        // mDetail.setEnabled(false);

        mWebView = (WebView) findViewById(R.id.discussion_content_webview);
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDefaultFontSize(15);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CommentListFragment commentList = new CommentListFragment(companyId, projectId, "discussion", discussionId);
        ft.replace(R.id.discussion_comments, commentList).commit();

    }

    private class GetDiscussionTask extends AsyncTask<Void, Void, Discussion> {

        @Override
        protected Discussion doInBackground(Void... params) {
            try {
                discussion = ((AppContext) getApplication())
                        .getDiscussionById(companyId, projectId,
                                discussionId);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return discussion;
        }

        @Override
        protected void onPostExecute(Discussion discussion) {
            findViewById(R.id.loading_progress_bar).setVisibility(View.GONE);
            if (discussion != null) {
                mAuthor.setText(discussion.getCreatorName());
                mPubDate.setText(new PrettyTime(new Locale("zh")).format(discussion.getCreated()));
                getSupportActionBar().setTitle("讨论/" + discussion.getSubject());
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
                mWebView.setWebViewClient(new WebViewClient());
            } else {
                UIHelper.ToastMessage(DiscussionDetail.this, getString(R.string.get_discussion_fail));
            }
        }
    }

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
                        } else {
                            WindowManager.LayoutParams params = getWindow()
                                    .getAttributes();
                            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            getWindow().setAttributes(params);
                            getWindow()
                                    .addFlags(
                                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
