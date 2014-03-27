package cn.onboard.android.app.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.onboard.api.dto.Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.StringUtils;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.DiscussionDetail;
import cn.onboard.android.app.ui.DocumentDetail;
import cn.onboard.android.app.ui.EditTodo;
import cn.onboard.android.app.ui.UploadDetail;
import cn.onboard.android.app.widget.pullrefresh.PullToRefreshListView;

public class ActivityFragment extends Fragment {
    private Integer companyId;

    private Integer userId;

    private PullToRefreshListView activiPullToRefreshListView;

    private ListViewNewsAdapter lvQuestionAdapter;

    private Handler handler;

    private View lvQuestion_footer;

    private TextView lvQuestion_foot_more;

    private ProgressBar lvQuestion_foot_progress;

    private List<Activity> activities = new ArrayList<Activity>();

    private List<Activity> returnedActivities;

    private LinearLayout lv;

    private int sum = 0;

    public ActivityFragment() {
        setRetainInstance(true);
    }

    public ActivityFragment(Integer companyId, Integer userId) {
        this();
        this.companyId = companyId;
        this.userId = userId;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        lv = (LinearLayout) inflater.inflate(R.layout.activities, null);
        initQuestionListView();
        loadLvQuestionData(0, handler, UIHelper.LISTVIEW_ACTION_REFRESH);
        return lv;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initQuestionListView() {
        lvQuestionAdapter = new ListViewNewsAdapter(getActivity(), activities,
                R.layout.question_listitem);
        lvQuestion_footer = getActivity().getLayoutInflater().inflate(
                R.layout.listview_footer, null);
        lvQuestion_foot_more = (TextView) lvQuestion_footer
                .findViewById(R.id.listview_foot_more);
        lvQuestion_foot_progress = (ProgressBar) lvQuestion_footer
                .findViewById(R.id.listview_foot_progress);
        activiPullToRefreshListView = (PullToRefreshListView) lv
                .findViewById(R.id.frame_listview_question);
        activiPullToRefreshListView.addFooterView(lvQuestion_footer);// 添加底部视图
        // 必须在setAdapter前
        activiPullToRefreshListView.setAdapter(lvQuestionAdapter);
        handler = this.getLvHandler(activiPullToRefreshListView,
                lvQuestionAdapter, lvQuestion_foot_more,
                lvQuestion_foot_progress, AppContext.PAGE_SIZE);
        activiPullToRefreshListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Activity activity = null;
                        // 判断是否是TextView
                        if (view instanceof TextView) {
                            activity = (Activity) view.getTag();
                        } else {
                            TextView tv = (TextView) view
                                    .findViewById(R.id.question_listitem_title);
                            activity = (Activity) tv.getTag();
                        }
                        Context context = view.getContext();
                        Intent intent = null;
                        if (activity.getAttachType().equals("discussion")) {
                            intent = new Intent(context,
                                    DiscussionDetail.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("discussionId", activity.getAttachId());
                            intent.putExtra("companyId", companyId);
                            intent.putExtra("projectId", activity.getProjectId());
                            context.startActivity(intent);
                        }
                        else if (activity.getAttachType().equals("document")){
                            intent = new Intent(context,
                                    DocumentDetail.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("documentId", activity.getAttachId());
                            intent.putExtra("companyId", companyId);
                            intent.putExtra("projectId", activity.getProjectId());
                            context.startActivity(intent);
                        } else if (activity.getAttachType().equals("todo")){
                            intent = new Intent(context,
                                    EditTodo.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("companyId", companyId);
                            intent.putExtra("projectId", activity.getProjectId());
                            intent.putExtra("todoId", activity.getAttachId());
                            intent.putExtra("editType", EditTodo.EditType.UPDATE.value());
                            context.startActivity(intent);
                        } else if (activity.getAttachType().equals("upload")){
                            intent = new Intent(context,
                                    UploadDetail.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("companyId", companyId);
                            intent.putExtra("projectId", activity.getProjectId());
                            intent.putExtra("uploadId", activity.getAttachId());
                            context.startActivity(intent);
                        }
                    }
                });
        activiPullToRefreshListView
                .setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScrollStateChanged(AbsListView view,
                                                     int scrollState) {
                        activiPullToRefreshListView.onScrollStateChanged(view,
                                scrollState);

                        // 数据为空--不用继续下面代码了
                        if (activities.isEmpty())
                            return;

                        // 判断是否滚动到底部
                        boolean scrollEnd = false;
                        try {
                            if (view.getPositionForView(lvQuestion_footer) == view
                                    .getLastVisiblePosition())
                                scrollEnd = true;
                        } catch (Exception e) {
                            scrollEnd = false;
                        }

                        int lvDataState = StringUtils
                                .toInt(activiPullToRefreshListView.getTag());
                        if (scrollEnd
                                && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                            activiPullToRefreshListView
                                    .setTag(UIHelper.LISTVIEW_DATA_LOADING);
                            lvQuestion_foot_more.setText(R.string.load_ing);
                            lvQuestion_foot_progress
                                    .setVisibility(View.VISIBLE);
                            // 当前pageIndex
                            int pageIndex = sum / AppContext.PAGE_SIZE;
                            loadLvQuestionData(pageIndex, handler,
                                    UIHelper.LISTVIEW_ACTION_SCROLL);
                        }
                    }

                    public void onScroll(AbsListView view,
                                         int firstVisibleItem, int visibleItemCount,
                                         int totalItemCount) {
                        activiPullToRefreshListView.onScroll(view,
                                firstVisibleItem, visibleItemCount,
                                totalItemCount);
                    }
                });
        activiPullToRefreshListView
                .setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                    public void onRefresh() {
                        loadLvQuestionData(0, handler,
                                UIHelper.LISTVIEW_ACTION_REFRESH);
                    }
                });
    }

    private void loadLvQuestionData(final int pageIndex, final Handler handler,
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
                    if (userId != null)
                        returnedActivities = ac.getActivitiesByCompanyIdByUserId(companyId, userId, pageIndex);
                    else
                        returnedActivities = ac.getActivitiesByCompanyId(companyId, pageIndex);
                    msg.what = returnedActivities.size();
                    msg.obj = returnedActivities;
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
                            break;
                        case UIHelper.LISTVIEW_ACTION_SCROLL:
                            sum += msg.what;
                    }
                    if (msg.what < pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
                        activities.addAll(returnedActivities);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_full);
                    } else if (msg.what == pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                        activities.addAll(returnedActivities);
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

    private static class ListViewNewsAdapter extends BaseAdapter {
        private Context context;// 运行上下文
        private List<Activity> listItems;// 数据集合
        private LayoutInflater listContainer;// 视图容器
        private int itemViewResource;// 自定义项视图源
        private BitmapManager bmpManager;

        /**
         * 实例化Adapter
         *
         * @param context
         * @param data
         * @param resource
         */
        public ListViewNewsAdapter(Context context, List<Activity> data,
                                   int resource) {
            this.context = context;
            this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
            this.itemViewResource = resource;
            this.listItems = data;
            this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.widget_dface_loading));
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
            // Log.d("method", "getView");

            // 自定义视图
            ListItemView listItemView = null;

            if (convertView == null) {
                // 获取list_item布局文件的视图
                convertView = listContainer
                        .inflate(this.itemViewResource, null);

                listItemView = new ListItemView();
                // 获取控件对象
                listItemView.face = (ImageView) convertView
                        .findViewById(R.id.question_listitem_userface);
                listItemView.title = (TextView) convertView
                        .findViewById(R.id.question_listitem_title);
                listItemView.author = (TextView) convertView
                        .findViewById(R.id.question_listitem_author);
                listItemView.count = (TextView) convertView
                        .findViewById(R.id.question_listitem_count);
                listItemView.date = (TextView) convertView
                        .findViewById(R.id.question_listitem_date);

                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            // 设置文字和图片
            Activity activity = listItems.get(position);
            String faceURL = "http://teamforge.b0.upaiyun.com/avatar/"
                    + activity.getCreatorId() + "/avatar.gif";
            bmpManager.loadBitmap(faceURL, listItemView.face);
            // }
            // listItemView.face.setOnClickListener(faceClickListener);
            listItemView.face.setTag(activity);

            listItemView.title.setText(activity.getSubject() + " " + activity.getTarget());
            listItemView.title.setTag(activity);// 设置隐藏参数(实体类)
            listItemView.author.setText(activity.getCreatorName());
            listItemView.date.setText(new SimpleDateFormat("yyyy-MM-dd")
                    .format(activity.getCreated()));
            listItemView.count.setVisibility(0);

            return convertView;
        }

        static class ListItemView { // 自定义控件集合
            public ImageView face;
            public TextView title;
            public TextView author;
            public TextView date;
            public TextView count;
        }
    }


}
