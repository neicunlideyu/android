package cn.onboard.android.app.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;

import com.onboard.api.dto.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.widget.calendar.CalendarController;

public class EditTodo extends BaseActivity implements CalendarController.EventHandler {

	List<User> assigneesList;

	private TextView assigneeText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_edit);

		findViewById(R.id.ll_assignee).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						DialogFragment newFragment = new AssigneeDialogFragment();
						newFragment.show(getFragmentManager(), "assignees");
					}
				});
		getAssigeesList();
		
		findViewById(R.id.ll_assigndate).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_YEAR, 1);
				final DatePickerDialog dateDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
				    boolean fired = false;
				    public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
				        Log.i("PEW PEW", "Double fire check");
				        if (fired == true) {
				            Log.i("PEW PEW", "Double fire occured. Silently-ish returning");
				            return;
				        } else {
				            //first time fired
				            fired = true;
				        }
				        //Normal date picking logic goes here
				    }
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				dateDialog.show();
			}
		});

	}

	String[] getAssigneeNameList() {
		List<String> names = new ArrayList<String>();
		for (User user : assigneesList) {
			names.add(user.getName());
		}
		names.add("不指定");
		String[] array = new String[names.size()];
		names.toArray(array); // fill the array
		return array;

	}

	void getAssigeesList(){
		new Thread(){
			public void run(){
				Message msg = new Message();
				try {
					final AppContext ac = (AppContext) getApplication();
					assigneesList = ac.getUsersByProjectId(1, 34);
					msg.what = 1;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}

			}
		}.start();
	}

	private class AssigneeDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("选择负责人")
					.setSingleChoiceItems(getAssigneeNameList(), 0, null)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
								}
							});

			return builder.create();
		}
	}

	@Override
	public long getSupportedEventTypes() {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public void handleEvent(CalendarController.EventInfo event) {

    }


	@Override
	public void eventsChanged() {
		// TODO Auto-generated method stub

	}

}
