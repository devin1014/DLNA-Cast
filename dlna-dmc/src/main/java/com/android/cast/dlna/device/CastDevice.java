package com.android.cast.dlna.device;

import androidx.annotation.NonNull;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

/**
 *
 */
public class CastDevice implements IDLNACastDevice<Device<?, ?, ?>> {
    private final Device<?, ?, ?> mDevice;

    public CastDevice(Device<?, ?, ?> device) {
        mDevice = device;
        if (device == null) throw new IllegalArgumentException("device can not be NULL!");
    }

    @Override
    public Device<?, ?, ?> getDevice() {
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
        return mDevice.getDetails().getManufacturerDetails().getManufacturer();
    }

    @Override
    public boolean supportAction(String name) {
        if (mDevice.getServices() != null) {
            for (Service<?, ?> service : mDevice.getServices()) {
                if (service.getAction(name) != null) return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CastDevice) return getId().equals(((CastDevice) obj).getId());
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }
}
