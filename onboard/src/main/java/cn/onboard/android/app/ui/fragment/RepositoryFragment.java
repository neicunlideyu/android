package cn.onboard.android.app.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

class RepositoryFragment extends Fragment {

    private RepositoryFragment() {
		setRetainInstance(true);
	}

	public RepositoryFragment(int companyId, int projectId) {
		this();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout v = new RelativeLayout(getActivity());
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}
