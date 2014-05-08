package cn.onboard.android.app.ui.fragment;

import android.app.Activity;
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

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.onboard.api.dto.Topic;

import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.TopicListViewAdapter;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.core.company.CompanyService;
import cn.onboard.android.app.core.discussion.DiscussionService;
import cn.onboard.android.app.ui.NewDiscussion;
import cn.onboard.android.app.widget.pullrefresh.PullToRefreshListView;

public class TopicFragment extends Fragment implements OnMenuItemClickListener {
    private DiscussionService discussionService;

    private int companyId;

    private int projectId;

    private final List<Topic> topicList = new ArrayList<Topic>();

    private List<Topic> returnedTopics;

    private TopicFragment() {
        setRetainInstance(true);
    }

    private TopicListViewAdapter lvca;

    private PullToRefreshListView topicsPullToRefreshListView;

    private View listview_footer;

    private TextView listview_foot_more;

    private ProgressBar listview_foot_progress;

    private LinearLayout lv;

    private int sum = 0;

    private Handler handler;

    public TopicFragment(int companyId, int projectId) {
        this();
        this.companyId = companyId;
        this.projectId = projectId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        lv = (LinearLayout) inflater.inflate(
                R.layout.topics, null);
        initService();
        initTopicView();
        loadTopicsData(0, handler, UIHelper.LISTVIEW_ACTION_REFRESH);
        return lv;
    }

    private void initService() {
        discussionService = new DiscussionService((AppContext)getActivity().getApplicationContext());
    }


    void initTopicView() {
        lvca = new TopicListViewAdapter(
                getActivity().getApplicationContext(), topicList,
                R.layout.topic_listitem);


        listview_footer = getActivity().getLayoutInflater().inflate(
                R.layout.listview_footer, null);
        listview_foot_more = (TextView) listview_footer
                .findViewById(R.id.listview_foot_more);
        listview_foot_progress = (ProgressBar) listview_footer
                .findViewById(R.id.listview_foot_progress);
        topicsPullToRefreshListView = (PullToRefreshListView) lv
                .findViewById(R.id.topic_list);
        topicsPullToRefreshListView.addFooterView(listview_footer);// 添加底部视图
        // 必须在setAdapter前
        topicsPullToRefreshListView.setAdapter(lvca);
        handler = this.getLvHandler(topicsPullToRefreshListView,
                lvca, listview_foot_more,
                listview_foot_progress, AppContext.PAGE_SIZE);

        topicsPullToRefreshListView
                .setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScrollStateChanged(AbsListView view,
                                                     int scrollState) {
                        topicsPullToRefreshListView.onScrollStateChanged(view,
                                scrollState);

                        // 数据为空--不用继续下面代码了
                        if (topicList.isEmpty())
                            return;

                        // 判断是否滚动到底部
                        boolean scrollEnd = false;
                        try {
                            if (view.getPositionForView(listview_footer) == view
                                    .getLastVisiblePosition())
                                scrollEnd = true;
                        } catch (Exception e) {
                            scrollEnd = false;
                        }

                        int lvDataState = Integer.parseInt(topicsPullToRefreshListView.getTag().toString());
                        if (scrollEnd
                                && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                            topicsPullToRefreshListView
                                    .setTag(UIHelper.LISTVIEW_DATA_LOADING);
                            listview_foot_more.setText(R.string.load_ing);
                            listview_foot_progress
                                    .setVisibility(View.VISIBLE);
                            // 当前pageIndex
                            int pageIndex = sum / AppContext.PAGE_SIZE;
                            loadTopicsData(pageIndex, handler,
                                    UIHelper.LISTVIEW_ACTION_SCROLL);
                        }
                    }

                    public void onScroll(AbsListView view,
                                         int firstVisibleItem, int visibleItemCount,
                                         int totalItemCount) {
                        topicsPullToRefreshListView.onScroll(view,
                                firstVisibleItem, visibleItemCount,
                                totalItemCount);
                    }
                });
        topicsPullToRefreshListView
                .setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                    public void onRefresh() {
                        loadTopicsData(0, handler,
                                UIHelper.LISTVIEW_ACTION_REFRESH);
                    }
                });
        topicsPullToRefreshListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Topic topic = null;
                        // 判断是否是TextView
                        if (view instanceof TextView) {
                            topic = (Topic) view.getTag();
                        } else {
                            TextView tv = (TextView) view
                                    .findViewById(R.id.topic_listitem_title);
                            topic = (Topic) tv.getTag();
                        }
                        if (topic == null)
                            return;
                        Context context = view.getContext();
                        Intent intent = UIHelper.pageLink(context, topic.getRefType(), topic.getRefId(), companyId, topic.getProjectId());
                        context.startActivity(intent);
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
                            topicList.clear();
                            break;
                        case UIHelper.LISTVIEW_ACTION_SCROLL:
                            sum += msg.what;
                    }
                    if (msg.what < pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
                        topicList.addAll(returnedTopics);
                        lvca.notifyDataSetChanged();
                        more.setText(R.string.load_full);
                    } else if (msg.what == pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                        topicList.addAll(returnedTopics);
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

    private void loadTopicsData(final int pageIndex, final Handler handler,
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
                    returnedTopics = discussionService
                            .getTopicsByProjectId(companyId, projectId, pageIndex);
                    msg.what = returnedTopics.size();
                    msg.obj = returnedTopics;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent = new Intent(getActivity().getApplicationContext(), NewDiscussion.class);
        intent.putExtra("companyId", companyId);
        intent.putExtra("projectId", projectId);
        startActivityForResult(intent, 0);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        topicsPullToRefreshListView.clickRefresh();
    }

}
