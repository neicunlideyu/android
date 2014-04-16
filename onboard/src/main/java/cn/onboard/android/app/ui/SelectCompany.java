package cn.onboard.android.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.onboard.api.dto.Company;

import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;

public class SelectCompany extends SherlockActivity {

    private ListViewCompanyAdapter listViewCompanyAdapter;

    private ListView companyListView;

    private ProgressBar loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_company);
        getActionBar().setTitle(R.string.company_list_title);
        initCompanyListView();
        new GetCompanyListTask().execute();
    }

    private void initCompanyListView() {
        companyListView = (ListView) findViewById(R.id.company_list);
        loading = (ProgressBar) findViewById(R.id.loading_progress_bar);
        companyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Company company = null;
                // 判断是否是TextView
                if (view instanceof TextView) {
                    company = (Company) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.company_item_title);
                    company = (Company) tv.getTag();
                }
                if (company == null)
                    return;

                Context context = view.getContext();
                Intent intent = new Intent(context, cn.onboard.android.app.ui.Company.class);
                intent.putExtra("companyId", company.getId());
                context.startActivity(intent);
            }
        });

    }


    private class GetCompanyListTask extends AsyncTask<Void, Void, List<Company>> {

        @Override
        protected List<Company> doInBackground(Void... params) {
            List<Company> companies = null;
            try {
                companies = ((AppContext) getApplication()).getCompanyList();
            } catch (AppException e) {
                e.printStackTrace();
            }
            return companies;
        }

        @Override
        protected void onPostExecute(List<Company> companies) {
            if (companies != null) {
                listViewCompanyAdapter = new ListViewCompanyAdapter(getBaseContext(), companies, R.layout.company_item);
                companyListView.setAdapter(listViewCompanyAdapter);
                companyListView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
            } else {
                UIHelper.ToastMessage(SelectCompany.this, getString(R.string.get_company_list_fail));
            }
        }
    }

    private static class ListViewCompanyAdapter extends BaseAdapter {
        private final List<Company> listItems;// 数据集合
        private final LayoutInflater listContainer;// 视图容器
        private final int itemViewResource;// 自定义项视图源

        static class ListItemView {                //自定义控件集合
            public TextView title;
            public TextView description;
        }

        public ListViewCompanyAdapter(Context context, List<Company> listItems,
                                      int itemViewResource) {
            this.listItems = listItems;
            this.listContainer = LayoutInflater.from(context);
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
            ListItemView listItemView = null;

            if (convertView == null) {
                //获取list_item布局文件的视图
                convertView = listContainer.inflate(this.itemViewResource, null);

                listItemView = new ListItemView();
                //获取控件对象
                listItemView.title = (TextView) convertView.findViewById(R.id.company_item_title);
                listItemView.description = (TextView) convertView.findViewById(R.id.company_item_description);
                //设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            //设置文字和图片
            Company company = listItems.get(position);

            listItemView.title.setText(company.getName());
            listItemView.description.setText(company.getDescription());
            listItemView.title.setTag(company);//设置隐藏参数(实体类)
            return convertView;

        }


    }

}
