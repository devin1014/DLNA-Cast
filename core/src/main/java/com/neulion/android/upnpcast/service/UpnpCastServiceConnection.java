package com.neulion.android.upnpcast.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.neulion.android.upnpcast.service.NLUpnpCastService.NLUpnpCastBinder;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-06-29
 * Time: 18:38
 */
public abstract class UpnpCastServiceConnection implements ServiceConnection
{
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        onServiceConnected(componentName, ((NLUpnpCastBinder) iBinder).getService());
    }

    public abstract void onServiceConnected(ComponentName componentName, NLUpnpCastService service);
}
