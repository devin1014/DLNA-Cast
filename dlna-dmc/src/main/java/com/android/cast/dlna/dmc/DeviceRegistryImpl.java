package com.android.cast.dlna.dmc;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 */
final class DeviceRegistryImpl extends DefaultRegistryListener {

    private final OnDeviceRegistryListener mOnDeviceRegistryListener;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private volatile boolean mIgnoreUpdate = true;

    public DeviceRegistryImpl(@NonNull OnDeviceRegistryListener listener) {
        mOnDeviceRegistryListener = listener;
        setIgnoreUpdateEvent(true);
    }

    public void setIgnoreUpdateEvent(boolean ignoreUpdate) {
        mIgnoreUpdate = ignoreUpdate;
    }

    public void setDevices(@SuppressWarnings("rawtypes") Collection<Device> collection) {
        if (collection != null && collection.size() > 0) {
            for (Device<?, ?, ?> device : collection) {
                notifyDeviceAdd(device);
            }
        }
    }

    // Discovery performance optimization for very slow Android devices!
    // This function will called early than 'remoteDeviceAdded',but the device services maybe not entirely.
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        Logger.i(String.format("[%s] discovery started...", device.getDetails().getFriendlyName()));
    }

    //End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz)
    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        Logger.e(String.format("[%s] discovery failed...", device.getDetails().getFriendlyName()));
        Logger.e(ex.toString());
    }

    // remote device
    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        Logger.i("remoteDeviceAdded: " + parseDeviceInfo(device));
        Logger.i(parseDeviceService(device));
        notifyDeviceAdd(device);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        if (!mIgnoreUpdate) {
            Logger.d("remoteDeviceUpdated: " + parseDeviceInfo(device));
            notifyDeviceUpdate(device);
        }
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        Logger.w("remoteDeviceRemoved: " + parseDeviceInfo(device));
        notifyDeviceRemove(device);
    }

    // local device
    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        super.localDeviceAdded(registry, device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        super.localDeviceRemoved(registry, device);
    }

    private void notifyDeviceAdd(final Device<?, ?, ?> device) {
        mHandler.post(() -> mOnDeviceRegistryListener.onDeviceAdded(device));
    }

    private void notifyDeviceUpdate(final Device<?, ?, ?> device) {
        mHandler.post(() -> mOnDeviceRegistryListener.onDeviceUpdated(device));
    }

    private void notifyDeviceRemove(final Device<?, ?, ?> device) {
        mHandler.post(() -> mOnDeviceRegistryListener.onDeviceRemoved(device));
    }

    /**
     * @return device information like: [deviceType][name][manufacturer][udn]
     */
    private static String parseDeviceInfo(@NonNull RemoteDevice device) {
        return String.format("[%s][%s][%s][%s]",
                device.getType().getType(),
                device.getDetails().getFriendlyName(),
                device.getDetails().getManufacturerDetails().getManufacturer(),
                device.getIdentity().getUdn());
    }

    private static String parseDeviceService(@NonNull RemoteDevice device) {
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
