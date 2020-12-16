package com.android.cast.dlna;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.DeviceUtil;
import com.android.cast.dlna.util.ILogger;
import com.android.cast.dlna.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DeviceRegistryListener extends DefaultRegistryListener {
    private final ILogger mLogger = new DefaultLoggerImpl(this);
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<OnRegistryDeviceListener> mOnRegistryDeviceListener = new ArrayList<>();
    private final byte[] mLock = new byte[0];

    //Discovery performance optimization for very slow Android devices!
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        mLogger.i("DeviceDiscovery:" + DeviceUtil.parseDevice(device));
    }

    //End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz)
    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        mLogger.e("DeviceDiscoveryFailed:" + DeviceUtil.parseDevice(device));
    }

    @Override
    public void deviceAdded(Registry registry, final Device device) {
        mLogger.i("++ deviceAdded:" + DeviceUtil.parseDevice(device));

        mHandler.post(() -> {
            synchronized (mLock) {
                for (OnRegistryDeviceListener listener : mOnRegistryDeviceListener) {
                    listener.onDeviceAdded(new CastDevice(device));
                }
            }
        });
    }

    @Override
    public void deviceRemoved(Registry registry, final Device device) {
        mLogger.w("-- deviceRemoved:" + DeviceUtil.parseDevice(device));

        mHandler.post(() -> {
            synchronized (mLock) {
                for (OnRegistryDeviceListener listener : mOnRegistryDeviceListener) {
                    listener.onDeviceRemoved(new CastDevice(device));
                }
            }
        });
    }

    public void addRegistryDeviceListener(@NonNull OnRegistryDeviceListener listener) {
        synchronized (mLock) {
            if (!mOnRegistryDeviceListener.contains(listener)) {
                mOnRegistryDeviceListener.add(listener);
            }
        }
    }

    public void removeRegistryListener(@NonNull OnRegistryDeviceListener listener) {
        synchronized (mLock) {
            mOnRegistryDeviceListener.remove(listener);
        }
    }

    public interface OnRegistryDeviceListener {
        void onDeviceAdded(CastDevice device);

        void onDeviceRemoved(CastDevice device);
    }
}
