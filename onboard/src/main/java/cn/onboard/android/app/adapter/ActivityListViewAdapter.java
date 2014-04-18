package cn.onboard.android.app.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onboard.api.dto.Activity;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;
import java.util.Locale;

import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;

/**
 * Created by xuchen on 14-4-18.
 */
public class ActivityListViewAdapter extends BaseAdapter {
    private final List<Activity> listItems;// 数据集合
    private final LayoutInflater listContainer;// 视图容器
    private final int itemViewResource;// 自定义项视图源
    private final BitmapManager bmpManager;

    /**
     * 实例化Adapter
     *
     * @param context
     * @param data
     * @param resource
     */
    public ActivityListViewAdapter(Context context, List<Activity> data,
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
            listItemView.face = (ImageView) convertView
                    .findViewById(R.id.activity_listitem_userface);
            listItemView.title = (TextView) convertView
                    .findViewById(R.id.activity_listitem_title);
            listItemView.author = (TextView) convertView
                    .findViewById(R.id.activity_listitem_author);
            listItemView.date = (TextView) convertView
                    .findViewById(R.id.activity_listitem_date);

            // 设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }

        // 设置文字和图片
        Activity activity = listItems.get(position);
        String faceURL = URLs.USER_FACE_HTTP + activity.getCreator().getAvatar();
        bmpManager.loadBitmap(faceURL, listItemView.face);
        // }
        // listItemView.face.setOnClickListener(faceClickListener);
        listItemView.face.setTag(activity);

        listItemView.title.setText("在项目" + activity.getProjectName() + activity.getSubject() + " " + activity.getTarget());
        listItemView.title.setTag(activity);// 设置隐藏参数(实体类)
        listItemView.author.setText(activity.getCreatorName());
        listItemView.date.setText(new PrettyTime(new Locale("zh")).format(activity.getCreated()));

        return convertView;
    }

    static class ListItemView { // 自定义控件集合
        public ImageView face;
        public TextView title;
        public TextView author;
        public TextView date;
    }
}
