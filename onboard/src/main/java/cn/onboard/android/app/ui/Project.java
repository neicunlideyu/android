package cn.onboard.android.app.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.fragment.DocumentFragment;
import cn.onboard.android.app.ui.fragment.ProjectMenuFragment;
import cn.onboard.android.slidingmenu.SlidingMenu;
import cn.onboard.android.slidingmenu.app.SlidingFragmentActivity;

public class Project extends SlidingFragmentActivity {

    private Fragment mContent;

    private int companyId;

    private int projectId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        companyId = getIntent().getIntExtra("companyId", 0);

        projectId = getIntent().getIntExtra("projectId", 0);

        // set the Above View
        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(
                    savedInstanceState, "mContent");
        if (mContent == null)
            mContent = new DocumentFragment(companyId, projectId);

        // set the Above View
        setContentView(R.layout.content_frame);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mContent).commit();

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame, new ProjectMenuFragment()).commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);

//		 getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "mContent", mContent);
    }

    public void switchContent(Fragment fragment) {
        mContent = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
        getSlidingMenu().showContent();
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

}
