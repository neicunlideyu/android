package cn.onboard.android.app.ui;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
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
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.UIHelper;

public class SelectCompany extends SherlockActivity {

    private AppContext appContext;

    private ListViewCompanyAdapter listViewCompanyAdapter;

    private ListView companyListView;

    private ProgressBar loading;

    private AlertDialog updateNotificationDialog;

    private AlertDialog installNotificationDialog;

    private long apkDownloadReference;

    private DownloadManager downloadManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
        new GetCompanyListTask().execute();
        new CheckVersionTask().execute();
    }

    private void createView() {
        appContext = (AppContext) getApplication();
        setContentView(R.layout.select_company);
        getActionBar().setTitle(R.string.company_list_title);
        initCompanyListView();


        downloadManager = (DownloadManager) appContext.getSystemService(Context.DOWNLOAD_SERVICE);

        updateNotificationDialog = new AlertDialog.Builder(SelectCompany.this)
                .setTitle("软件更新")
                .setMessage("有新版本是否更新")
                .setPositiveButton("确定", toUpdateListener)
                .setNegativeButton("暂不更新", notUpdateListener).create();

        installNotificationDialog = new AlertDialog.Builder(SelectCompany.this)
                .setTitle("安装")
                .setMessage("已下载完成是否更新")
                .setPositiveButton("确定", toInstallListener)
                .setNegativeButton("暂不更新", notInstallListener).create();
    }

    DialogInterface.OnClickListener toUpdateListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            downloadNewVersion();
        }
    };

    DialogInterface.OnClickListener notUpdateListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            updateNotificationDialog.dismiss();
        }
    };

    DialogInterface.OnClickListener toInstallListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            installNewVersion(downloadManager);
        }
    };

    DialogInterface.OnClickListener notInstallListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            installNotificationDialog.dismiss();
        }
    };

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


    private void downloadNewVersion() {
        String newUrl = URLs.LATEST_VERSION_DOWNLOAD_HTTP;

        Uri uri = Uri.parse(newUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("onboard更新");
        request.setDestinationInExternalFilesDir(appContext, null, "onboard.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        apkDownloadReference = downloadManager.enqueue(request);
        UIHelper.ToastMessage(appContext, "已进入后台下载");

        checkIfdownloaded();
    }

    private void checkIfdownloaded() {
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (reference == apkDownloadReference) {
                    installNotificationDialog.show();

                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private void installNewVersion(DownloadManager downloadManager) {
        DownloadManager.Query updateDownloadQuery = new DownloadManager.Query();
        updateDownloadQuery.setFilterById(apkDownloadReference);
        Cursor thisDownload = downloadManager.query(updateDownloadQuery);
        String fileName = "onboard.apk";
        if (thisDownload.moveToFirst()) {
            int fileUriIndex = thisDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            fileName = thisDownload.getString(fileUriIndex);
        }
        Intent install = new Intent();
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.setAction(Intent.ACTION_VIEW);
        install.setDataAndType(Uri.parse(fileName),
                "application/vnd.android.package-archive");
        startActivity(install);
    }

    private class CheckVersionTask extends AsyncTask<String, Boolean, Boolean> {

        AppContext appContext;

        @Override
        protected Boolean doInBackground(String... params) {
            appContext = (AppContext) getApplication();
            apkDownloadReference = 0;
            int currentVersionCode = appContext.getPackageInfo().versionCode;
            Integer lastestVersionCode = currentVersionCode;
            try {
                lastestVersionCode = appContext.getLatestVersionCode();
            }
            catch (AppException e) {
                e.printStackTrace();
            }
            return lastestVersionCode > currentVersionCode ? true : false;
        }

        @Override
        protected void onPostExecute(Boolean ifUpdate) {
            if (ifUpdate) {
                updateNotificationDialog.show();
            }
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }

}
