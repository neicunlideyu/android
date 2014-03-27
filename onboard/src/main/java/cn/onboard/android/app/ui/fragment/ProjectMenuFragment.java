package cn.onboard.android.app.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.Project;

public class ProjectMenuFragment extends SherlockListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] project_head_titles = getResources().getStringArray(
				R.array.project_head_titles);
		ArrayAdapter<String> projectTitleAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_list_item_1,
				android.R.id.text1, project_head_titles);
		setListAdapter(projectTitleAdapter);
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
		Project project = (Project) getActivity();
		switch (position) {
		case 0:
			newContent = new RepositoryFragment(project.getCompanyId(),project.getProjectId());
			break;
		case 1:
			newContent = new DisscussionFragment(project.getCompanyId(),project.getProjectId());
            project.getSupportActionBar().setLogo(R.drawable.actionbar_discussion);
            project.getSupportActionBar().setTitle("讨论");
            project.setCreateString("新建讨论");
            project.setPopupListener((DisscussionFragment) newContent);
            break;
		case 2:
			newContent = new TodoFragment(project.getCompanyId(),project.getProjectId(),null);
            project.getSupportActionBar().setLogo(R.drawable.actionbar_todo);
            project.getSupportActionBar().setTitle("任务");
            project.setCreateString("新建任务列表");
            project.setPopupListener((TodoFragment) newContent);
            break;
		case 3:
			newContent = new DocumentFragment(project.getCompanyId(),project.getProjectId());
            project.getSupportActionBar().setLogo(R.drawable.actionbar_document);
            project.getSupportActionBar().setTitle("文档");
            project.setCreateString("新建文档");
            project.setPopupListener((DocumentFragment) newContent);
            break;
		case 4:
			newContent = new UploadFragment(project.getCompanyId(),project.getProjectId(),null);
            project.getSupportActionBar().setLogo(R.drawable.actionbar_upload);
            project.getSupportActionBar().setTitle("文件");
            project.setCreateString("上传文件");
            project.setPopupListener((UploadFragment) newContent);
            break;
		}
        if (newContent != null){
			switchFragment(newContent);
            project.invalidateOptionsMenu();
        }
	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof Project) {
			Project fca = (Project) getActivity();
			fca.switchContent(fragment);
		}
	}

}
