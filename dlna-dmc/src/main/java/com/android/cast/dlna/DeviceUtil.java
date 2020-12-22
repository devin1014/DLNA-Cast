package com.android.cast.dlna;

import androidx.annotation.NonNull;

import org.fourthline.cling.model.meta.Action;
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
        StringBuilder builder = new StringBuilder(device.getDetails().getFriendlyName());
        builder.append(":");
        for (RemoteService service : device.getServices()) {
            builder.append("\nservice:").append(service.getServiceType().getType());
            if (service.hasActions()) {
                builder.append("\nactions: ");
                for (Action<?> action : service.getActions()) {
                    builder.append(action.getName()).append(", ");
                }
            }
        }
        return builder.toString();
    }
}
