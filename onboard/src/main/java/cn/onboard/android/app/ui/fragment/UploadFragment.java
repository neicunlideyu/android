
package cn.onboard.android.app.ui.fragment;

import java.text.SimpleDateFormat;
import java.util.List;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.onboard.api.dto.Attachment;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.DiscussionDetail;
import cn.onboard.android.app.ui.Project;
import cn.onboard.android.app.ui.TodoDetail;
import cn.onboard.android.app.ui.UploadDetail;

public class UploadFragment extends Fragment {
    private int companyId;

    private int projectId;

    private static String cookie;

    private final String DISCUSSION_TYPE_NAME = "discussion";
    private final String UPLOAD_TYPE_NAME = "upload";
    private final String TODO_TYPE_NAME = "todo";

    public UploadFragment() {
        setRetainInstance(true);
    }

    public UploadFragment(int companyId, int projectId) {
        this();
        this.companyId = companyId;
        this.projectId = projectId;
    }

    private static class ListViewNewsAdapter extends BaseAdapter {
        private Context context;// 运行上下文
        private List<Attachment> listItems;// 数据集合
        private LayoutInflater listContainer;// 视图容器
        private int itemViewResource;// 自定义项视图源
        private BitmapManager bmpManager;

        static class ListItemView { // 自定义控件集合
            public ImageView face;
            public TextView title;
            public TextView author;
            public TextView date;
            public Button btn_download;
        }

        /**
         * 实例化Adapter
         *
         * @param context
         * @param attachments
         * @param resource
         */
        public ListViewNewsAdapter(Context context, List<Attachment> attachments, int resource) {
            this.context = context;
            this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
            this.itemViewResource = resource;
            this.listItems = attachments;
            this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.widget_dface_loading));
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

            // 自定义视图
            ListItemView listItemView = null;
            final Attachment attachment = listItems.get(position);

            if (convertView == null) {
                // 获取list_item布局文件的视图
                convertView = listContainer.inflate(this.itemViewResource, null);

                listItemView = new ListItemView();
                // 获取控件对象
                listItemView.face = (ImageView) convertView.findViewById(R.id.question_listitem_userface);
                listItemView.title = (TextView) convertView.findViewById(R.id.question_listitem_title);
                listItemView.author = (TextView) convertView.findViewById(R.id.question_listitem_author);
                listItemView.date = (TextView) convertView.findViewById(R.id.question_listitem_date);
                listItemView.btn_download = (Button) convertView.findViewById(R.id.button_download);

                listItemView.btn_download.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        AppContext ac = (AppContext) v.getContext();
                        ac.downloadAttachmentByAttachmentId(attachment.getId(), attachment.getCompanyId(),
                                attachment.getProjectId());
                    }
                });

                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            // 设置文字和图片
            String faceURL = "http://onboard.cn/" + attachment.getCompanyId() + "/projects/" + attachment.getProjectId()
                    + "/attachments/image/" + attachment.getId();
            if (!attachment.getContentType().contains("image")) {
                faceURL = "http://onboard.cn/static/img/attachment-icon/default.png";
            }
            bmpManager.loadBitmap(faceURL, listItemView.face);
            // }
            // listItemView.face.setOnClickListener(faceClickListener);
            listItemView.face.setTag(attachment);

            listItemView.title.setText(attachment.getName());
            listItemView.title.setTag(attachment);// 设置隐藏参数(实体类)
            listItemView.author.setText(attachment.getCreatorName());
            listItemView.date.setText(new SimpleDateFormat("yyyy-MM-dd").format(attachment.getCreated()));

            return convertView;
        }
    }

    void startUploadDetailActivityForAttachment(int targetUploadId, String uploadTitle) {
        Intent intent = new Intent(getActivity().getApplicationContext(), UploadDetail.class);
        intent.putExtra("companyId", companyId);
        intent.putExtra("projectId", projectId);
        intent.putExtra("uploadId", targetUploadId);
        intent.putExtra("uploadTitle", uploadTitle);
        startActivity(intent);
    }

    void startTodoDetailActivityForAttachment(int targetTodoId, String TodoTitle) {
        Intent intent = new Intent(getActivity().getApplicationContext(), TodoDetail.class);
        intent.putExtra("companyId", companyId);
        intent.putExtra("projectId", projectId);
        intent.putExtra("todoId", targetTodoId);
        intent.putExtra("todoTitle", TodoTitle);
        startActivity(intent);
    }

    void startDiscussionDetailActivityForAttachment(int targetDiscussionId, String discussionTitle) {
        Intent intent = new Intent(getActivity().getApplicationContext(), DiscussionDetail.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("discussionId", targetDiscussionId);
        intent.putExtra("companyId", companyId);
        intent.putExtra("projectId", projectId);
        intent.putExtra("discussionTitle", discussionTitle);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout lv = (LinearLayout) inflater.inflate(R.layout.upload_list, null);

        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what >= 1) {
                    @SuppressWarnings("unchecked")
                    List<Attachment> attachments = (List<Attachment>) msg.obj;
                    ListViewNewsAdapter lvca = new ListViewNewsAdapter(getActivity().getApplicationContext(), attachments,
                            R.layout.upload_listitem);

                    ListView listView = (ListView) getActivity().findViewById(R.id.upload_list_view);
                    listView.setAdapter(lvca);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Attachment attachment = null;
                            // 判断是否是TextView
                            if (view instanceof TextView) {
                                attachment = (Attachment) view.getTag();
                            } else {
                                TextView tv = (TextView) view.findViewById(R.id.question_listitem_title);
                                attachment = (Attachment) tv.getTag();
                            }
                            if (attachment == null)
                                return;
                            if (attachment.getAttachType().equals(DISCUSSION_TYPE_NAME)) {
                                startDiscussionDetailActivityForAttachment(attachment.getTargetId(), attachment.getName());
                            } else if (attachment.getAttachType().equals(UPLOAD_TYPE_NAME)) {
                                startUploadDetailActivityForAttachment(attachment.getTargetId(), attachment.getName());
                            } else if (attachment.getAttachType().equals(TODO_TYPE_NAME)) {
                                startTodoDetailActivityForAttachment(attachment.getTargetId(), attachment.getName());
                            }
                        }
                    });

                } else if (msg.what == -1) {
                    UIHelper.ToastMessage(getActivity().getApplicationContext(), getString(R.string.get_todo_list_fail));
                }
            }
        };
        Project project = (Project) getActivity();

        project.getSupportActionBar().setLogo(R.drawable.frame_logo_news);
        project.getSupportActionBar().setTitle("文件");
        project.setCreateString("上传文件");
        project.invalidateOptionsMenu();
        initGetUploadsByProject(handler);
        return lv;
    }

    private void initGetUploadsByProject(final Handler handler) {
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    AppContext ac = (AppContext) getActivity().getApplication();
                    List<Attachment> attachments = ac.getAttachmentsByProjectId(companyId, projectId);
                    msg.what = attachments.size();
                    msg.obj = attachments;
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

}

