package com.android.cast.dlna;

import com.android.cast.dlna.device.CastDevice;

/**
 * this listener call in UI thread.
 */
public interface OnDeviceRegistryListener {
    void onDeviceAdded(CastDevice device);

    void onDeviceUpdated(CastDevice device);

    void onDeviceRemoved(CastDevice device);
}
