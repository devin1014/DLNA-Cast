package com.android.cast.dlna.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.cast.dlna.device.CastDevice;

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
