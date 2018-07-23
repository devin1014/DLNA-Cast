package com.neulion.android.upnpcast.util;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-02
 * Time: 17:26
 */
public class DeviceUtil
{
    public static String parseDevice(RemoteDevice remoteDevice)
    {
        return String.format("[%s@%s] ", remoteDevice.getDetails().getFriendlyName(), Integer.toHexString(remoteDevice.hashCode())) +
                String.format("[%s] ", remoteDevice.getType().getType()) +
                String.format("[%s] ", remoteDevice.getIdentity().getUdn()) +
                String.format("[%s] ", remoteDevice.getIdentity().getDescriptorURL());
    }

    public static String parseDevice(LocalDevice localDevice)
    {
        return String.format("[%s@%s] ", localDevice.getDetails().getFriendlyName(), Integer.toHexString(localDevice.hashCode())) +
                String.format("[%s] ", localDevice.getType().getType()) +
                String.format("[%s] ", localDevice.getIdentity().getUdn());
    }
}
