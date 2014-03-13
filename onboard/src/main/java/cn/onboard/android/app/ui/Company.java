package cn.onboard.android.app.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.GridViewProjectAdapter;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.widget.calendar.CalendarController;
import cn.onboard.android.app.widget.calendar.CalendarController.EventHandler;
import cn.onboard.android.app.widget.calendar.CalendarController.EventInfo;
import cn.onboard.android.app.widget.calendar.CalendarController.EventType;
import cn.onboard.android.app.widget.calendar.MonthByWeekFragment;
import cn.onboard.android.app.widget.scroll.ScrollLayout;
import com.onboard.domain.model.Project;

import java.util.List;

public class Company extends BaseActivity implements EventHandler {
    private int companyId;

    private ScrollLayout mScrollLayout;
    private RadioButton[] mButtons;
    private String[] mHeadTitles;
    private int mViewCount;
    private int mCurSel;


    private TextView mHeadTitle;

    private GridView projectGridView;


    private CalendarController mController;
    Fragment monthFrag;
    Fragment dayFrag;
    private EventInfo event;
    private boolean dayView;
    private long eventID;
    boolean eventView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company);
        companyId = getIntent().getIntExtra("companyId", 0);
        mHeadTitle = (TextView) findViewById(R.id.company_head_title);
        initPageScroll();
        initProjectFrameView();
        initCalendarFrameView();
    }


    private void initProjectFrameView() {
        projectGridView = (GridView) findViewById(R.id.project_grid_list);
        getProjectList(this);
    }

    private void initCalendarFrameView() {
        mController = CalendarController.getInstance(this);
        //setContentView(R.layout.cal_layout);
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        monthFrag = new MonthByWeekFragment(System.currentTimeMillis(), false);
        ft.replace(R.id.cal_frame, monthFrag).commit();
        mController.registerEventHandler(R.id.cal_frame, (EventHandler) monthFrag);

        mController.registerFirstEventHandler(0, this);

    }

    @SuppressLint("HandlerLeak")
    private void getProjectList(final Context context) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what >= 1) {
                    @SuppressWarnings("unchecked")
                    List<Project> projectList = (List<Project>) msg.obj;
                    GridViewProjectAdapter gridViewProjectAdapter = new GridViewProjectAdapter(
                            context, projectList, R.layout.project_item);
                    projectGridView.setAdapter(gridViewProjectAdapter);
                    //projectGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
                    projectGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent,
                                                View view, int position, long id) {
                            Project project = null;
                            // 判断是否是TextView
                            if (view instanceof TextView) {
                                project = (Project) view.getTag();
                            } else {
                                TextView tv = (TextView) view
                                        .findViewById(R.id.project_name);
                                project = (Project) tv.getTag();
                                Context context = view.getContext();
                                Intent intent = new Intent(context, cn.onboard.android.app.ui.Project.class);
                                intent.putExtra("projectId", project.getId());
                                intent.putExtra("companyId", companyId);
                                context.startActivity(intent);

                            }
                            if (project == null)
                                return;
                        }
                    });

                } else if (msg.what == -1) {
                    UIHelper.ToastMessage(Company.this,
                            getString(R.string.get_project_list_fail));
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    AppContext ac = (AppContext) getApplication();
                    List<Project> projectList = ac
                            .getProjectListByCompanyId(companyId);
                    msg.what = projectList.size();
                    msg.obj = projectList;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    private void initPageScroll() {
        mHeadTitles = getResources()
                .getStringArray(R.array.company_head_titles);
        mScrollLayout = (ScrollLayout) findViewById(R.id.company_scrolllayout);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.company_linearlayout_footer);
        mHeadTitles = getResources()
                .getStringArray(R.array.company_head_titles);
        mViewCount = mScrollLayout.getChildCount();
        mButtons = new RadioButton[mViewCount];
        for (int i = 0; i < mViewCount; i++) {
            mButtons[i] = (RadioButton) linearLayout.getChildAt(i * 2);
            mButtons[i].setTag(i);
            mButtons[i].setChecked(false);
            mButtons[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = (Integer) (v.getTag());
                    mScrollLayout.snapToScreen(pos);
                }
            });
        }
        // 设置第一显示屏
        mCurSel = 0;
        mButtons[mCurSel].setChecked(true);
        mScrollLayout
                .SetOnViewChangeListener(new ScrollLayout.OnViewChangeListener() {
                    public void OnViewChange(int viewIndex) {
                        if (viewIndex < 0 || viewIndex > mViewCount - 1
                                || mCurSel == viewIndex)
                            return;
                        mButtons[mCurSel].setChecked(false);
                        mButtons[viewIndex].setChecked(true);
                        mHeadTitle.setText(mHeadTitles[viewIndex]);
                        mCurSel = viewIndex;
                    }
                });
    }


    @Override
    public long getSupportedEventTypes() {
        return EventType.GO_TO | EventType.VIEW_EVENT | EventType.UPDATE_TITLE;
    }

    @Override
    public void handleEvent(EventInfo event) {
        if (event.eventType == EventType.GO_TO) {
            this.event = event;
        }
        if (event.eventType == EventType.VIEW_EVENT) {
            //TODO do something when an event is clicked
            dayView = false;
            eventView = true;
            this.event = event;

        }

    }

    @Override
    public void eventsChanged() {

    }

}
