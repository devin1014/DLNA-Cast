package com.neulion.android.demo.render;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

public class NetworkStateChangeReceiver extends BroadcastReceiver
{
    ILogger mLogger = new DefaultLoggerImpl(this);

    @Override
    public void onReceive(Context context, Intent intent)
    {
        mLogger.d("onReceive: " + intent);

        if (intent.getAction() != null)
        {
            final String action = intent.getAction();

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
            {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                mLogger.d("NetworkInfo = " + info.getState());

                if (!info.getState().equals(NetworkInfo.State.CONNECTED))
                {
                    context.stopService(new Intent(context, MediaRendererService.class));
                }
            }
        }
    }
}
