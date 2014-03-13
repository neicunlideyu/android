package cn.onboard.android.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.onboard.domain.model.Company;

import java.util.List;

import cn.onboard.android.app.R;

public class ListViewCompanyAdapter extends BaseAdapter{
	private List<Company> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	static class ListItemView{				//自定义控件集合  
	        public TextView title;
	 }

	public ListViewCompanyAdapter(Context context, List<Company> listItems,
			int itemViewResource) {
		this.listItems = listItems;
		this.listContainer =  LayoutInflater.from(context);
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
		//Log.d("method", "getView");
		
		//自定义视图
		ListItemView  listItemView = null;
		
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			//获取控件对象
			listItemView.title = (TextView)convertView.findViewById(R.id.company_listitem_title);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}	
		
		//设置文字和图片
		Company company = listItems.get(position);
		
		listItemView.title.setText(company.getName());
		listItemView.title.setTag(company);//设置隐藏参数(实体类)
		return convertView;

	}  


}
