package com.android.cast.dlna;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.cast.dlna.controller.CastControlImp;
import com.android.cast.dlna.controller.CastEventListenerListWrapper;
import com.android.cast.dlna.controller.CastObject;
import com.android.cast.dlna.controller.ICastEventListener;
import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.service.DLNACastService;
import com.android.cast.dlna.service.DLNACastService.DLNACastBinder;
import com.android.cast.dlna.util.ILogger;
import com.android.cast.dlna.util.ILogger.DefaultLoggerImpl;

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

    private static class Holder {
        private static final DLNACastManager INSTANCE = new DLNACastManager();
    }

    public static DLNACastManager getInstance() {
        return Holder.INSTANCE;
    }

    private DLNACastService mDLNACastService;
    private final ILogger mLogger = new DefaultLoggerImpl(this);
    private final DeviceRegistryImpl mDeviceRegistryImpl = new DeviceRegistryImpl(this);
    private final List<ICastEventListener> mListeners = new ArrayList<>();

    private DeviceType mSearchDeviceType = DEVICE_TYPE_DMR;
    private CastControlImp mCastControlImp;

    private DLNACastManager() {
    }

    public void bindUpnpCastService(Activity activity) {
        if (activity != null) {
            activity.getApplication().bindService(new Intent(activity, DLNACastService.class), mDLNACastServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    public void unbindUpnpCastService(Activity activity) {
        if (activity != null) {
            activity.getApplication().unbindService(mDLNACastServiceConnection);
            mDLNACastServiceConnection.onServiceDisconnected(null);
        }
    }

    private final ServiceConnection mDLNACastServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mLogger.i(String.format("[%s] onServiceConnected", componentName.getShortClassName()));

            DLNACastService service = ((DLNACastBinder) iBinder).getService();

            mDLNACastService = service;

            // add registry listener
            Collection<RegistryListener> collection = service.getRegistry().getListeners();

            if (collection == null || !collection.contains(mDeviceRegistryImpl)) {
                service.getRegistry().addListener(mDeviceRegistryImpl);
            }

            // Now add all devices to the list we already know about
            for (Device<?, ?, ?> device : service.getRegistry().getDevices()) {
                if (device instanceof RemoteDevice) {
                    mDeviceRegistryImpl.remoteDeviceAdded(service.getRegistry(), (RemoteDevice) device);
                } else if (device instanceof LocalDevice) {
                    mDeviceRegistryImpl.localDeviceAdded(service.getRegistry(), (LocalDevice) device);
                } else {
                    mDeviceRegistryImpl.deviceAdded(service.getRegistry(), device);
                }
            }

            if (mCastControlImp != null) {
                mCastControlImp.bindNLUpnpCastService(service);
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

    public void addCastEventListener(ICastEventListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeCastEventListener(ICastEventListener listener) {
        mListeners.remove(listener);
    }

    // -----------------------------------------------------------------------------------------
    // ---- control
    // -----------------------------------------------------------------------------------------
    @Override
    public void search(DeviceType type, int maxSeconds) {
        mSearchDeviceType = type;

        UpnpHeader<?> header = type == null ? new STAllHeader() : new UDADeviceTypeHeader(type);

        if (mDLNACastService != null) {
            mDLNACastService.get().getControlPoint().search(header, maxSeconds);
        }
    }

    @Override
    public void clear() {
        if (mDLNACastService != null) {
            mDLNACastService.get().getRegistry().removeAllRemoteDevices();
        }
    }

    @Override
    public void connect(CastDevice castDevice) {
        if (mCastControlImp == null) {
            mCastControlImp = new CastControlImp(mDLNACastService, new CastEventListenerListWrapper(mListeners));
        }

        mCastControlImp.connect(castDevice);
    }

    @Override
    public void disconnect() {
        if (mCastControlImp != null) {
            mCastControlImp.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return mCastControlImp != null && mCastControlImp.isConnected();
    }

    @Override
    public void cast(CastObject castObject) {
        if (mCastControlImp != null) {
            mCastControlImp.cast(castObject);
        }
    }

    @Override
    public void start() {
        if (mCastControlImp != null) {
            mCastControlImp.start();
        }
    }

    @Override
    public void pause() {
        if (mCastControlImp != null) {
            mCastControlImp.pause();
        }
    }

    @Override
    public void stop() {
        if (mCastControlImp != null) {
            mCastControlImp.stop();
        }
    }

    @Override
    public void seekTo(long position) {
        if (mCastControlImp != null) {
            mCastControlImp.seekTo(position);
        }
    }

    @Override
    public void setVolume(int percent) {
        if (mCastControlImp != null) {
            mCastControlImp.setVolume(percent);
        }
    }

    @Override
    public void setBrightness(int percent) {
        if (mCastControlImp != null) {
            mCastControlImp.setBrightness(percent);
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
