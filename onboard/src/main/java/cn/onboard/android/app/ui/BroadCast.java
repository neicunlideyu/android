package cn.onboard.android.app.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.onboard.api.dto.Activity;

import org.codehaus.jackson.type.TypeReference;

import cn.onboard.android.app.R;
import cn.onboard.android.app.common.DataHandleUtil;
import cn.onboard.android.app.common.UIHelper;

public class BroadCast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String activityString = intent.getStringExtra("activity");
        Activity activity = null;
        activity = DataHandleUtil.StringToObject(new TypeReference<Activity>() {
        }, activityString);
        if (activity != null)
            notification(context, 1, activity);

    }

    private void notification(Context context, int noticeCount, Activity activity) {
        //创建 NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String contentTitle = "onboard";
        String contentText = activity.getCreatorName() + "在项目" + activity.getProjectName() + activity.getSubject() + " " + activity.getTarget();
        //创建通知 Notification
        Notification notification = null;

        String noticeTitle = "您有1条最新信息";
        notification = new Notification(R.drawable.logo, noticeTitle, System.currentTimeMillis());

        Intent intent = UIHelper.pageLink(context, activity.getAttachType(), activity.getAttachId(), activity.getCompanyId(), activity.getProjectId());

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //设置最新信息
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        //设置点击清除通知
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        //设置通知方式
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        //发出通知
        notificationManager.notify(activity.getId(), notification);
    }

}
