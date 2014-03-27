package cn.onboard.android.app.ui;

import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Upload;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.AttachmentType;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.fragment.CommentListFragment;

public class UploadDetail extends SherlockFragmentActivity {
    private BitmapManager bmpManager;
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
    private Upload upload;
    private Button mUploadNameDownload;
    private ImageView mAttachmentTypeIcon;

    private int uploadId;
    private int companyId;
    private int projectId;
    private int attachmentId;
    private String attachmentName;
    private String attachmentType;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;

    private GestureDetector gd;
    private boolean isFullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_detail);
        Log.d("initData", "started");
        this.initView();
        Log.d("initView", "finished");
        this.initData();
        Log.d("initData", "finished");
        // //加载评论视图&数据
        // this.initCommentView();
        // this.initCommentData();

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
        uploadId = getIntent().getIntExtra("uploadId", 0);
        companyId = getIntent().getIntExtra("companyId", 0);
        projectId = getIntent().getIntExtra("projectId", 0);
        attachmentId = getIntent().getIntExtra("attachmentId", 0);
        attachmentName = getIntent().getStringExtra("attachmentName");
        attachmentType = getIntent().getStringExtra("attachmentType");

        mScrollView = (ScrollView) findViewById(R.id.blog_detail_scrollview);
        bmpManager = new BitmapManager(BitmapFactory.decodeResource(getResources(),
                R.drawable.widget_dface_loading));

        mAuthor = (TextView) findViewById(R.id.blog_detail_author);
        mPubDate = (TextView) findViewById(R.id.blog_detail_date);
        mCommentCount = (TextView) findViewById(R.id.blog_detail_commentcount);
        mUploadNameDownload = (Button) findViewById(R.id.upload_name_download);
        mAttachmentTypeIcon = (ImageView) findViewById(R.id.attachment_type_icon);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CommentListFragment commentList = new CommentListFragment(companyId,projectId,"upload",uploadId);
        ft.replace(R.id.discussion_comments, commentList).commit();

        mUploadNameDownload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AppContext ac = (AppContext) getApplication();
                ac.downloadAttachmentByAttachmentId(attachmentId, companyId,
                        projectId);
            }
        });

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
                    mAuthor.setText(upload.getCreatorName());
                    mPubDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(upload.getCreated()));
                    mCommentCount.setText((upload.getComments() == null ? 0 : upload.getComments().size()) + "");
                    getSupportActionBar().setTitle("文件/" + upload.getContent());
                    if (attachmentType.contains("image")) {
                        String attachmentImageURL = URLs.ATTACHMENT_IMAGE_HTTP;
                        attachmentImageURL = attachmentImageURL.replaceAll("companyId", companyId + "")
                                .replaceAll("projectId", projectId + "")
                                .replaceAll("attachmentId", attachmentId + "");
                        bmpManager.loadBitmap(attachmentImageURL, mAttachmentTypeIcon);
                    }
                    else {
                        mAttachmentTypeIcon.setImageDrawable(getResources()
                                .getDrawable(AttachmentType.getAttachmentTypeIconResourceId(attachmentName,
                                        attachmentType)));
                    }
                    mUploadNameDownload.setText(attachmentName + "(点击下载)");

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

        initData(uploadId, false);
    }

    private void initData(final int blog_id, final boolean isRefresh) {
        // headButtonSwitch(DATA_LOAD_ING);

        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    upload = ((AppContext) getApplication()).getUploadById(companyId, projectId, uploadId);
                    msg.what = (upload != null && upload.getId() > 0) ? 1 : 0;
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
