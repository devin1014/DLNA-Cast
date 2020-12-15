package com.android.cast.dlna.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.cast.dlna.device.CastDevice;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class CastEventListenerListWrapper implements ICastEventListener
{
    private List<ICastEventListener> mCastListenerList;

    public CastEventListenerListWrapper(List<ICastEventListener> list)
    {
        mCastListenerList = list != null ? list : new ArrayList<ICastEventListener>();
    }

    @Override
    public void onConnecting(@NonNull CastDevice castDevice)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onConnecting(castDevice);
        }
    }

    @Override
    public void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onConnected(castDevice, transportInfo, mediaInfo, volume);
        }
    }

    @Override
    public void onDisconnect()
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onDisconnect();
        }
    }

    @Override
    public void onCast(CastObject castObject)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onCast(castObject);
        }
    }

    @Override
    public void onStart()
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onStart();
        }
    }

    @Override
    public void onPause()
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onPause();
        }
    }

    @Override
    public void onStop()
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onStop();
        }
    }

    @Override
    public void onSeekTo(long position)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onSeekTo(position);
        }
    }

    @Override
    public void onError(String errorMsg)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onError(errorMsg);
        }
    }

    @Override
    public void onVolume(int volume)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onVolume(volume);
        }
    }

    @Override
    public void onBrightness(int brightness)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onBrightness(brightness);
        }
    }

    @Override
    public void onUpdatePositionInfo(PositionInfo positionInfo)
    {
        for (ICastEventListener listener : mCastListenerList)
        {
            listener.onUpdatePositionInfo(positionInfo);
        }
    }
}
