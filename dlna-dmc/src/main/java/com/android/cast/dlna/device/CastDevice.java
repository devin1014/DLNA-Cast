package com.android.cast.dlna.device;

import androidx.annotation.NonNull;

import org.fourthline.cling.model.meta.Device;

/**
 *
 */
public class CastDevice implements IDLNACastDevice<Device> {
    private final Device mDevice;

    public CastDevice(Device device) {
        mDevice = device;

        if (device == null) {
            throw new IllegalArgumentException("device can not be NULL!");
        }
    }

    @Override
    public Device getDevice() {
        return mDevice;
    }

    @NonNull
    @Override
    public String getId() {
        return mDevice.getIdentity().getUdn().getIdentifierString();
    }

    @Override
    public String getName() {
        return mDevice.getDetails().getFriendlyName();
    }

    @Override
    public String getDescription() {
        return String.format("[%s]", mDevice.getIdentity().getUdn().toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CastDevice) {
            return getId().equals(((CastDevice) obj).getId());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }
}
