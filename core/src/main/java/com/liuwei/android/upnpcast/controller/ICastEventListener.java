package com.liuwei.android.upnpcast.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.liuwei.android.upnpcast.device.CastDevice;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.TransportInfo;

/**
 */
public interface ICastEventListener extends ICastControlListener
{
    void onConnecting(@NonNull CastDevice castDevice);

    void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume);

    void onDisconnect();
}
