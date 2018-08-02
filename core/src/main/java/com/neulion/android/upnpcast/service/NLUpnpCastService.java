package com.neulion.android.upnpcast.service;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.registry.Registry;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-06-29
 * Time: 18:09
 */
public class NLUpnpCastService extends AndroidUpnpServiceImpl implements AndroidUpnpService
{
    @Override
    public void onCreate()
    {
        org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());

        super.onCreate();

        binder = new NLUpnpCastBinder();
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
