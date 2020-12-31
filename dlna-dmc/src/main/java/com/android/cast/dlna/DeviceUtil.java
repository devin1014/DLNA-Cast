package com.android.cast.dlna;

import androidx.annotation.NonNull;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
                List<Action<?>> list = Arrays.asList(service.getActions());
                Collections.sort(list, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                for (Action<?> action : list) {
                    builder.append(action.getName()).append(", ");
                }
            }
        }
        return builder.toString();
    }
}
