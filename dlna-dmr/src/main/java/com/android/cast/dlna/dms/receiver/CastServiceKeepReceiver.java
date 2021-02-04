package com.android.cast.dlna.dms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.cast.dlna.dms.DLNARendererService;

/**
 *
 */
public class CastServiceKeepReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DLNARendererService.startService(context);
    }
}
