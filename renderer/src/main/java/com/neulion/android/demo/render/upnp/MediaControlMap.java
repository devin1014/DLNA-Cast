package com.neulion.android.demo.render.upnp;

import android.content.Context;

import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.TransportState;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class MediaControlMap extends ConcurrentHashMap<UnsignedIntegerFourBytes, DefMediaControl> implements Serializable
{
    private ILogger mLogger = new DefaultLoggerImpl(this);

    public MediaControlMap(Context context, int numberOfPlayers, LastChange avTransportLastChange, LastChange renderLastChange)
    {
        super(numberOfPlayers);

        for (int i = 0; i < numberOfPlayers; i++)
        {
            DefMediaControl player = new DefMediaControl(context, new UnsignedIntegerFourBytes(i), avTransportLastChange, renderLastChange)
            {
                @Override
                public void transportStateChanged(TransportState newState)
                {
                    super.transportStateChanged(newState);

                    if (newState.equals(TransportState.PLAYING))
                    {
                        onPlayerPlay(this);
                    }
                    else if (newState.equals(TransportState.STOPPED))
                    {
                        onPlayerStop(this);
                    }
                    else if (newState.equals(TransportState.PAUSED_PLAYBACK))
                    {
                        onPlayerPaused(this);
                    }
                }

            };

            put(player.getInstanceId(), player);
        }
    }

    private void onPlayerPlay(DefMediaControl player)
    {
        mLogger.d("onPlayerPlay: " + player.getInstanceId());
    }

    private void onPlayerStop(DefMediaControl player)
    {
        mLogger.d("onPlayerStop: " + player.getInstanceId());
    }

    private void onPlayerPaused(DefMediaControl player)
    {
        mLogger.d("onPlayerPaused: " + player.getInstanceId());
    }
}
