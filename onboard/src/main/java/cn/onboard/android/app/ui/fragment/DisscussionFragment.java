package cn.onboard.android.app.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.onboard.api.dto.Discussion;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.DiscussionDetail;
import cn.onboard.android.app.ui.NewDiscussion;

public class DisscussionFragment extends Fragment implements OnMenuItemClickListener {
	private int companyId;

	private int projectId;

	public DisscussionFragment() {
		setRetainInstance(true);
	}

	public DisscussionFragment(int companyId, int projectId) {
		this();
		this.companyId = companyId;
		this.projectId = projectId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final LinearLayout lv = (LinearLayout) inflater.inflate(
				R.layout.document_list, null);

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what >= 1) {
					@SuppressWarnings("unchecked")
					List<Discussion> discussions = (List<Discussion>) msg.obj;
					ListViewNewsAdapter lvca = new ListViewNewsAdapter(
							getActivity().getApplicationContext(), discussions,
							R.layout.question_listitem);
					ListView listView = (ListView) getActivity().findViewById(
							R.id.frame_listview_news);
					listView.setAdapter(lvca);
					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {

							Discussion discussion = null;
							// 判断是否是TextView
							if (view instanceof TextView) {
								discussion = (Discussion) view.getTag();
							} else {
								TextView tv = (TextView) view
										.findViewById(R.id.question_listitem_title);
								discussion = (Discussion) tv.getTag();
							}
							if (discussion == null)
								return;
							Context context = view.getContext();
							Intent intent = new Intent(context,
									DiscussionDetail.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra("discussionId", discussion.getId());
							intent.putExtra("companyId", companyId);
							intent.putExtra("projectId", projectId);
							intent.putExtra("discussionTitle", discussion.getSubject());
							context.startActivity(intent);

						}
					});

				} else if (msg.what == -1) {
					UIHelper.ToastMessage(
                            getActivity().getApplicationContext(),
                            getString(R.string.get_todo_list_fail));
				}
			}
		};
		initGetDiscussionsByProject(handler);
		return lv;
	}

	private void initGetDiscussionsByProject(final Handler handler) {
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					AppContext ac = (AppContext) getActivity().getApplication();
					List<Discussion> discussions = ac
							.getDiscussionsByProjectId(companyId, projectId);
					msg.what = discussions.size();
					msg.obj = discussions;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private static class ListViewNewsAdapter extends BaseAdapter {
		private Context context;// 运行上下文
		private List<Discussion> listItems;// 数据集合
		private LayoutInflater listContainer;// 视图容器
		private int itemViewResource;// 自定义项视图源
		private BitmapManager bmpManager;

		static class ListItemView { // 自定义控件集合
			public ImageView face;
			public TextView title;
			public TextView author;
			public TextView date;
			public TextView count;
		}

		/**
		 * 实例化Adapter
		 * 
		 * @param context
		 * @param data
		 * @param resource
		 */
		public ListViewNewsAdapter(Context context, List<Discussion> data,
				int resource) {
			this.context = context;
			this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
			this.itemViewResource = resource;
			this.listItems = data;
			this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.widget_dface_loading));
		}

		public int getCount() {
			return listItems.size();
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return 0;
		}

		/**
		 * ListView Item设置
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			// Log.d("method", "getView");

			// 自定义视图
			ListItemView listItemView = null;

			if (convertView == null) {
				// 获取list_item布局文件的视图
				convertView = listContainer
						.inflate(this.itemViewResource, null);

				listItemView = new ListItemView();
				// 获取控件对象
				listItemView.face = (ImageView) convertView
						.findViewById(R.id.question_listitem_userface);
				listItemView.title = (TextView) convertView
						.findViewById(R.id.question_listitem_title);
				listItemView.author = (TextView) convertView
						.findViewById(R.id.question_listitem_author);
				listItemView.count = (TextView) convertView
						.findViewById(R.id.question_listitem_count);
				listItemView.date = (TextView) convertView
						.findViewById(R.id.question_listitem_date);

				// 设置控件集到convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (ListItemView) convertView.getTag();
			}

			// 设置文字和图片
			Discussion discussion = listItems.get(position);
			String faceURL = "http://teamforge.b0.upaiyun.com/avatar/"
					+ discussion.getCreatorId() + "/avatar.gif";
			bmpManager.loadBitmap(faceURL, listItemView.face);
			// }
			// listItemView.face.setOnClickListener(faceClickListener);
			listItemView.face.setTag(discussion);

			listItemView.title.setText(discussion.getSubject());
			listItemView.title.setTag(discussion);// 设置隐藏参数(实体类)
			listItemView.author.setText(discussion.getCreatorName());
			listItemView.date.setText(new SimpleDateFormat("yyyy-MM-dd")
					.format(discussion.getCreated()));
			int commentCount = discussion.getComments() == null ? 0
					: discussion.getComments().size();
			listItemView.count.setText(commentCount + "回复");

			return convertView;
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		Intent intent = new Intent(getActivity().getApplicationContext(), NewDiscussion.class);
		intent.putExtra("companyId", companyId);
		intent.putExtra("projectId", projectId);
		startActivity(intent);
		return true;
	}

}
