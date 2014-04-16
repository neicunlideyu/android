package cn.onboard.android.app.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Attachment;
import com.onboard.api.dto.Upload;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.AttachmentIconType;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.ui.fragment.CommentListFragment;

public class UploadDetail extends SherlockFragmentActivity {
    private ListViewNewsAdapter attachmentAdapter;

    private TextView mAuthor;
    private TextView mPubDate;
    private TextView mCommentCount;
    private ListView attachmentListview;

    private Handler mHandler;
    private Upload upload;
    private List<Attachment> attachments;

    private int uploadId;
    private static int companyId;
    private static int projectId;

    private GestureDetector gd;
    private boolean isFullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_detail);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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

    private static class ListViewNewsAdapter extends BaseAdapter {
        private final List<Attachment> listItems;// 数据集合
        private final LayoutInflater listContainer;// 视图容器
        private final int itemViewResource;// 自定义项视图源
        private final BitmapManager bmpManager;

        static class ListItemView { // 自定义控件集合
            public ImageView attachmentImage;
            public Button btn_download;
        }

        public ListViewNewsAdapter(Context context, List<Attachment> attachments, int resource) {
            this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
            this.itemViewResource = resource;
            this.listItems = attachments;
            this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.widget_dface_loading));
        }

        public int getCount() {
            return listItems.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 自定义视图
            ListItemView listItemView = null;
            final Attachment attachment = listItems.get(position);

            if (convertView == null) {
                // 获取list_item布局文件的视图
                convertView = listContainer.inflate(this.itemViewResource, null);

                listItemView = new ListItemView();
                // 获取控件对象
                listItemView.attachmentImage = (ImageView) convertView.findViewById(R.id.attachment_type_icon);
                listItemView.btn_download = (Button) convertView.findViewById(R.id.upload_name_download);

                listItemView.btn_download.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        AppContext appContext = (AppContext) v.getContext();
                        appContext.downloadAttachmentByAttachmentId(attachment.getId(),
                                attachment.getName(), companyId, projectId);
                    }
                });

                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }
            if (attachment.getContentType().contains("image")) {
                String attachmentImageURL = URLs.ATTACHMENT_IMAGE_HTTP;
                attachmentImageURL = attachmentImageURL.replaceAll("companyId", companyId + "")
                        .replaceAll("projectId", projectId + "")
                        .replaceAll("attachmentId", attachment.getId() + "");
                bmpManager.loadBitmap(attachmentImageURL, listItemView.attachmentImage);
            } else {
                listItemView.attachmentImage.setImageDrawable(convertView.getResources()
                        .getDrawable(AttachmentIconType.getAttachmentTypeIconResourceId(attachment.getName(),
                                attachment.getContentType())));
            }
            listItemView.btn_download.setText(attachment.getName() + "(点击下载)");

            return convertView;
        }
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
        mAuthor = (TextView) findViewById(R.id.blog_detail_author);
        mPubDate = (TextView) findViewById(R.id.blog_detail_date);
        mCommentCount = (TextView) findViewById(R.id.blog_detail_commentcount);

        attachmentListview = (ListView) findViewById(R.id.upload_attachment_list);
        attachments = new ArrayList<Attachment>();
        attachmentAdapter = new ListViewNewsAdapter(getApplicationContext(), attachments,
                R.layout.upload_attachment_item);
        attachmentListview.setAdapter(attachmentAdapter);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CommentListFragment commentList = new CommentListFragment(companyId, projectId, "upload", uploadId);
        ft.replace(R.id.discussion_comments, commentList).commit();


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
                    attachments.addAll(upload.getAttachments());
                    attachmentAdapter.notifyDataSetChanged();

                    getSupportActionBar().setTitle("文件/" + upload.getContent());


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
                } else {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    getWindow().setAttributes(params);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
