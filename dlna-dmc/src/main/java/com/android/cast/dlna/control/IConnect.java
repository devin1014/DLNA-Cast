package com.android.cast.dlna.control;

import com.android.cast.dlna.device.CastDevice;

public interface IConnect {
    IControl connect(CastDevice device);

    void disconnect();

    boolean isConnected();

    interface IConnectCallback {
        void onDeviceConnected(CastDevice device);

        void onDeviceDisconnected(CastDevice device);
    }
}
