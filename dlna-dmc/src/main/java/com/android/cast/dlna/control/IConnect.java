package com.android.cast.dlna.control;

import androidx.annotation.Nullable;

import org.fourthline.cling.model.meta.Device;

public interface IConnect {
    void connect(@Nullable IConnectCallback callback);

    void disconnect();

    boolean isConnected(@Nullable Device<?, ?, ?> device);

    interface IConnectCallback {
        void onDeviceConnected(Device<?, ?, ?> device);

        void onDeviceDisconnected(Device<?, ?, ?> device, String errMsg);
    }
}
