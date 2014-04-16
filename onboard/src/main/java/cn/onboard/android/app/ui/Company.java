package cn.onboard.android.app.ui;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.onboard.api.dto.Project;
import com.onboard.api.dto.User;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import java.util.List;
import java.util.Map;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.EveryoneAdapter;
import cn.onboard.android.app.adapter.GridViewProjectAdapter;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.fragment.ActivityFragment;
import cn.onboard.android.app.widget.calendar.CalendarController;
import cn.onboard.android.app.widget.calendar.MonthByWeekFragment;
import cn.onboard.android.app.widget.scroll.ScrollLayout;

public class Company extends FragmentActivity implements CalendarController.EventHandler {
    private AppContext ac;

    private int companyId;

    private ScrollLayout mScrollLayout;
    private RadioButton[] mButtons;
    private String[] mHeadTitles;
    private int mViewCount;
    private int mCurSel;

    private GridView projectGridView;
    private StickyGridHeadersGridView everyoneView;

    private CalendarController mController;
    private MonthByWeekFragment monthFrag;
    private ActivityFragment activityFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company);
        ac = (AppContext) getApplication();
        companyId = getIntent().getIntExtra("companyId", 0);
        initPageScroll();

        initProjectFrameView();
        initCalendarFrameView();
        initEveryOneFrameView();
        initActivityView();
        initMeFrameView();

        findViewById(R.id.app_footbar_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Company.this, Person.class);
                AppContext ac = (AppContext) getApplication();
                intent.putExtra("userId", ac.getLoginInfo().getId());
                intent.putExtra("companyId", companyId);
                startActivity(intent);
            }
        });
    }


    private void initProjectFrameView() {
        projectGridView = (GridView) findViewById(R.id.project_grid_list);
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
        new GetProjectListTask().execute();
    }

    private void initEveryOneFrameView() {
        everyoneView = (StickyGridHeadersGridView) findViewById(R.id.everyone_grid);
        new GetDepartmentNameUserMapTask().execute();
    }

    private void initCalendarFrameView() {
        mController = CalendarController.getInstance(this);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        monthFrag = new MonthByWeekFragment(System.currentTimeMillis(), false);
        monthFrag.setCompanyId(companyId);
        ft.replace(R.id.cal_frame, monthFrag).commit();
        mController.registerEventHandler(R.id.cal_frame, (CalendarController.EventHandler) monthFrag);
        mController.registerFirstEventHandler(0, this);

    }

    private void initActivityView() {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        activityFragment = new ActivityFragment(companyId, null);
        ft.replace(R.id.activity_frame, activityFragment).commit();

    }

    private void initMeFrameView() {
    }


    private void initPageScroll() {
        mHeadTitles = getResources()
                .getStringArray(R.array.company_head_titles);
        getActionBar().setTitle(mHeadTitles[0]);
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
                        getActionBar().setTitle(mHeadTitles[viewIndex]);
                        mCurSel = viewIndex;
                    }
                });
    }

    //get project info
    private class GetProjectListTask extends AsyncTask<Void, Void, List<Project>> {

        @Override
        protected List<Project> doInBackground(Void... params) {
            List<Project> projects = null;
            try {
                projects = ac.getProjectListByCompanyId(companyId);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return projects;
        }

        @Override
        protected void onPostExecute(List<Project> projects) {
            if (projects != null) {
                GridViewProjectAdapter gridViewProjectAdapter = new GridViewProjectAdapter(
                        getBaseContext(), projects, R.layout.project_item);
                projectGridView.setAdapter(gridViewProjectAdapter);
                findViewById(R.id.loading_progress_bar).setVisibility(View.GONE);
            } else {
                UIHelper.ToastMessage(Company.this,
                        getString(R.string.get_project_list_fail));
            }
        }
    }


    // get department info
    private class GetDepartmentNameUserMapTask extends AsyncTask<Void, Void, Map<String, List<User>>> {

        @Override
        protected Map<String, List<User>> doInBackground(Void... params) {
            Map<String, List<User>> departmentNameUserMap = null;
            try {
                departmentNameUserMap = ac.getDepartmentNameUserMapByCompanyId(companyId);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return departmentNameUserMap;
        }

        @Override
        protected void onPostExecute(Map<String, List<User>> departmentNameUserMap) {
            if (departmentNameUserMap != null) {
                everyoneView.setAdapter(new EveryoneAdapter(getApplicationContext(), departmentNameUserMap, companyId, R.layout.everyone_header, R.layout.everyone_item));
            } else {
                UIHelper.ToastMessage(Company.this,
                        getString(R.string.get_department_name_user_map_fail));
            }
        }
    }

    @Override
    public long getSupportedEventTypes() {
        return CalendarController.EventType.GO_TO | CalendarController.EventType.VIEW_EVENT | CalendarController.EventType.UPDATE_TITLE;
    }

    @Override
    public void handleEvent(CalendarController.EventInfo event) {
        if (event.eventType == CalendarController.EventType.GO_TO) {
            Intent intent = new Intent(Company.this, DayTodo.class);
            intent.putExtra("startTime", event.startTime.toMillis(true));
            intent.putExtra("companyId", companyId);
            startActivity(intent);
        }
    }

    @Override
    public void eventsChanged() {

    }

}
