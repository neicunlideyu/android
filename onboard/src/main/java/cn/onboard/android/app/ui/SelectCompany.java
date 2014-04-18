package cn.onboard.android.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.onboard.api.dto.Company;

import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.CompanyListViewAdapter;
import cn.onboard.android.app.common.UIHelper;

public class SelectCompany extends BaseActivity {

    private CompanyListViewAdapter listViewCompanyAdapter;

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
                listViewCompanyAdapter = new CompanyListViewAdapter(getBaseContext(), companies, R.layout.company_item);
                companyListView.setAdapter(listViewCompanyAdapter);
                companyListView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
            } else {
                UIHelper.ToastMessage(SelectCompany.this, getString(R.string.get_company_list_fail));
            }
        }
    }



}
