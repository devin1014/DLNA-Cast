package com.liuwei.android.upnpcast.controller;

import androidx.annotation.NonNull;

import com.liuwei.android.upnpcast.device.CastDevice;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

/**
 */
public class CastEventListenerWrapper implements ICastEventListener
{
    private ICastEventListener mListener;

    public CastEventListenerWrapper(ICastEventListener listener)
    {
        mListener = listener;
    }

    @Override
    public void onConnecting(@NonNull CastDevice castDevice)
    {
        if (mListener != null)
        {
            mListener.onConnecting(castDevice);
        }
    }

    @Override
    public void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, MediaInfo mediaInfo, int volume)
    {
        if (mListener != null)
        {
            mListener.onConnected(castDevice, transportInfo, mediaInfo, volume);
        }
    }

    @Override
    public void onDisconnect()
    {
        if (mListener != null)
        {
            mListener.onDisconnect();
        }
    }

    @Override
    public void onCast(CastObject castObject)
    {
        if (mListener != null)
        {
            mListener.onCast(castObject);
        }
    }

    @Override
    public void onStart()
    {
        if (mListener != null)
        {
            mListener.onStart();
        }
    }

    @Override
    public void onPause()
    {
        if (mListener != null)
        {
            mListener.onPause();
        }
    }

    @Override
    public void onStop()
    {
        if (mListener != null)
        {
            mListener.onStop();
        }
    }

    @Override
    public void onSeekTo(long position)
    {
        if (mListener != null)
        {
            mListener.onSeekTo(position);
        }
    }

    @Override
    public void onError(String errorMsg)
    {
        if (mListener != null)
        {
            mListener.onError(errorMsg);
        }
    }

    @Override
    public void onBrightness(int brightness)
    {
        if (mListener != null)
        {
            mListener.onBrightness(brightness);
        }
    }

    @Override
    public void onVolume(int volume)
    {
        if (mListener != null)
        {
            mListener.onVolume(volume);
        }
    }

    @Override
    public void onUpdatePositionInfo(PositionInfo positionInfo)
    {
        if (mListener != null)
        {
            mListener.onUpdatePositionInfo(positionInfo);
        }
    }
}
