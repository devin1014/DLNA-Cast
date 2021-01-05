package com.android.cast.dlna;

import android.content.ComponentName;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.android.cast.dlna.util.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
final class Utils {
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

    public static void logServiceConnected(ILogger mLogger, AndroidUpnpService upnpService, ComponentName componentName, IBinder iBinder) {
        mLogger.i("-------------------------------------------------------------------");
        mLogger.i(String.format("[%s] onServiceConnected, %s@%s", componentName.getShortClassName(), iBinder.getClass().getName(), iBinder.hashCode()));
        mLogger.i(String.format("[UpnpService]: %s@%s", upnpService.get().getClass().getName(), upnpService.get().hashCode()));
        mLogger.i(String.format("[Registry]: %s@%s", upnpService.getRegistry().getClass().getName(), upnpService.getRegistry().hashCode()));
        mLogger.i(String.format("[ControlPoint]: %s@%s", upnpService.getControlPoint().getClass().getName(), upnpService.getControlPoint().hashCode()));
        mLogger.i("-------------------------------------------------------------------");
    }
}
