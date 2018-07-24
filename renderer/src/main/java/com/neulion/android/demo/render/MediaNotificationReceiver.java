package com.neulion.android.demo.render;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.app.NotificationCompat;

import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

public class MediaNotificationReceiver extends BroadcastReceiver
{
    private ILogger mLogger = new DefaultLoggerImpl(this);
    private static final int NOTIFICATION_ID = 7891;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        mLogger.d("onReceive: " + intent);

        if (intent.getAction() != null)
        {
            if (intent.getAction().equals(MediaRendererService.ACTION_STARTED))
            {
                setupNotification(context);
            }
            else if (intent.getAction().equals(MediaRendererService.ACTION_STOPPED))
            {
                clearNotification(context);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setupNotification(Context context)
    {
        mLogger.d("Setting up the notification");
        // Get NotificationManager reference
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);

        // Instantiate a Notification
        int icon = R.drawable.ic_stat_done;
        CharSequence tickerText = String.format(context.getString(R.string.notif_server_starting), MediaSettings.getDeviceName(context));
        long when = System.currentTimeMillis();

        // Define Notification's message and Intent
        CharSequence contentTitle = context.getString(R.string.notif_title);
        CharSequence contentText = String.format(context.getString(R.string.notif_text), MediaSettings.getDeviceName(context));

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        int stopIcon = android.R.drawable.ic_menu_close_clear_cancel;
        CharSequence stopText = context.getString(R.string.notif_stop_text);
        Intent stopIntent = new Intent(MediaRendererService.ACTION_STOP_RENDER);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context) //
                .setContentTitle(contentTitle) //
                .setContentText(contentText) //
                .setContentIntent(contentIntent) //
                .setSmallIcon(icon) //
                .setTicker(tickerText) //
                .setWhen(when) //
                .setOngoing(true);

        Notification notification = null;
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN)
        {
            nb.addAction(stopIcon, stopText, stopPendingIntent);
            notification = nb.build();
        }
        else
        {
            notification = nb.getNotification();
        }

        // Pass Notification to NotificationManager
        nm.notify(NOTIFICATION_ID, notification);

        mLogger.d("Notication setup done");
    }

    private void clearNotification(Context context)
    {
        mLogger.d("Clearing the notifications");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);
        nm.cancelAll();
        mLogger.d("Cleared notification");
    }
}
