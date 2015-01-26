
package com.zycoo.android.zphone;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.zycoo.android.zphone.ui.LaunchActivity;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.sip.NgnAVSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine
        extends NgnEngine {
    private static final String CONTENT_TITLE = ZphoneApplication.getContext().getResources().getString(R.string.app_name);
    private static final int NOTIF_APP_ID = 19833894;
    private static final int NOTIF_MISS_CALL_ID = 19833893;
    private static final int NOTIF_AVCALL_ID = 19833892;
    private static final Logger sLogger = LoggerFactory.getLogger(Engine.class);

    public static Engine getInstance() {
        if (sInstance == null) {
            sInstance = new Engine();
        }
        return (Engine) sInstance;
    }

    private Engine() {
        super();
    }

    @Override
    public boolean start() {
        sLogger.debug("Engine start");
        return super.start();
    }

    @Override
    public boolean stop() {
        return super.stop();
    }

    @SuppressWarnings("deprecation")
    private void showNotificationOld(int notifId, int drawableId, String tickerText, String titleText) {

        // Set the icon, scrolling text and timestamp
        // 设置图标，滚动文本和时间戳
        final Notification notification;
        notification = new Notification(drawableId, "", System.currentTimeMillis());
        Intent intent =
                new Intent(ZphoneApplication.getContext(),
                        LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (notifId) {
            case NOTIF_MISS_CALL_ID:
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                break;
            case NOTIF_APP_ID:
                notification.flags |= Notification.FLAG_ONGOING_EVENT;
                intent.putExtra("notif-type", "reg");
                break;

            case NOTIF_AVCALL_ID:
                notification.flags |= Notification.FLAG_ONGOING_EVENT;
                tickerText =
                        String.format("%s (%d)", tickerText, NgnAVSession.getSize());
                notification.flags |= Notification.FLAG_ONGOING_EVENT;
                intent.putExtra("action", LaunchActivity.ACTION_SHOW_AVSCREEN);
                break;
            default:
                break;
        }

        PendingIntent contentIntent =
                PendingIntent.getActivity(ZphoneApplication.getContext(),
                        notifId/* requestCode */, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT < 11) {
            // Set the info for the views that show in the notification panel.
            if(titleText == null)
            {
                notification.setLatestEventInfo(ZphoneApplication.getContext(),
                        CONTENT_TITLE, tickerText, contentIntent);
            }
            else
            {
                notification.setLatestEventInfo(ZphoneApplication.getContext(),
                        titleText, tickerText, contentIntent);
            }

        }

        // Send the notification.
        // We use a layout id because it is a unique number. We use it later to
        // cancel.
        mNotifManager.notify(notifId, notification);
    }

    private void showNotificationNew(int notifId, int drawableId, String title, String contentText, String tickerText, long when) {
        // Set the icon, scrolling text and timestamp
        // 设置图标，滚动文本和时间戳
        final Notification notification;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                ZphoneApplication.getContext())
                .setSmallIcon(drawableId).setWhen(System.currentTimeMillis());
        Intent intent =
                new Intent(ZphoneApplication.getContext(),
                        LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (notifId) {
            case NOTIF_APP_ID:
                intent.putExtra("notif-type", "reg");
                break;

            case NOTIF_AVCALL_ID:
                tickerText =
                        String.format("%s (%d)", tickerText, NgnAVSession.getSize());
                intent.putExtra("action", LaunchActivity.ACTION_SHOW_AVSCREEN);
                break;
            case NOTIF_MISS_CALL_ID:
                builder.setAutoCancel(true);
                break;
            default:
                break;
        }

        PendingIntent contentIntent =
                PendingIntent.getActivity(ZphoneApplication.getContext(),
                        notifId/* requestCode */, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        if (title == null) {
            builder.setContentTitle(CONTENT_TITLE);
        } else {
            builder.setContentTitle(title);
        }
        if(contentText != null)
        {
            builder.setContentText(contentText);
        }
        if(tickerText !=null ) {
            builder.setTicker(tickerText);
        }
        if(when != 0)
        {
            builder.setWhen(when);
        }
        builder.setContentIntent(contentIntent);
        notification = builder.build();
        if (NOTIF_APP_ID == notifId || NOTIF_AVCALL_ID == notifId) {
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        }
        if(NOTIF_AVCALL_ID == notifId)
        {
            notification.sound = null;
        }
        // Send the notification.
        // We use a layout id because it is a unique number. We use it later to
        // cancel.
        mNotifManager.notify(notifId, notification);
    }

    /**
     * 从通知栏跳转到对应的界面
     *
     * @param notifId
     * @param drawableId
     * @param tickerText
     */

    private void showNotification(int notifId, int drawableId, String titleText, String contextText, String tickerText, long when) {
        if (!mStarted) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            showNotificationOld(notifId, drawableId, tickerText, titleText);
        } else {
            showNotificationNew(notifId, drawableId, titleText, contextText, tickerText, when);
        }

    }

    public void showAppNotif(int drawableId, String tickerText) {
        showNotification(NOTIF_APP_ID, drawableId, null, tickerText, tickerText, 0);
    }

    public void showMissCallNotif(int drawableId, String titleText, String number, long  when) {
        showNotification(NOTIF_MISS_CALL_ID, drawableId, titleText, number, titleText, when);
    }


    public void showAVCallNotif(int drawableId, String tickerText) {
        showNotification(NOTIF_AVCALL_ID, drawableId, null, tickerText, tickerText, 0);
    }

    public void cancelAVCallNotif() {
        if (!NgnAVSession.hasActiveSession()) {
            mNotifManager.cancel(NOTIF_AVCALL_ID);
        }
    }

    public void refreshAVCallNotif(int drawableId) {
        if (!NgnAVSession.hasActiveSession()) {
            mNotifManager.cancel(NOTIF_AVCALL_ID);
        } else {
            showNotification(NOTIF_AVCALL_ID, drawableId, null, getResString(R.string.string_incall), getResString(R.string.string_incall), 0);
        }
    }

    @Override
    public Class<? extends NgnNativeService> getNativeServiceClass() {
        return NativeService.class;
    }
    public String getResString(int id)
    {
        return ZphoneApplication.getAppResources().getString(id);
    }
}
