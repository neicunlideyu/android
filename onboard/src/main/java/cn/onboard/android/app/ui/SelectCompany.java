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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.onboard.api.dto.Company;

import org.springframework.web.client.RestClientException;

import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.CompanyListViewAdapter;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.core.comment.CommentService;
import cn.onboard.android.app.core.company.CompanyService;

public class SelectCompany extends BaseActivity {

    private AppContext appContext;

    private CompanyService companyService;

    private CompanyListViewAdapter listViewCompanyAdapter;

    private ListView companyListView;

    private ProgressBar loading;

    private AlertDialog updateNotificationDialog;

    private AlertDialog installNotificationDialog;

    private long apkDownloadReference;

    private DownloadManager downloadManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initService();
        createView();
        new GetCompanyListTask().execute();
        new CheckVersionTask().execute();
    }

    private void initService() {
        companyService = new CompanyService((AppContext)getApplicationContext());
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
            } catch (AppException e) {
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


    private class GetCompanyListTask extends AsyncTask<Void, Void, List<Company>> {

        @Override
        protected List<Company> doInBackground(Void... params) {
            List<Company> companies = null;
            try {
                companies = companyService.getCompanyList();
            } catch (RestClientException e) {
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
