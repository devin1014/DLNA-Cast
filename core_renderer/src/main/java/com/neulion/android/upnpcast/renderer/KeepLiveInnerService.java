package com.neulion.android.upnpcast.renderer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-08-02
 * Time: 10:44
 */
public class KeepLiveInnerService extends Service
{
    private ILogger mLogger = new DefaultLoggerImpl(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mLogger.i(getClass().getSimpleName() + " onCreate!");

        //发送与KeepLiveService中ID相同的Notification，然后将其取消并取消自己的前台显示
        Notification.Builder builder = new Notification.Builder(this);
        //builder.setSmallIcon(R.mipmap.ic_launcher);
        startForeground(NLUpnpRendererService.NOTIFICATION_ID, builder.build());
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                stopForeground(true);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(NLUpnpRendererService.NOTIFICATION_ID);
                stopSelf();
            }
        }, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mLogger.i(getClass().getSimpleName() + " onStartCommand:" + intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        mLogger.i(getClass().getSimpleName() + " onDestroy!");

        super.onDestroy();
    }
}
