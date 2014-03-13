package cn.onboard.android.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.onboard.domain.model.Company;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.ListViewCompanyAdapter;
import cn.onboard.android.app.common.UIHelper;

@SuppressLint("HandlerLeak")
public class SelectCompany extends SherlockActivity {

    private Handler handler;

    private AppContext appContext;

	private List<Company> companyList = new ArrayList<Company>();

	ListViewCompanyAdapter listViewCompanyAdapter;

	ListView companyListView;
	
    View companyListLoading;
    
    @SuppressLint("HandlerLeak")
	private Handler getHandler(final Context context) {
        return new Handler() {
            @SuppressWarnings("unchecked")
			public void handleMessage(Message msg) {
                if (msg.what >=0 ) {
                    companyList = (List<Company>) msg.obj;             
                    listViewCompanyAdapter = new ListViewCompanyAdapter(context, companyList, R.layout.company_item);
                    companyListView.setAdapter(listViewCompanyAdapter);
                    companyListView.setVisibility(0);
                    companyListLoading.setVisibility(8);
                } else if (msg.what == -1) {
                    UIHelper.ToastMessage(SelectCompany.this, getString(R.string.get_company_list_fail));
                }
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_company);
        getActionBar().setTitle("公司列表");
        initCompanyListView();
        appContext = (AppContext) getApplication();
        handler = getHandler(this);
        getCompanyList(handler);
    }

    private void initCompanyListView() {
        companyListView = (ListView) findViewById(R.id.company_list);
        companyListLoading = (View) findViewById(R.id.company_list_loading);
        AnimationDrawable loadingAnimation = (AnimationDrawable) companyListLoading.getBackground();
        loadingAnimation.start();
        companyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Company company = null;
                // 判断是否是TextView
                if (view instanceof TextView) {
                    company = (Company) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.company_listitem_title);
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
    private void getCompanyList(final Handler handler) {
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    List<Company> list = appContext.getCompanyList();
                    msg.what = list.size();
                    msg.obj = list;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

}
