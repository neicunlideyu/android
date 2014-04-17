package cn.onboard.android.app.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Document;

import java.text.SimpleDateFormat;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.common.UIHelper;
import cn.onboard.android.app.ui.fragment.CommentListFragment;

public class DocumentDetail extends SherlockFragmentActivity {


    private TextView mAuthor;
    private TextView mPubDate;
    private TextView mCommentCount;

    private WebView mWebView;
    private Handler mHandler;
    private Document document;

    private int documentId;
    private int companyId;
    private int projectId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_detail);

        this.initView();
        new GetDocumentTask().execute();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void initView() {
        documentId = getIntent().getIntExtra("documentId", 0);
        companyId = getIntent().getIntExtra("companyId", 0);
        projectId = getIntent().getIntExtra("projectId", 0);

        mAuthor = (TextView) findViewById(R.id.document_creator);
        mPubDate = (TextView) findViewById(R.id.document_created_date);

        mWebView = (WebView) findViewById(R.id.document_content_webview);
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDefaultFontSize(15);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CommentListFragment commentList = new CommentListFragment(companyId, projectId, "document", documentId);
        ft.replace(R.id.document_comments, commentList).commit();

    }

    private class GetDocumentTask extends AsyncTask<Void, Void, Document> {

        @Override
        protected Document doInBackground(Void... params) {
            try {
                document = ((AppContext) getApplication())
                        .getDocumentById(companyId, projectId,
                                documentId);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            findViewById(R.id.loading_progress_bar).setVisibility(View.GONE);
            if (document != null) {
                mAuthor.setText(document.getCreatorName());
                mPubDate.setText(new SimpleDateFormat("yyyy-MM-dd")
                        .format(document.getCreated()));
                getSupportActionBar().setTitle("文档/" + document.getTitle());

                String body = UIHelper.WEB_STYLE + document.getContent()
                        + "<div style=\"margin-bottom: 80px\" />";
                // 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
                boolean isLoadImage;
                AppContext ac = (AppContext) getApplication();
                if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
                    isLoadImage = true;
                } else {
                    isLoadImage = ac.isLoadImage();
                }
                if (isLoadImage) {
                    body = body.replaceAll(
                            "(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
                    body = body.replaceAll(
                            "(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");
                } else {
                    body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
                }

                mWebView.loadDataWithBaseURL(null, body, "text/html",
                        "utf-8", null);
                mWebView.setWebViewClient(new WebViewClient());

            } else {
                UIHelper.ToastMessage(DocumentDetail.this, getString(R.string.get_document_fail));
            }
        }
    }

}
