package cn.onboard.android.app.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.fragment.TodoFragment;

/**
 * 查看某一天的todo
 */
public class DayTodo extends SherlockFragmentActivity {

    private int companyId;
    private long startTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_todo);
        companyId = getIntent().getIntExtra("companyId", 0);
        startTime = getIntent().getLongExtra("startTime", 0);
        Date date = new Date();
        date.setTime(startTime);
        getSupportFragmentManager().beginTransaction().replace(R.id.day_todo, new TodoFragment(companyId, null, null, date)).commit();
        getSupportActionBar().setTitle(new SimpleDateFormat("yyyy-MM-dd日的任务").format(date));
    }


}
