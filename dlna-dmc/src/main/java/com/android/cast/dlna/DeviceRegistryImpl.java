package com.android.cast.dlna;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.ILogger;
import com.android.cast.dlna.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

/**
 *
 */
final class DeviceRegistryImpl extends DefaultRegistryListener {

    private final OnDeviceRegistryListener mOnDeviceRegistryListener;
    private final ILogger mLogger = new DefaultLoggerImpl(this);
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public DeviceRegistryImpl(@NonNull OnDeviceRegistryListener listener) {
        mOnDeviceRegistryListener = listener;
    }

    //Discovery performance optimization for very slow Android devices!
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        mLogger.i(String.format("[%s] discovery started >>>>", device.getDetails().getFriendlyName()));
    }

    //End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz)
    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        mLogger.e(String.format("[%s] discovery failed <<<<", device.getDetails().getFriendlyName()));
        mLogger.e(ex.toString());
    }

    // remote device
    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        mLogger.i("remoteDeviceAdded: " + DeviceUtil.parseDeviceInfo(device));
        mLogger.i(DeviceUtil.parseDeviceService(device));
        notifyDeviceAdd(device);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        // mLogger.d("remoteDeviceUpdated: " + DeviceUtil.parseDeviceInfo(device));
        // notifyDeviceUpdate(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        mLogger.w("remoteDeviceRemoved: " + DeviceUtil.parseDeviceInfo(device));
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
        mHandler.post(() -> mOnDeviceRegistryListener.onDeviceAdded(new CastDevice(device)));
    }

    @SuppressWarnings("unused")
    private void notifyDeviceUpdate(final Device<?, ?, ?> device) {
        mHandler.post(() -> mOnDeviceRegistryListener.onDeviceUpdated(new CastDevice(device)));
    }

    private void notifyDeviceRemove(final Device<?, ?, ?> device) {
        mHandler.post(() -> mOnDeviceRegistryListener.onDeviceRemoved(new CastDevice(device)));
    }
}
