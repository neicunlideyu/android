package cn.onboard.android.app.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.onboard.api.dto.Comment;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;

public class CommentListViewAdapter extends BaseAdapter {
    private final List<Comment> listItems;// 数据集合
    private final LayoutInflater listContainer;// 视图容器
    private final int itemViewResource;// 自定义项视图源
    private final BitmapManager bmpManager;

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
    public CommentListViewAdapter(Context context, List<Comment> data,
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
