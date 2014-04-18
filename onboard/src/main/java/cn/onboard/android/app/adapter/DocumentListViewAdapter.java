package cn.onboard.android.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onboard.api.dto.Document;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.onboard.android.app.R;

public class DocumentListViewAdapter extends BaseAdapter {
    private final List<Document> listItems;// 数据集合
    private final LayoutInflater listContainer;// 视图容器
    private final int itemViewResource;// 自定义项视图源

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
    public DocumentListViewAdapter(Context context, List<Document> data,
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
