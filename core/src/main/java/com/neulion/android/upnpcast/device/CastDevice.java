package com.neulion.android.upnpcast.device;

import android.support.annotation.NonNull;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.RemoteDevice;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-02
 * Time: 16:26
 */
public class CastDevice implements ICastDevice<Device>
{
    private final Device mDevice;

    public CastDevice(Device device)
    {
        mDevice = device;
    }

    @Override
    public Device getDevice()
    {
        return mDevice;
    }

    @NonNull
    @Override
    public String getId()
    {
        return mDevice.getIdentity().getUdn().getIdentifierString();
    }

    @Override
    public String getName()
    {
        return mDevice.getDetails().getFriendlyName();
    }

    @Override
    public String getDescription()
    {
        String result = String.format("[%s]", mDevice.getIdentity().getUdn().toString());

        if (mDevice instanceof RemoteDevice)
        {
            result += "\n" + String.format("[%s]", mDevice.getDetails().getBaseURL());
        }

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof CastDevice)
        {
            return getId().equals(((CastDevice) obj).getId());
        }

        return super.equals(obj);
    }
}
