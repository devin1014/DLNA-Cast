package com.android.cast.dlna;

import androidx.annotation.NonNull;

import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;

/**
 *
 */
final class DeviceUtil {
    /**
     * @return device information like: [deviceType][name][manufacturer][udn]
     */
    public static String parseDeviceInfo(@NonNull RemoteDevice device) {
        return String.format("[%s][%s][%s][%s]",
                device.getType().getType(),
                device.getDetails().getFriendlyName(),
                device.getDetails().getManufacturerDetails().getManufacturer(),
                device.getIdentity().getUdn());
    }

    public static String parseDeviceService(@NonNull RemoteDevice device) {
        StringBuilder builder = new StringBuilder("service list:[");
        for (RemoteService services : device.getServices()) {
            builder.append(" ").append(services.getServiceType().getType());
        }
        builder.append(" ]");
        return builder.toString();
    }
}
