package com.neulion.android.upnpcast.renderer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.neulion.android.upnpcast.renderer.NLUpnpRendererService;
import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-25
 * Time: 11:44
 */
public class CastServiceKeepReceiver extends BroadcastReceiver
{
    private ILogger mLogger = new DefaultLoggerImpl(this);

    @Override
    public void onReceive(Context context, Intent intent)
    {
        mLogger.i(String.format("onReceive:[%s]", intent));

        NLUpnpRendererService.startService(context);
    }
}
