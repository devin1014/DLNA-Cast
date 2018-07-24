package com.neulion.android.demo.render;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

public class ServiceStartStopReceiver extends BroadcastReceiver
{
    private ILogger mLogger = new DefaultLoggerImpl(this);

    @Override
    public void onReceive(Context context, Intent intent)
    {
        mLogger.d("onReceive: " + intent);

        if (intent.getAction() != null)
        {
            try
            {
                if (intent.getAction().equals(MediaRendererService.ACTION_START_RENDER))
                {
                    if (!MediaRendererService.isRunning())
                    {
                        context.startService(new Intent(context, MediaRendererService.class));
                    }
                }
                else if (intent.getAction().equals(MediaRendererService.ACTION_STOP_RENDER))
                {
                    context.stopService(new Intent(context, MediaRendererService.class));
                }
            }
            catch (Exception e)
            {
                mLogger.e("Failed to start/stop on intent " + e.getMessage());
            }
        }
    }
}
