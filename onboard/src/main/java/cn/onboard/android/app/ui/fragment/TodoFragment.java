package cn.onboard.android.app.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class TodoFragment extends Fragment {

	private int companyId;

	private int projectId;

	public TodoFragment() {
		setRetainInstance(true);
	}

	public TodoFragment(int companyId, int projectId) {
		this();
		this.companyId = companyId;
		this.projectId = projectId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// construct the RelativeLayout
		RelativeLayout v = new RelativeLayout(getActivity());
		v.setBackgroundColor(Color.LTGRAY);
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}
