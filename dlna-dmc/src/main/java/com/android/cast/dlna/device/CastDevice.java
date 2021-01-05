package com.android.cast.dlna.device;

import androidx.annotation.NonNull;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

/**
 *
 */
public class CastDevice {
    private final Device<?, ?, ?> mDevice;

    public CastDevice(Device<?, ?, ?> device) {
        mDevice = device;
        if (device == null) throw new IllegalArgumentException("device can not be NULL!");
    }

    public Device<?, ?, ?> getDevice() {
        return mDevice;
    }

    @NonNull
    public String getId() {
        return mDevice.getIdentity().getUdn().getIdentifierString();
    }

    public String getName() {
        return mDevice.getDetails().getFriendlyName();
    }

    public String getDescription() {
        return mDevice.getDetails().getManufacturerDetails().getManufacturer();
    }

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
