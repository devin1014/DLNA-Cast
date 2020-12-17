package com.android.cast.dlna.device;

import androidx.annotation.NonNull;

import org.fourthline.cling.model.meta.Device;

/**
 *
 */
public interface IDLNACastDevice<T extends Device<?, ?, ?>> {
    T getDevice();

    @NonNull
    String getId();

    String getName();

    String getDescription();
}
