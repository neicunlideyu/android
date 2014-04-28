package cn.onboard.android.app.ui.fragment;

import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.common.base.Strings;
import com.onboard.api.dto.Comment;

import java.util.ArrayList;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.CommentListViewAdapter;
import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.common.UIHelper;


public class CommentListFragment extends Fragment {

    private CommentListViewAdapter lvCommentAdapter;
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

    private CommentListFragment() {
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
        final LinearLayout lv = (LinearLayout) inflater.inflate(R.layout.comments, null);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        commentContent = (EditText) getActivity().findViewById(R.id.comment_foot_editer);
        commentPublish = (Button) getActivity().findViewById(R.id.comment_foot_pubcomment);
        commentPublish.setOnClickListener(publishClickListener);
        lvComment = (ListView) lv.findViewById(R.id.frame_listview_comment);
        new GetCommentsTask().execute();
        return lv;
    }


    private final View.OnClickListener publishClickListener = new View.OnClickListener() {
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
                    if (msg.what == 1) {
                        commentContent.setText("");
                        comments.add(comment);
                        lvCommentAdapter.notifyDataSetChanged();
//                        setListViewHeightBasedOnChildren(lvComment);
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

    private class GetCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

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
            lvCommentAdapter = new CommentListViewAdapter(getActivity(), comments,
                    R.layout.comment_listitem);
            lvComment.setAdapter(lvCommentAdapter);
//            setListViewHeightBasedOnChildren(lvComment);
            if (comments.size() == 0)
                getActivity().findViewById(R.id.data_empty).setVisibility(View.VISIBLE);
        }
    }

}
