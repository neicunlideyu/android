package cn.onboard.android.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.onboard.api.dto.Project;

import java.util.List;

import cn.onboard.android.app.R;

public class ProjectGridViewAdapter extends BaseAdapter {
    private final List<Project> listItems;// 数据集合
    private final LayoutInflater gridContainer;// 视图容器
    private final int itemViewResource;// 自定义项视图源

    static class GridItemView {                //自定义控件集合
        public TextView title;
    }

    public ProjectGridViewAdapter(Context context, List<Project> listItems,
                                  int itemViewResource) {
        this.listItems = listItems;
        this.gridContainer = LayoutInflater.from(context);
        this.itemViewResource = itemViewResource;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("method", "getView");

        //自定义视图
        GridItemView gridItemView = null;

        if (convertView == null) {
            //获取list_item布局文件的视图
            convertView = gridContainer.inflate(this.itemViewResource, null);

            gridItemView = new GridItemView();
            //获取控件对象
            gridItemView.title = (TextView) convertView.findViewById(R.id.project_name);

            //设置控件集到convertView
            convertView.setTag(gridItemView);
        } else {
            gridItemView = (GridItemView) convertView.getTag();
        }

        //设置文字和图片
        Project project = listItems.get(position);

        gridItemView.title.setText(project.getName());
        gridItemView.title.setTag(project);//设置隐藏参数(实体类)
        return convertView;

    }


}
