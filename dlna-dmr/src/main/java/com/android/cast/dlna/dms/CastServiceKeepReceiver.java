package com.android.cast.dlna.dms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *
 */
public class CastServiceKeepReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DLNARendererService.startService(context);
    }
}
