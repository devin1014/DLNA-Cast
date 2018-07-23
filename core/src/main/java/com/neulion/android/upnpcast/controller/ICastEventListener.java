package com.neulion.android.upnpcast.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.neulion.android.upnpcast.device.CastDevice;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.TransportInfo;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-11
 * Time: 18:23
 */
public interface ICastEventListener extends ICastControlListener
{
    void onConnecting(@NonNull CastDevice castDevice);

    void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume);

    void onDisconnect();
}
