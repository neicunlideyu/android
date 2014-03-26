package cn.onboard.android.app.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.onboard.api.dto.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.StringUtils;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.widget.pullrefresh.PullToRefreshListView;

/**
 * 应用程序Activity的基类
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-9-18
 */
public class CommentList extends Fragment {

	private ListViewAdapter lvCommentAdapter;
	private PullToRefreshListView lvComment;
	private List<Comment> comments = new ArrayList<Comment>();
	private int companyId;
    private int projectId;
    private String attachType;
    private int attachId;

    private InputMethodManager imm;
	private EditText commentContent;
	private Button commentPublish;
	private Comment comment;

    public CommentList(int companyId,int projectId,String attachType,int attachId){
        this.companyId=companyId;
        this.projectId=projectId;
        this.attachType=attachType;
        this.attachId=attachId;
        comments=new ArrayList<Comment>();
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		//setContentView(R.layout.comment_list);
        lvComment = (PullToRefreshListView) inflater.inflate(R.layout.comment_list, null);
//		getSupportActionBar().setHomeButtonEnabled(true);
//		getSupportActionBar().setIcon(R.drawable.head_back);
//		getSupportActionBar().setTitle("评论");
		initCommentListView();
        return lvComment;
	}


	/**
	 * 初始化动弹列表
	 */
	private void initCommentListView() {
		imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		commentContent = (EditText) getActivity().findViewById(R.id.comment_foot_editer);
		commentPublish = (Button) getActivity().findViewById(R.id.comment_foot_pubcomment);
		commentPublish.setOnClickListener(publishClickListener);
		lvCommentAdapter = new ListViewAdapter(getActivity(), comments,
				R.layout.tweet_listitem);
		lvComment = (PullToRefreshListView) getActivity().findViewById(R.id.frame_listview_tweet);
		// lvComment.addFooterView(lvComment_footer);// 添加底部视图 必须在setAdapter前
		lvComment.setAdapter(lvCommentAdapter);
		lvComment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// // 点击头部、底部栏无效
				// if (position == 0 || view == lvComment_footer)
				// return;

				Comment comment = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					comment = (Comment) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.tweet_listitem_username);
					comment = (Comment) tv.getTag();
				}
				if (comment == null)
					return;

				// 跳转到动弹详情&评论页面
				// UIHelper.showTweetDetail(view.getContext(), comment.getId());
			}
		});
	}

	public static class ListViewAdapter extends BaseAdapter {
		private Context context;// 运行上下文
		private List<Comment> listItems;// 数据集合
		private LayoutInflater listContainer;// 视图容器
		private int itemViewResource;// 自定义项视图源
		private BitmapManager bmpManager;

		static class ListItemView { // 自定义控件集合
			public ImageView userface;
			public TextView username;
			public TextView date;
			public TextView content;
			public TextView commentCount;
			public TextView client;
			public ImageView image;
		}

		/**
		 * 实例化Adapter
		 * 
		 * @param context
		 * @param data
		 * @param resource
		 */
		public ListViewAdapter(Context context, List<Comment> data,
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
				listItemView.userface = (ImageView) convertView
						.findViewById(R.id.tweet_listitem_userface);
				listItemView.username = (TextView) convertView
						.findViewById(R.id.tweet_listitem_username);
				listItemView.content = (TextView) convertView
						.findViewById(R.id.tweet_listitem_content);
				listItemView.image = (ImageView) convertView
						.findViewById(R.id.tweet_listitem_image);
				listItemView.date = (TextView) convertView
						.findViewById(R.id.tweet_listitem_date);
				listItemView.commentCount = (TextView) convertView
						.findViewById(R.id.tweet_listitem_commentCount);
				listItemView.client = (TextView) convertView
						.findViewById(R.id.tweet_listitem_client);

				// 设置控件集到convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (ListItemView) convertView.getTag();
			}

			// 设置文字和图片
			Comment comment = listItems.get(position);
			listItemView.username.setText(comment.getCreatorName());
			listItemView.username.setTag(comment);// 设置隐藏参数(实体类)
			listItemView.content.setText(comment.getContent());
			String test = new SimpleDateFormat("yyyy-MM-dd").format(comment
					.getCreated());

			listItemView.date.setText(new SimpleDateFormat("yyyy-MM-dd")
					.format(comment.getCreated()));

			if (StringUtils.isEmpty(listItemView.client.getText().toString()))
				listItemView.client.setVisibility(View.GONE);
			else
				listItemView.client.setVisibility(View.VISIBLE);

			String faceURL = "http://teamforge.b0.upaiyun.com/avatar/"
					+ comment.getCreatorId() + "/avatar.gif!avatar40";
			// if(faceURL.endsWith("portrait.gif") ||
			// StringUtils.isEmpty(faceURL)){
			// listItemView.userface.setImageResource(R.drawable.widget_dface);
			// }else{
			bmpManager.loadBitmap(faceURL, listItemView.userface);
			return convertView;
		}

	}

	private View.OnClickListener publishClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			// 隐藏软键盘
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//			setSupportProgressBarIndeterminateVisibility(true);
			String commentString = commentContent.getText().toString();
			if (StringUtils.isEmpty(commentString)) {
				UIHelper.ToastMessage(v.getContext(), "请输入评论内容");
				return;
			}

			final AppContext ac = (AppContext) getActivity().getApplication();
			comment = new Comment();

			comment.setAttachType(attachType);
			comment.setAttachId(attachId);
			comment.setCompanyId(companyId);
			comment.setProjectId(projectId);
			comment.setCreatorId(ac.getLoginUid());
			comment.setContent(commentString);
			// comment.setCompanyId(companyId);
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {

//					setSupportProgressBarIndeterminateVisibility(false);
					if (msg.what == 1) {
						commentContent.setText("");
						comments.add(comment);
						lvCommentAdapter.notifyDataSetChanged();
						return;
					} else {
						UIHelper.ToastMessage(getActivity(), "评论失败");
					}
				}
			};
			new Thread() {
				public void run() {
					Message msg = new Message();
					Log.i("comment", comment.getContent());
					try {
						comment = ac.publishComment(comment);
						msg.what = 1;
					} catch (AppException e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					}
					handler.sendMessage(msg);
				}
			}.start();
		}
	};

    public class GetCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(Void... voids) {
            AppContext ac = (AppContext) getActivity().getApplication();
            List<Comment> commentList = new ArrayList<Comment>();
            try {
                commentList = ApiClient
                        .getCommentsByCommentable(ac,companyId, projectId, attachType, attachId);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return commentList;
        }

        @Override
        protected void onPostExecute(List<Comment> commentList) {
            comments.clear();
            comments.addAll(commentList);
            lvCommentAdapter.notifyDataSetChanged();
        }
    }

}
