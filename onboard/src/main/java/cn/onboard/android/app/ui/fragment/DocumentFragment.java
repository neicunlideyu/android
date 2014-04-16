package cn.onboard.android.app.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.onboard.api.dto.Document;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.DocumentDetail;
import cn.onboard.android.app.ui.NewDiscussion;

public class DocumentFragment extends Fragment implements MenuItem.OnMenuItemClickListener {

	private int projectId;

	private int companyId;

	public DocumentFragment() {
		setRetainInstance(true);
	}

	public DocumentFragment(int companyId, int projectId) {
		this();
		this.projectId = projectId;
		this.companyId = companyId;
	}

	@SuppressLint("HandlerLeak")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final LinearLayout lv = (LinearLayout) inflater.inflate(
				R.layout.document_list, null);

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what >= 1) {
					@SuppressWarnings("unchecked")
					List<Document> documents = (List<Document>) msg.obj;
					ListViewNewsAdapter lvca = new ListViewNewsAdapter(
							getActivity().getApplicationContext(), documents,
							R.layout.document_listitem);
					ListView listView = (ListView) getActivity().findViewById(
							R.id.document_list_view);
					listView.setAdapter(lvca);
					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							Document document = null;
							// 判断是否是TextView
							if (view instanceof TextView) {
								document = (Document) view.getTag();
							} else {
								TextView tv = (TextView) view
										.findViewById(R.id.document_listitem_title);
								document = (Document) tv.getTag();
							}
							if (document == null)
								return;
							Context context = view.getContext();
							Intent intent = new Intent(context,
									DocumentDetail.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra("documentId", document.getId());
							intent.putExtra("companyId", companyId);
							intent.putExtra("projectId", projectId);
							intent.putExtra("documentTitle", document.getTitle());
							context.startActivity(intent);
							Log.i("documentDetails", "start");
						}
					});

				} else if(msg.what==0){
                    getActivity().findViewById(R.id.data_empty).setVisibility(View.VISIBLE);
                } else if (msg.what == -1) {
					UIHelper.ToastMessage(
                            getActivity().getApplicationContext(),
                            getString(R.string.get_todo_list_fail));
				}
                getActivity().findViewById(R.id.progress_bar).setVisibility(View.GONE);

            }
		};

		initGetDocumentsByProject(handler);
		return lv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent = new Intent(getActivity().getApplicationContext(), NewDiscussion.class);
        intent.putExtra("companyId", companyId);
        intent.putExtra("projectId", projectId);
        startActivity(intent);
        return true;
    }

    private void initGetDocumentsByProject(final Handler handler) {
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					AppContext ac = (AppContext) getActivity().getApplication();
                    List<Document> documents = ac.getDocumentsByProjectId(
							companyId, projectId);
					msg.what = documents.size();
					msg.obj = documents;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	public class ListViewNewsAdapter extends BaseAdapter {
        private List<Document> listItems;// 数据集合
		private LayoutInflater listContainer;// 视图容器
		private int itemViewResource;// 自定义项视图源

		class ListItemView { // 自定义控件集合
			public TextView title;
			public TextView author;
			public TextView date;
			public TextView count;
			public ImageView flag;
		}

		/**
		 * 实例化Adapter
		 * 
		 * @param context
		 * @param data
		 * @param resource
		 */
		public ListViewNewsAdapter(Context context, List<Document> data,
				int resource) {
            this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
			this.itemViewResource = resource;
			this.listItems = data;
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
				listItemView.title = (TextView) convertView
						.findViewById(R.id.document_listitem_title);
				listItemView.author = (TextView) convertView
						.findViewById(R.id.document_listitem_author);
				listItemView.count = (TextView) convertView
						.findViewById(R.id.document_listitem_commentCount);
				listItemView.date = (TextView) convertView
						.findViewById(R.id.document_listitem_date);
				listItemView.flag = (ImageView) convertView
						.findViewById(R.id.document_listitem_flag);

				// 设置控件集到convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (ListItemView) convertView.getTag();
			}

			// 设置文字和图片
			Document document = listItems.get(position);

			listItemView.title.setText(document.getTitle());
			listItemView.title.setTag(document);// 设置隐藏参数(实体类)
			listItemView.author.setText(document.getCreatorName());
			listItemView.date.setText(new SimpleDateFormat("yyyy-MM-dd")
					.format(document.getCreated()));
			listItemView.count.setText(10 + "");
			// if(StringUtils.isToday(document.getPubDate()))
			listItemView.flag.setVisibility(View.VISIBLE);
			// else
			// listItemView.flag.setVisibility(View.GONE);

			return convertView;
		}
	}

}
