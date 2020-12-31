package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.device.CastDevice;

public interface IConnect {
    void connect(@NonNull CastDevice device, @NonNull IConnectCallback callback);

    void disconnect();

    boolean isConnected();

    interface IConnectCallback {
        void onDeviceConnected(CastDevice device);

        void onDeviceDisconnected(CastDevice device, String errMsg);
    }
}
