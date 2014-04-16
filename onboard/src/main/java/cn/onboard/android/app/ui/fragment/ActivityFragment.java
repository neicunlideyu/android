package cn.onboard.android.app.ui.fragment;

import android.content.Context;
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

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.StringUtils;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.widget.pullrefresh.PullToRefreshListView;

public class ActivityFragment extends Fragment {
    private Integer companyId;

    private Integer userId;

    private PullToRefreshListView activiPullToRefreshListView;

    private ListViewNewsAdapter listViewNewsAdapter;

    private Handler handler;

    private View listviewFooter;

    private TextView listview_footer_more;

    private ProgressBar listview_footer_progress;

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
        initActivityListView();
        loadActivityData(0, handler, UIHelper.LISTVIEW_ACTION_REFRESH);
        return lv;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initActivityListView() {
        listViewNewsAdapter = new ListViewNewsAdapter(getActivity(), activities,
                R.layout.activity_listitem);
        listviewFooter = getActivity().getLayoutInflater().inflate(
                R.layout.listview_footer, null);
        listview_footer_more = (TextView) listviewFooter
                .findViewById(R.id.listview_foot_more);
        listview_footer_progress = (ProgressBar) listviewFooter
                .findViewById(R.id.listview_foot_progress);
        activiPullToRefreshListView = (PullToRefreshListView) lv
                .findViewById(R.id.activity_listview);
        activiPullToRefreshListView.addFooterView(listviewFooter);// 添加底部视图
        // 必须在setAdapter前
        activiPullToRefreshListView.setAdapter(listViewNewsAdapter);
        handler = this.getLvHandler(activiPullToRefreshListView,
                listViewNewsAdapter, listview_footer_more,
                listview_footer_progress, AppContext.PAGE_SIZE);
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
                                    .findViewById(R.id.activity_listitem_title);
                            activity = (Activity) tv.getTag();
                        }
                        Context context = view.getContext();
                        UIHelper.pageLink(context, activity.getAttachType(), activity.getAttachId(), activity.getCompanyId(), activity.getProjectId());
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
                            if (view.getPositionForView(listviewFooter) == view
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
                            listview_footer_more.setText(R.string.load_ing);
                            listview_footer_progress
                                    .setVisibility(View.VISIBLE);
                            // 当前pageIndex
                            int pageIndex = sum / AppContext.PAGE_SIZE;
                            loadActivityData(pageIndex, handler,
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
                        loadActivityData(0, handler,
                                UIHelper.LISTVIEW_ACTION_REFRESH);
                    }
                });
    }

    private void loadActivityData(final int pageIndex, final Handler handler,
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
                            activities.clear();
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
                        .findViewById(R.id.activity_listitem_userface);
                listItemView.title = (TextView) convertView
                        .findViewById(R.id.activity_listitem_title);
                listItemView.author = (TextView) convertView
                        .findViewById(R.id.activity_listitem_author);
                listItemView.date = (TextView) convertView
                        .findViewById(R.id.activity_listitem_date);

                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            // 设置文字和图片
            Activity activity = listItems.get(position);
            String faceURL = URLs.USER_FACE_HTTP + activity.getCreator().getAvatar();
            bmpManager.loadBitmap(faceURL, listItemView.face);
            // }
            // listItemView.face.setOnClickListener(faceClickListener);
            listItemView.face.setTag(activity);

            listItemView.title.setText("在项目" + activity.getProjectName() + activity.getSubject() + " " + activity.getTarget());
            listItemView.title.setTag(activity);// 设置隐藏参数(实体类)
            listItemView.author.setText(activity.getCreatorName());
            listItemView.date.setText(new PrettyTime(new Locale("zh")).format(activity.getCreated()));

            return convertView;
        }

        static class ListItemView { // 自定义控件集合
            public ImageView face;
            public TextView title;
            public TextView author;
            public TextView date;
        }
    }


}
