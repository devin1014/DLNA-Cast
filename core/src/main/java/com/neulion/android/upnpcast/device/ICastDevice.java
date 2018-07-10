package com.neulion.android.upnpcast.device;

import android.support.annotation.NonNull;

import org.fourthline.cling.model.meta.Device;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-02
 * Time: 16:25
 */
public interface ICastDevice<T extends Device>
{
    T getDevice();

    @NonNull
    String getId();

    String getName();

    String getDescription();
}
