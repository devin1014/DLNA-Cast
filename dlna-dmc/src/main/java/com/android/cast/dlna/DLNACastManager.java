package com.android.cast.dlna;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.android.cast.dlna.ILogger.DefaultLoggerImpl;
import com.android.cast.dlna.control.ControlImpl;
import com.android.cast.dlna.control.ICastInterface;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public final class DLNACastManager implements ICastInterface.IControl, ICastInterface.ICast, OnDeviceRegistryListener {

    public static final DeviceType DEVICE_TYPE_DMR = new UDADeviceType("MediaRenderer");
    public static final ServiceType SERVICE_AV_TRANSPORT = new UDAServiceType("AVTransport");
    public static final ServiceType SERVICE_RENDERING_CONTROL = new UDAServiceType("RenderingControl");
    public static final ServiceType SERVICE_CONNECTION_MANAGER = new UDAServiceType("ConnectionManager");

    private static class Holder {
        private static final DLNACastManager INSTANCE = new DLNACastManager();
    }

    public static DLNACastManager getInstance() {
        return Holder.INSTANCE;
    }

    private AndroidUpnpService mDLNACastService;
    private final ILogger mLogger = new DefaultLoggerImpl(this);
    private final DeviceRegistryImpl mDeviceRegistryImpl = new DeviceRegistryImpl(this);
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private DeviceType mSearchDeviceType;
    private ControlImpl mControlImpl;

    private DLNACastManager() {
    }

    public void bindCastService(@NonNull Context context) {
        if (context instanceof Application || context instanceof Activity) {
            context.bindService(new Intent(context, DLNACastService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        } else {
            mLogger.e("bindCastService only support Application or Activity implementation.");
        }
    }

    public void unbindCastService(@NonNull Context context) {
        if (context instanceof Application || context instanceof Activity) {
            context.unbindService(mServiceConnection);
        } else {
            mLogger.e("bindCastService only support Application or Activity implementation.");
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AndroidUpnpService upnpService = (AndroidUpnpService) iBinder;
            if (mDLNACastService != upnpService) {
                mDLNACastService = upnpService;
                Utils.logServiceConnected(mLogger, upnpService, componentName, iBinder);
                Registry registry = upnpService.getRegistry();
                // add registry listener
                Collection<RegistryListener> collection = registry.getListeners();
                if (collection == null || !collection.contains(mDeviceRegistryImpl)) {
                    registry.addListener(mDeviceRegistryImpl);
                }
                // Now add all devices to the list we already know about
                mDeviceRegistryImpl.setDevices(upnpService.getRegistry().getDevices());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mLogger.w(String.format("[%s] onServiceDisconnected", componentName != null ? componentName.getShortClassName() : "NULL"));
            removeRegistryListener();
        }

        @Override
        public void onBindingDied(ComponentName componentName) {
            mLogger.e(String.format("[%s] onBindingDied", componentName.getClassName()));
            removeRegistryListener();
        }

        private void removeRegistryListener() {
            if (mDLNACastService != null) {
                mDLNACastService.getRegistry().removeListener(mDeviceRegistryImpl);
            }
            mDLNACastService = null;
        }
    };

    // -----------------------------------------------------------------------------------------
    // ---- register or unregister device listener
    // -----------------------------------------------------------------------------------------
    private final byte[] mLock = new byte[0];
    private final List<OnDeviceRegistryListener> mRegisterDeviceListeners = new ArrayList<>();

    public void registerDeviceListener(OnDeviceRegistryListener listener) {
        if (listener == null) return;
        if (mDLNACastService != null) {
            @SuppressWarnings("rawtypes") Collection<Device> devices;

            if (mSearchDeviceType == null) {
                devices = mDLNACastService.getRegistry().getDevices();
            } else {
                devices = mDLNACastService.getRegistry().getDevices(mSearchDeviceType);
            }

            // if some devices has been found, notify first.
            if (devices != null && devices.size() > 0) {
                exeActionInUIThread(() -> {
                    for (Device<?, ?, ?> device : devices) listener.onDeviceAdded(device);
                });
            }
        }

        synchronized (mLock) {
            if (!mRegisterDeviceListeners.contains(listener)) {
                mRegisterDeviceListeners.add(listener);
            }
        }
    }

    public void unregisterListener(OnDeviceRegistryListener listener) {
        synchronized (mLock) {
            mRegisterDeviceListeners.remove(listener);
        }
    }

    @Override
    public void onDeviceAdded(Device<?, ?, ?> device) {
        if (checkDeviceType(device)) {
            synchronized (mLock) {
                for (OnDeviceRegistryListener listener : mRegisterDeviceListeners) listener.onDeviceAdded(device);
            }
        }
    }

    @Override
    public void onDeviceUpdated(Device<?, ?, ?> device) {
        if (checkDeviceType(device)) {
            synchronized (mLock) {
                for (OnDeviceRegistryListener listener : mRegisterDeviceListeners) listener.onDeviceUpdated(device);
            }
        }
    }

    @Override
    public void onDeviceRemoved(Device<?, ?, ?> device) {
        if (checkDeviceType(device)) {
            // if this device is casting, disconnect first!
            if (mControlImpl != null && mControlImpl.isCasting(device)) {
                mControlImpl.release();
            }
            synchronized (mLock) {
                for (OnDeviceRegistryListener listener : mRegisterDeviceListeners) listener.onDeviceRemoved(device);
            }
        }
    }

    private boolean checkDeviceType(Device<?, ?, ?> device) {
        return mSearchDeviceType == null || mSearchDeviceType.equals(device.getType());
    }

    // -----------------------------------------------------------------------------------------
    // ---- search
    // -----------------------------------------------------------------------------------------
    public void search(DeviceType type, int maxSeconds) {
        mSearchDeviceType = type;

        if (mDLNACastService != null) {
            UpnpService upnpService = mDLNACastService.get();
            //TODO: clear all devices first? check!!!
            upnpService.getRegistry().removeAllRemoteDevices();
            upnpService.getControlPoint().search(type == null ? new STAllHeader() : new UDADeviceTypeHeader(type), maxSeconds);
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- cast
    // -----------------------------------------------------------------------------------------
    @Override
    public void cast(Device<?, ?, ?> device, CastObject object) {
        // check device has been connected.
        if (mControlImpl != null) mControlImpl.stop();
        //FIXME: cast same video should not stop and restart!
        mControlImpl = new ControlImpl(mDLNACastService, device, object);
        //mControlImpl.connect();
    }

    @Override
    public void play() {
        if (mControlImpl != null) mControlImpl.play();
    }

    @Override
    public void pause() {
        if (mControlImpl != null) mControlImpl.pause();
    }

    @Override
    public boolean isCasting(Device<?, ?, ?> device) {
        return mControlImpl != null && mControlImpl.isCasting(device);
    }

    @Override
    public void stop() {
        if (mControlImpl != null) mControlImpl.stop();
    }

    @Override
    public void seekTo(long position) {
        if (mControlImpl != null) mControlImpl.seekTo(position);
    }

    @Override
    public void setVolume(int percent) {
        if (mControlImpl != null) mControlImpl.setVolume(percent);
    }

    @Override
    public void setMute(boolean mute) {
        if (mControlImpl != null) mControlImpl.setMute(mute);
    }

    @Override
    public void setBrightness(int percent) {
        if (mControlImpl != null) mControlImpl.setBrightness(percent);
    }

    private void exeActionInUIThread(Runnable action) {
        if (action != null) {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                mMainHandler.post(action);
            } else {
                action.run();
            }
        }
    }

}
