package com.liuwei.android.upnpcast.util;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

/**
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

    public static String parseDevice(Device device)
    {
        return String.format("[%s@%s] ", device.getDetails().getFriendlyName(), Integer.toHexString(device.hashCode())) +
                String.format("[%s] ", device.getType().getType()) +
                String.format("[%s] ", device.getIdentity().getUdn());
    }
}
