package com.neulion.android.upnpcast;

import com.neulion.android.upnpcast.controller.ICastControl;

import org.fourthline.cling.model.types.DeviceType;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-06-29
 * Time: 18:51
 */
public interface IUpnpCast extends ICastControl
{
    int DEFAULT_MAX_SECONDS = 60;

    void search(DeviceType type);

    void search(DeviceType type, int maxSeconds);

    void clear();
}
