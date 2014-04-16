package cn.onboard.android.app.ui.fragment;

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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.onboard.api.dto.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.UIHelper;

/**
 * 应用程序Activity的基类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-9-18
 */
public class CommentListFragment extends Fragment {

    private ListViewAdapter lvCommentAdapter;
    private ListView lvComment;
    private List<Comment> comments = new ArrayList<Comment>();
    private int companyId;
    private int projectId;
    private String attachType;
    private int attachId;

    private InputMethodManager imm;
    private EditText commentContent;
    private Button commentPublish;
    private Comment comment;

    public CommentListFragment() {
        setRetainInstance(true);
    }

    public CommentListFragment(int companyId, int projectId, String attachType, int attachId) {
        this();
        this.companyId = companyId;
        this.projectId = projectId;
        this.attachType = attachType;
        this.attachId = attachId;
        comments = new ArrayList<Comment>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //setContentView(R.layout.comment_list);
        final LinearLayout lv = (LinearLayout) inflater.inflate(R.layout.comments, null);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        commentContent = (EditText) getActivity().findViewById(R.id.comment_foot_editer);
        commentPublish = (Button) getActivity().findViewById(R.id.comment_foot_pubcomment);
        commentPublish.setOnClickListener(publishClickListener);
        lvComment = (ListView) lv.findViewById(R.id.frame_listview_comment);
        new GetCommentsTask().execute();
        return lv;
    }


    public static class ListViewAdapter extends BaseAdapter {
        private List<Comment> listItems;// 数据集合
        private LayoutInflater listContainer;// 视图容器
        private int itemViewResource;// 自定义项视图源
        private BitmapManager bmpManager;

        static class ListItemView { // 自定义控件集合
            public ImageView userface;
            public TextView username;
            public TextView date;
            public TextView content;
            public TextView client;
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
                        .findViewById(R.id.comment_listitem_userface);
                listItemView.username = (TextView) convertView
                        .findViewById(R.id.comment_listitem_username);
                listItemView.content = (TextView) convertView
                        .findViewById(R.id.comment_listitem_content);
                listItemView.date = (TextView) convertView
                        .findViewById(R.id.comment_listitem_date);
                listItemView.client = (TextView) convertView
                        .findViewById(R.id.comment_listitem_client);

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

            if (Strings.isNullOrEmpty(listItemView.client.getText().toString()))
                listItemView.client.setVisibility(View.GONE);
            else
                listItemView.client.setVisibility(View.VISIBLE);

            String faceURL = URLs.USER_FACE_HTTP + comment.getCreator().getAvatar();
            // if(faceURL.endsWith("portrait.gif") ||
            // Strings.isNullOrEmpty(faceURL)){
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
            if (Strings.isNullOrEmpty(commentString)) {
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
                        setListViewHeightBasedOnChildren(lvComment);
                        getActivity().findViewById(R.id.data_empty).setVisibility(View.GONE);
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
                        .getCommentsByCommentable(ac, companyId, projectId, attachType, attachId);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return commentList;
        }

        @Override
        protected void onPostExecute(List<Comment> commentList) {
            comments.clear();
            comments.addAll(commentList);
            lvCommentAdapter = new ListViewAdapter(getActivity(), comments,
                    R.layout.comment_listitem);
            // lvComment.addFooterView(lvComment_footer);// 添加底部视图 必须在setAdapter前
            lvComment.setAdapter(lvCommentAdapter);
            setListViewHeightBasedOnChildren(lvComment);
            if (comments.size() == 0)
                getActivity().findViewById(R.id.data_empty).setVisibility(View.VISIBLE);
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}
