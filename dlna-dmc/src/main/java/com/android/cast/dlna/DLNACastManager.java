package com.android.cast.dlna;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.android.cast.dlna.control.ControlImpl;
import com.android.cast.dlna.control.IConnect;
import com.android.cast.dlna.controller.CastControlImp;
import com.android.cast.dlna.controller.CastObject;
import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.ILogger;
import com.android.cast.dlna.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public final class DLNACastManager implements IDLNACast, OnDeviceRegistryListener {

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

    private DeviceType mSearchDeviceType;
    private CastControlImp mCastControlImp;

    private DLNACastManager() {
    }

    @Override
    public void bindCastService(@NonNull Context context) {
        if (context instanceof Application || context instanceof Activity) {
            context.bindService(new Intent(context, DLNACastService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        } else {
            mLogger.e("bindCastService only support Application or Activity implementation.");
        }
    }

    @Override
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
            mDLNACastService = (AndroidUpnpService) iBinder;
            mLogger.i("-------------------------------------------------------------------");
            mLogger.i(String.format("[%s] onServiceConnected, %s@%s", componentName.getShortClassName(), iBinder.getClass().getName(), iBinder.hashCode()));
            mLogger.i(String.format("[UpnpService]: %s@%s", mDLNACastService.get().getClass().getName(), mDLNACastService.get().hashCode()));
            mLogger.i(String.format("[Registry]: %s@%s", mDLNACastService.getRegistry().getClass().getName(), mDLNACastService.getRegistry().hashCode()));
            mLogger.i(String.format("[ControlPoint]: %s@%s", mDLNACastService.getControlPoint().getClass().getName(), mDLNACastService.getControlPoint().hashCode()));
            mLogger.i("-------------------------------------------------------------------");
            // add registry listener
            Collection<RegistryListener> collection = mDLNACastService.getRegistry().getListeners();

            if (collection == null || !collection.contains(mDeviceRegistryImpl)) {
                mDLNACastService.getRegistry().addListener(mDeviceRegistryImpl);
            }

            // Now add all devices to the list we already know about
            for (Device<?, ?, ?> device : mDLNACastService.getRegistry().getDevices()) {
                if (device instanceof RemoteDevice) {
                    mDeviceRegistryImpl.remoteDeviceAdded(mDLNACastService.getRegistry(), (RemoteDevice) device);
                } else if (device instanceof LocalDevice) {
                    mDeviceRegistryImpl.localDeviceAdded(mDLNACastService.getRegistry(), (LocalDevice) device);
                } else {
                    mDeviceRegistryImpl.deviceAdded(mDLNACastService.getRegistry(), device);
                }
            }

            if (mCastControlImp != null) {
                mCastControlImp.bindNLUpnpCastService(mDLNACastService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mLogger.w(String.format("[%s] onServiceDisconnected", componentName != null ? componentName.getShortClassName() : "NULL"));

            // clear registry listener
            if (mDLNACastService != null) {
                Collection<RegistryListener> collection = mDLNACastService.getRegistry().getListeners();

                if (collection != null && collection.contains(mDeviceRegistryImpl)) {
                    mDLNACastService.getRegistry().removeListener(mDeviceRegistryImpl);
                }
            }

            if (mCastControlImp != null) {
                mCastControlImp.unbindNLUpnpCastService();
            }

            mDLNACastService = null;
        }

        @Override
        public void onBindingDied(ComponentName componentName) {
            mLogger.e(String.format("[%s] onBindingDied", componentName.getClassName()));
        }
    };

    // -----------------------------------------------------------------------------------------
    // ---- register device listener
    // -----------------------------------------------------------------------------------------
    private final byte[] mLock = new byte[0];
    private final List<OnDeviceRegistryListener> mRegisterDeviceListenerList = new ArrayList<>();

    public void addRegistryDeviceListener(OnDeviceRegistryListener listener) {
        if (mDLNACastService != null) {
            @SuppressWarnings("rawtypes") Collection<Device> devices;

            if (mSearchDeviceType == null) {
                devices = mDLNACastService.getRegistry().getDevices();
            } else {
                devices = mDLNACastService.getRegistry().getDevices(mSearchDeviceType);
            }

            if (devices != null) {
                for (Device<?, ?, ?> device : devices) {
                    if (device instanceof RemoteDevice) {
                        mDeviceRegistryImpl.remoteDeviceAdded(mDLNACastService.getRegistry(), (RemoteDevice) device);
                    } else if (device instanceof LocalDevice) {
                        mDeviceRegistryImpl.localDeviceAdded(mDLNACastService.getRegistry(), (LocalDevice) device);
                    } else {
                        mDeviceRegistryImpl.deviceAdded(mDLNACastService.getRegistry(), device);
                    }
                }
            }
        }

        synchronized (mLock) {
            if (!mRegisterDeviceListenerList.contains(listener)) {
                mRegisterDeviceListenerList.add(listener);
            }
        }
    }

    public void removeRegistryListener(OnDeviceRegistryListener listener) {
        synchronized (mLock) {
            mRegisterDeviceListenerList.remove(listener);
        }
    }

    @Override
    public void onDeviceAdded(CastDevice device) {
        if (checkDeviceType(device.getDevice())) {
            synchronized (mLock) {
                for (OnDeviceRegistryListener listener : mRegisterDeviceListenerList) {
                    listener.onDeviceAdded(device);
                }
            }
            if (mCastControlImp != null) {
                mCastControlImp.onDeviceAdded(device);
            }
        }
    }

    @Override
    public void onDeviceUpdated(CastDevice device) {
        if (checkDeviceType(device.getDevice())) {
            synchronized (mLock) {
                for (OnDeviceRegistryListener listener : mRegisterDeviceListenerList) {
                    listener.onDeviceUpdated(device);
                }
            }
            if (mCastControlImp != null) {
                mCastControlImp.onDeviceUpdated(device);
            }
        }
    }

    @Override
    public void onDeviceRemoved(CastDevice device) {
        if (checkDeviceType(device.getDevice())) {
            synchronized (mLock) {
                for (OnDeviceRegistryListener listener : mRegisterDeviceListenerList) {
                    listener.onDeviceRemoved(device);
                }
            }
            if (mCastControlImp != null) {
                mCastControlImp.onDeviceRemoved(device);
            }
        }
    }

    private boolean checkDeviceType(Device<?, ?, ?> device) {
        return mSearchDeviceType == null || mSearchDeviceType.equals(device.getType());
    }

    // -----------------------------------------------------------------------------------------
    // ---- search
    // -----------------------------------------------------------------------------------------
    @Override
    public void search(DeviceType type, int maxSeconds) {
        mSearchDeviceType = type;

        if (mDLNACastService != null) {
            UpnpHeader<?> header = type == null ? new STAllHeader() : new UDADeviceTypeHeader(type);
            mDLNACastService.get().getControlPoint().search(header, maxSeconds);
        }
    }

    @Override
    public void clear() {
        if (mDLNACastService != null) {
            mDLNACastService.get().getRegistry().removeAllRemoteDevices();
        }
    }

    private ControlImpl mControlImpl;

    // -----------------------------------------------------------------------------------------
    // ---- connect
    // -----------------------------------------------------------------------------------------
    private final List<IConnect.IConnectCallback> mDeviceConnectionCallbackList = new ArrayList<>();

    public void addCastEventListener(@NonNull IConnect.IConnectCallback listener) {
        if (!mDeviceConnectionCallbackList.contains(listener)) {
            mDeviceConnectionCallbackList.add(listener);
        }
    }

    public void removeCastEventListener(@NonNull IConnect.IConnectCallback listener) {
        mDeviceConnectionCallbackList.remove(listener);
    }

    @Override
    public void connect(CastDevice castDevice) {
        // check device has been connected.
        if (mControlImpl != null && mControlImpl.isConnected(castDevice.getDevice())) return;

        if (mControlImpl == null) {
            mControlImpl = new ControlImpl(mDLNACastService, castDevice);
        }
        // always auto disconnected previous one if necessary
        mControlImpl.connect(null);
    }

    @Override
    public void disconnect() {
        if (mControlImpl != null) mControlImpl.disconnect();
    }

    @Override
    public boolean isConnected() {
        return mCastControlImp != null && mControlImpl.isConnected(null);
    }

    @Override
    public CastDevice getCastDevice() {
        return mCastControlImp != null ? mCastControlImp.getCastDevice() : null;
    }

    // -----------------------------------------------------------------------------------------
    // ---- cast
    // -----------------------------------------------------------------------------------------
    @Override
    public void cast(CastObject castObject) {
        if (mControlImpl != null) {
            mControlImpl.cast(castObject);
        }
    }

    @Override
    public void start() {
        if (mControlImpl != null) {
            mControlImpl.play();
        }
    }

    @Override
    public void pause() {
        if (mControlImpl != null) {
            mControlImpl.pause();
        }
    }

    @Override
    public void stop() {
        if (mControlImpl != null) {
            mControlImpl.stop();
        }
    }

    @Override
    public void seekTo(long position) {
        if (mControlImpl != null) {
            mControlImpl.seekTo(position);
        }
    }

    @Override
    public void setVolume(int percent) {
        if (mCastControlImp != null) {
            mCastControlImp.setVolume(percent);
        }
    }

    @Override
    public void setMute(boolean mute) {
        if (mControlImpl != null) {
            mControlImpl.setMute(mute);
        }
    }

    @Override
    public void setBrightness(int percent) {
        if (mControlImpl != null) {
            mControlImpl.setBrightness(percent);
        }
    }

    @Override
    public int getCastStatus() {
        if (mCastControlImp != null) {
            return mCastControlImp.getCastStatus();
        }

        return CastControlImp.IDLE;
    }

    @Override
    public PositionInfo getPosition() {
        if (mCastControlImp != null) {
            return mCastControlImp.getPosition();
        }

        return null;
    }

    @Override
    public MediaInfo getMedia() {
        if (mCastControlImp != null) {
            return mCastControlImp.getMedia();
        }

        return null;
    }

}
