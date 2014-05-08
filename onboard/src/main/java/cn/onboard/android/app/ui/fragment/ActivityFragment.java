package cn.onboard.android.app.ui.fragment;

import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.onboard.api.dto.Activity;

import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.ActivityListViewAdapter;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.core.activity.ActivityService;
import cn.onboard.android.app.widget.pullrefresh.PullToRefreshListView;

public class ActivityFragment extends Fragment {
    private Integer companyId;

    private Integer userId;

    private ActivityService activityService;

    private PullToRefreshListView activiPullToRefreshListView;

    private Handler handler;

    private View listviewFooter;

    private TextView listview_footer_more;

    private ProgressBar listview_footer_progress;

    private final List<Activity> activities = new ArrayList<Activity>();

    private List<Activity> returnedActivities;

    private LinearLayout lv;

    private int sum = 0;

    private ActivityFragment() {
        setRetainInstance(true);
    }

    public ActivityFragment(Integer companyId, Integer userId) {
        this.companyId = companyId;
        this.userId = userId;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        lv = (LinearLayout) inflater.inflate(R.layout.activities, null);
        initService();
        initActivityListView();
        loadActivityData(0, handler, UIHelper.LISTVIEW_ACTION_REFRESH);
        return lv;
    }

    private void initService() {
        activityService = new ActivityService((AppContext) getActivity().getApplication());
    }

    private void initActivityListView() {
        ActivityListViewAdapter listViewNewsAdapter = new ActivityListViewAdapter(getActivity(), activities,
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
                        Intent intent = UIHelper.pageLink(context, activity.getAttachType(), activity.getAttachId(), activity.getCompanyId(), activity.getProjectId());
                        context.startActivity(intent);
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

                        int lvDataState = Integer.parseInt(activiPullToRefreshListView.getTag().toString());
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
                        returnedActivities = activityService.getActivitiesByCompanyIdByUserId(companyId, userId, pageIndex);
                    else
                        returnedActivities = activityService.getActivitiesByCompany(companyId, pageIndex);
                    msg.what = returnedActivities.size();
                    msg.obj = returnedActivities;
                } catch (RestClientException e) {
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


}
