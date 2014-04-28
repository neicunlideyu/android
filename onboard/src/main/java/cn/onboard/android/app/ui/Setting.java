package cn.onboard.android.app.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import cn.onboard.android.app.R;

/**
 * Created by xuchen on 14-4-28.
 */

public class Setting extends SherlockPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
