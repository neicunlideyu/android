
package cn.onboard.android.app.ui.fragment;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Attachment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.AttachmentType;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.StringUtils;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.DiscussionDetail;
import cn.onboard.android.app.ui.DocumentDetail;
import cn.onboard.android.app.ui.EditTodo;
import cn.onboard.android.app.ui.UploadDetail;
import cn.onboard.android.app.widget.pullrefresh.PullToRefreshListView;


public class AttachmentFragment extends Fragment implements MenuItem.OnMenuItemClickListener {
    private static Integer companyId;

    private static Integer projectId;

    private PullToRefreshListView attachmentPullToRefreshListView;

    private ListViewNewsAdapter attachmentAdapter;

    private View attachment_list_footer;

    private Handler handler;

    private TextView attachment_list_foot_more;

    private ProgressBar attachment_foot_progress;

    private List<Attachment> attachments;

    private List<Attachment> returnedAttachments;

    private static Integer userId;

    private int sum = 0;

    private static String cookie;

    private final String DISCUSSION_TYPE_NAME = "discussion";
    private final String UPLOAD_TYPE_NAME = "upload";
    private final String TODO_TYPE_NAME = "todo";

    public AttachmentFragment() {
        setRetainInstance(true);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    public AttachmentFragment(Integer companyId, Integer projectId, Integer userId) {
        this();
        this.companyId = companyId;
        this.projectId = projectId;
        this.userId = userId;
    }

    private static class ListViewNewsAdapter extends BaseAdapter {
        private Context context;// 运行上下文
        private List<Attachment> listItems;// 数据集合
        private LayoutInflater listContainer;// 视图容器
        private int itemViewResource;// 自定义项视图源
        private BitmapManager bmpManager;

        static class ListItemView { // 自定义控件集合
            public ImageView face;
            public TextView title;
            public TextView author;
            public TextView date;
            public Button btn_download;
        }

        /**
         * 实例化Adapter
         *
         * @param context
         * @param attachments
         * @param resource
         */
        public ListViewNewsAdapter(Context context, List<Attachment> attachments, int resource) {
            this.context = context;
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

        /**
         * ListView Item设置
         */
        public View getView(int position, View convertView, ViewGroup parent) {

            // 自定义视图
            ListItemView listItemView = null;
            final Attachment attachment = listItems.get(position);

            if (convertView == null) {
                // 获取list_item布局文件的视图
                convertView = listContainer.inflate(this.itemViewResource, null);

                listItemView = new ListItemView();
                // 获取控件对象
                listItemView.face = (ImageView) convertView.findViewById(R.id.question_listitem_userface);
                listItemView.title = (TextView) convertView.findViewById(R.id.question_listitem_title);
                listItemView.author = (TextView) convertView.findViewById(R.id.question_listitem_author);
                listItemView.date = (TextView) convertView.findViewById(R.id.question_listitem_date);
                listItemView.btn_download = (Button) convertView.findViewById(R.id.button_download);

                listItemView.btn_download.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        AppContext appContext = (AppContext)v.getContext();
                        appContext.downloadAttachmentByAttachmentId(attachment.getId(),
                                attachment.getName(), companyId, projectId);
                    }
                });

                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            // 设置文字和图片
            if (attachment.getContentType().contains("image")) {
                String attachmentImageURL = URLs.ATTACHMENT_IMAGE_HTTP;
                attachmentImageURL = attachmentImageURL.replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "").replaceAll("attachmentId", attachment.getId() + "");
                bmpManager.loadBitmap(attachmentImageURL, listItemView.face);
            }
            else {
                listItemView.face.setImageDrawable(convertView.getResources()
                        .getDrawable(AttachmentType.getAttachmentTypeIconResourceId(attachment.getName(),
                                attachment.getContentType())));
            }

            // }
            // listItemView.face.setOnClickListener(faceClickListener);
            listItemView.face.setTag(attachment);

            listItemView.title.setText(attachment.getName());
            listItemView.title.setTag(attachment);// 设置隐藏参数(实体类)
            listItemView.author.setText(attachment.getCreatorName());
            listItemView.date.setText(new SimpleDateFormat("yyyy-MM-dd").format(attachment.getCreated()));

            return convertView;
        }
    }

    private void initView(LinearLayout lv) {
        attachments = new ArrayList<Attachment>();
        attachmentAdapter = new ListViewNewsAdapter(getActivity().getApplicationContext(), attachments,
                R.layout.attachment_listitem);
        attachmentPullToRefreshListView = (PullToRefreshListView)  lv
                .findViewById(R.id.upload_list_view);

        attachment_list_footer = getActivity().getLayoutInflater().inflate(
                R.layout.listview_footer, null);
        attachment_list_foot_more = (TextView) attachment_list_footer
                .findViewById(R.id.listview_foot_more);
        attachment_foot_progress = (ProgressBar) attachment_list_footer
                .findViewById(R.id.listview_foot_progress);
        attachmentPullToRefreshListView.addFooterView(attachment_list_footer);
        attachmentPullToRefreshListView.setAdapter(attachmentAdapter);

        handler = this.getLvHandler(attachmentPullToRefreshListView,
                attachmentAdapter, attachment_list_foot_more,
                attachment_foot_progress, AppContext.PAGE_SIZE);

        attachmentPullToRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view,
                                             int scrollState) {
                attachmentPullToRefreshListView.onScrollStateChanged(view,
                        scrollState);

                // 数据为空--不用继续下面代码了
                if (attachments.isEmpty())
                    return;

                // 判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(attachment_list_footer) == view
                            .getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                int lvDataState = StringUtils
                        .toInt(attachmentPullToRefreshListView.getTag());
                if (scrollEnd
                        && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    attachmentPullToRefreshListView
                            .setTag(UIHelper.LISTVIEW_DATA_LOADING);
                    attachment_list_foot_more.setText(R.string.load_ing);
                    attachment_foot_progress
                            .setVisibility(View.VISIBLE);
                    // 当前pageIndex
                    int pageIndex = sum / AppContext.PAGE_SIZE;
                    initGetUploadsByProject(pageIndex, handler,
                            UIHelper.LISTVIEW_ACTION_SCROLL);
                }
            }
            public void onScroll(AbsListView view,
                                 int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                attachmentPullToRefreshListView.onScroll(view,
                        firstVisibleItem, visibleItemCount,
                        totalItemCount);
            }
        });
        attachmentPullToRefreshListView
                .setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                    public void onRefresh() {
                            initGetUploadsByProject(0, handler,
                                    UIHelper.LISTVIEW_ACTION_REFRESH);

                    }
                });
        attachmentPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Attachment attachment = null;
                // 判断是否是TextView
                if (view instanceof TextView) {
                    attachment = (Attachment) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.question_listitem_title);
                    attachment = (Attachment) tv.getTag();
                }
                if (attachment == null)
                    return;
                Context context = view.getContext();
                Intent intent = null;
                if (attachment.getTargetType().equals("discussion")) {
                    intent = new Intent(context,
                            DiscussionDetail.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("discussionId", attachment.getAttachId());
                    intent.putExtra("companyId", companyId);
                    intent.putExtra("projectId", attachment.getProjectId());
                    context.startActivity(intent);
                }
                else if (attachment.getTargetType().equals("document")){
                    intent = new Intent(context,
                            DocumentDetail.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("documentId", attachment.getAttachId());
                    intent.putExtra("companyId", companyId);
                    intent.putExtra("projectId", attachment.getProjectId());
                    context.startActivity(intent);
                } else if (attachment.getTargetType().equals("todo")){
                    intent = new Intent(context,
                            EditTodo.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("companyId", companyId);
                    intent.putExtra("projectId", attachment.getProjectId());
                    intent.putExtra("todoId", attachment.getAttachId());
                    intent.putExtra("editType", EditTodo.EditType.UPDATE.value());
                    context.startActivity(intent);
                }else if (attachment.getTargetType().equals("upload")){
                    intent = new Intent(context,
                            UploadDetail.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("companyId", companyId);
                    intent.putExtra("projectId", attachment.getProjectId());
                    intent.putExtra("uploadId", attachment.getAttachId());
                    context.startActivity(intent);
                }
            }
        });
    }

    private Handler getLvHandler(final PullToRefreshListView lv,
                                 final BaseAdapter adapter, final TextView more,
                                 final ProgressBar progress, final int pageSize) {
        return new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    // listview数据处理
                    switch (msg.arg1) {
                        case UIHelper.LISTVIEW_ACTION_REFRESH:
                            sum = msg.what;
                            attachments.clear();
                            break;
                        case UIHelper.LISTVIEW_ACTION_SCROLL:
                            sum += msg.what;
                    }
                    if (msg.what < pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
                        attachments.addAll(returnedAttachments);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_full);
                    } else if (msg.what == pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                        attachments.addAll(returnedAttachments);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_more);

                    }
                } else if (msg.what == -1) {
                    // 有异常--显示加载出错 & 弹出错误消息
                    lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                    more.setText(R.string.load_error);
                }
                if (adapter.getCount() == 0) {
                    lv.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
                    more.setText(R.string.load_empty);
                }
                progress.setVisibility(ProgressBar.GONE);
                if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    lv.onRefreshComplete();
                    lv.setSelection(0);

                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout lv = (LinearLayout) inflater.inflate(R.layout.attachment_list, null);
        initView(lv);
        initGetUploadsByProject(0, handler, UIHelper.LISTVIEW_ACTION_REFRESH);
        return lv;
    }

    private void initGetUploadsByProject(final int pageIndex, final Handler handler,
                                         final int action) {
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH
                        || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    AppContext ac = (AppContext) getActivity().getApplication();
                    List<Attachment> attachments =new ArrayList<Attachment>();
                    if(projectId != null)
                        returnedAttachments= ac.getAttachmentsByProjectId(companyId, projectId, pageIndex);
                    else if(userId!=null)
                        returnedAttachments = ac.getAttachmentsByCompanyIdByUserId(companyId, userId, pageIndex);
                    msg.what = returnedAttachments.size();
                    msg.obj = returnedAttachments;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                handler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}

