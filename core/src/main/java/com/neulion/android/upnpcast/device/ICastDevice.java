package com.neulion.android.upnpcast.device;

import android.support.annotation.NonNull;

import org.fourthline.cling.model.meta.Device;

/**
 */
public interface ICastDevice<T extends Device>
{
    T getDevice();

    @NonNull
    String getId();

    String getName();

    String getDescription();
}
