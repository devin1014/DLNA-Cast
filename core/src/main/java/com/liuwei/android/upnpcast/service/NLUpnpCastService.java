package com.liuwei.android.upnpcast.service;

import android.content.Intent;

import com.liuwei.android.upnpcast.util.ILogger;
import com.liuwei.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.registry.Registry;

/**
 */
public class NLUpnpCastService extends AndroidUpnpServiceImpl implements AndroidUpnpService
{
    private ILogger mLogger = new DefaultLoggerImpl(this);

    @Override
    public void onCreate()
    {
        mLogger.i("onCreate");

        org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());

        super.onCreate();

        binder = new NLUpnpCastBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mLogger.i("onStartCommand:" + intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        mLogger.w("onDestroy");

        super.onDestroy();
    }

    @Override
    public UpnpService get()
    {
        return binder.get();
    }

    @Override
    public UpnpServiceConfiguration getConfiguration()
    {
        return binder.getConfiguration();
    }

    @Override
    public Registry getRegistry()
    {
        return binder.getRegistry();
    }

    @Override
    public ControlPoint getControlPoint()
    {
        return binder.getControlPoint();
    }

    // -------------------------------------------------------------------------------------
    // Upnp cast binder
    // -------------------------------------------------------------------------------------
    public class NLUpnpCastBinder extends Binder
    {
        public NLUpnpCastService getService()
        {
            return NLUpnpCastService.this;
        }
    }
}
