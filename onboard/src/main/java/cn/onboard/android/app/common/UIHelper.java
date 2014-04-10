package cn.onboard.android.app.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.Toast;

import cn.onboard.android.app.AppManager;
import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.ModelType;
import cn.onboard.android.app.ui.DiscussionDetail;
import cn.onboard.android.app.ui.DocumentDetail;
import cn.onboard.android.app.ui.EditTodo;
import cn.onboard.android.app.ui.ImageDialog;
import cn.onboard.android.app.ui.ImageZoomDialog;
import cn.onboard.android.app.ui.UploadDetail;


/**
 * 应用程序UI工具包：封装UI相关的一些操作
 */
public class UIHelper {

    public final static int LISTVIEW_ACTION_INIT = 0x01;
    public final static int LISTVIEW_ACTION_REFRESH = 0x02;
    public final static int LISTVIEW_ACTION_SCROLL = 0x03;
    public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;

    public final static int LISTVIEW_DATA_MORE = 0x01;
    public final static int LISTVIEW_DATA_LOADING = 0x02;
    public final static int LISTVIEW_DATA_FULL = 0x03;
    public final static int LISTVIEW_DATA_EMPTY = 0x04;

    /**
     * 全局web样式
     */
    public final static String WEB_STYLE = "<style>* {font-size:16px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} " +
            "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} " +
            "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;} " +
            "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

    /**
     * 弹出Toast消息
     *
     * @param msg
     */
    public static void ToastMessage(Context cont, String msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, int msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, String msg, int time) {
        Toast.makeText(cont, msg, time).show();
    }


    /**
     * 发送App异常崩溃报告
     *
     * @param cont
     * @param crashReport
     */
    public static void sendAppCrashReport(final Context cont, final String crashReport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.app_error);
        builder.setMessage(R.string.app_error_message);
        builder.setPositiveButton(R.string.submit_report, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //发送异常报告
                Intent i = new Intent(Intent.ACTION_SEND);
                //i.setType("text/plain"); //模拟器
                i.setType("message/rfc822"); //真机
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"xuchen0602@126.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Onboard_Android客户端 - 错误报告");
                i.putExtra(Intent.EXTRA_TEXT, crashReport);
                cont.startActivity(Intent.createChooser(i, "发送错误报告"));
                //退出
                AppManager.getAppManager().AppExit(cont);
            }
        });
        builder.setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //退出
                AppManager.getAppManager().AppExit(cont);
            }
        });
        builder.show();
    }

    /**
     * 点击返回监听事件
     *
     * @param activity
     * @return
     */
    public static View.OnClickListener finish(final Activity activity) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        };
    }

    /**
     * 获取webviewClient对象
     *
     * @return
     */
    public static WebViewClient getWebViewClient() {
        return new WebViewClient() {
//			@Override
//			public boolean shouldOverrideUrlLoading(WebView view,String url) {
//				showUrlRedirect(view.getContext(), url);
//				return true;
//			}
        };
    }

    public static void pageLink(Context context, String type, int id, int companyId, int projectId) {
        Intent intent = null;
        if (type.equals(ModelType.DISCUSSSION)) {
            intent = new Intent(context,
                    DiscussionDetail.class);
            intent.putExtra("discussionId", id);
        } else if (type.equals(ModelType.DISCUSSSION)) {
            intent = new Intent(context,
                    DocumentDetail.class);
            intent.putExtra("projectId", projectId);
        } else if (type.equals(ModelType.TODO)) {
            intent = new Intent(context,
                    EditTodo.class);
            intent.putExtra("todoId", id);
            intent.putExtra("editType", EditTodo.EditType.UPDATE.value());
        } else if (type.equals(ModelType.UPLOAD)) {
            intent = new Intent(context,
                    UploadDetail.class);
            intent.putExtra("uploadId", id);
        }
        if (intent != null) {
            intent.putExtra("companyId", companyId);
            intent.putExtra("projectId", projectId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * 显示图片对话框
     * @param context
     * @param imgUrl
     */
    public static void showImageDialog(Context context, String imgUrl)
    {
        Intent intent = new Intent(context, ImageDialog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("img_url", imgUrl);
        context.startActivity(intent);
    }

    /**
     * 显示放大的图片
     * @param context
     * @param imgUrl
     */
    public static void showImageZoomDialog(Context context, String imgUrl)
    {
        Intent intent = new Intent(context, ImageZoomDialog.class);
        intent.putExtra("img_url", imgUrl);
        context.startActivity(intent);
    }


}
