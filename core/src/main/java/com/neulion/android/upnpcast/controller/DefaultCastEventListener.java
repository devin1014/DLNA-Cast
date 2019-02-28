package com.neulion.android.upnpcast.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.neulion.android.upnpcast.device.CastDevice;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

/**
 */
public class DefaultCastEventListener implements ICastEventListener
{
    @Override
    public void onCast(CastObject castObject)
    {
    }

    @Override
    public void onStart()
    {
    }

    @Override
    public void onPause()
    {
    }

    @Override
    public void onStop()
    {
    }

    @Override
    public void onSeekTo(long position)
    {
    }

    @Override
    public void onError(String errorMsg)
    {
    }

    @Override
    public void onVolume(int volume)
    {
    }

    @Override
    public void onBrightness(int brightness)
    {
    }

    @Override
    public void onUpdatePositionInfo(PositionInfo positionInfo)
    {
    }

    @Override
    public void onConnecting(@NonNull CastDevice castDevice)
    {
    }

    @Override
    public void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume)
    {
    }

    @Override
    public void onDisconnect()
    {
    }
}
