package com.liuwei.android.upnpcast;

import com.liuwei.android.upnpcast.controller.ICastControl;

import org.fourthline.cling.model.types.DeviceType;

/**
 */
public interface IUpnpCast extends ICastControl
{
    int DEFAULT_MAX_SECONDS = 60;

    void search();

    void search(DeviceType type, int maxSeconds);

    void clear();
}
