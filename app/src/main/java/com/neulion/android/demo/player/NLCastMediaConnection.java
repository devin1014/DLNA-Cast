package com.neulion.android.demo.player;

import android.content.Context;

import com.neulion.media.control.MediaConnection.AbstractMediaConnection;
import com.neulion.media.control.MediaControl;
import com.neulion.media.control.MediaRequest;


public class NLCastMediaConnection extends AbstractMediaConnection
{
    private Context mContext;

    public NLCastMediaConnection(Context context)
    {
        super();

        mContext = context.getApplicationContext();
    }

    @Override
    public RemoteControl getRemoteControl(MediaControl player, MediaRequest request)
    {
        return new NLCastRemoteControl(mContext, request);
    }
}
