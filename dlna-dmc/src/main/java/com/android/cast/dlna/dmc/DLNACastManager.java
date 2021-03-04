package com.android.cast.dlna.dmc;

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
import androidx.annotation.Nullable;

import com.android.cast.dlna.core.ContentType;
import com.android.cast.dlna.core.ICast;
import com.android.cast.dlna.core.Utils;
import com.android.cast.dlna.dmc.control.ControlImpl;
import com.android.cast.dlna.dmc.control.ICastInterface;
import com.android.cast.dlna.dmc.control.IServiceAction;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 */
public final class DLNACastManager implements ICastInterface.IControl, ICastInterface.IGetInfo, OnDeviceRegistryListener {

    //public static final DeviceType DEVICE_TYPE_DMR = new UDADeviceType("MediaRenderer");
    public static final ServiceType SERVICE_AV_TRANSPORT = new UDAServiceType("AVTransport");
    public static final ServiceType SERVICE_RENDERING_CONTROL = new UDAServiceType("RenderingControl");
    public static final ServiceType SERVICE_CONNECTION_MANAGER = new UDAServiceType("ConnectionManager");
    public static final ServiceType SERVICE_CONTENT_DIRECTORY = new UDAServiceType("ContentDirectory");

    private static class Holder {
        private static final DLNACastManager INSTANCE = new DLNACastManager();
    }

    public static DLNACastManager getInstance() {
        return Holder.INSTANCE;
    }

    private AndroidUpnpService mDLNACastService;
    private final DeviceRegistryImpl mDeviceRegistryImpl = new DeviceRegistryImpl(this);
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, IServiceAction.IServiceActionCallback<?>> mActionEventCallbackMap = new LinkedHashMap<>();

    private DeviceType mSearchDeviceType;
    private ControlImpl mControlImpl;

    private DLNACastManager() {
    }

    public void enableLog() {
        java.util.logging.Logger.getLogger("org.fourthline.cling").setLevel(Level.FINEST);
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    public void bindCastService(@NonNull Context context) {
        if (context instanceof Application || context instanceof Activity) {
            context.bindService(new Intent(context, DLNACastService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        } else {
            Logger.e("bindCastService only support Application or Activity implementation.");
        }
    }

    public void unbindCastService(@NonNull Context context) {
        if (context instanceof Application || context instanceof Activity) {
            context.unbindService(mServiceConnection);
        } else {
            Logger.e("bindCastService only support Application or Activity implementation.");
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AndroidUpnpService upnpService = (AndroidUpnpService) iBinder;
            if (mDLNACastService != upnpService) {
                mDLNACastService = upnpService;
                Logger.i(String.format("[%s] connected %s", componentName.getShortClassName(), iBinder.getClass().getName()));
                Logger.i(String.format("[UpnpService]: %s@0x%s", upnpService.get().getClass().getName(), Utils.toHexString(upnpService.get().hashCode())));
                Logger.i(String.format("[Registry]: listener=%s, devices=%s", upnpService.getRegistry().getListeners().size(), upnpService.getRegistry().getDevices().size()));
                Registry registry = upnpService.getRegistry();
                // add registry listener
                Collection<RegistryListener> collection = registry.getListeners();
                if (collection == null || !collection.contains(mDeviceRegistryImpl)) {
                    registry.addListener(mDeviceRegistryImpl);
                }
                // Now add all devices to the list we already know about
                mDeviceRegistryImpl.setDevices(upnpService.getRegistry().getDevices());
            }
            if (_mediaServer != null) {
                mDLNACastService.getRegistry().addDevice(_mediaServer);
            }
            _mediaServer = null;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.w(String.format("[%s] onServiceDisconnected", componentName != null ? componentName.getShortClassName() : "NULL"));
            removeRegistryListener();
        }

        @Override
        public void onBindingDied(ComponentName componentName) {
            Logger.e(String.format("[%s] onBindingDied", componentName.getClassName()));
            removeRegistryListener();
        }

        private void removeRegistryListener() {
            if (mDLNACastService != null) {
                mDLNACastService.getRegistry().removeListener(mDeviceRegistryImpl);
            }
            mDLNACastService = null;
        }
    };

    @Nullable
    public AndroidUpnpService getService() {
        return mDLNACastService;
    }

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

    private void exeActionInUIThread(Runnable action) {
        if (action != null) {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                mMainHandler.post(action);
            } else {
                action.run();
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
                mControlImpl.stop();
            }
            mControlImpl = null;
            synchronized (mLock) {
                for (OnDeviceRegistryListener listener : mRegisterDeviceListeners) listener.onDeviceRemoved(device);
            }
        }
    }

    private boolean checkDeviceType(Device<?, ?, ?> device) {
        return mSearchDeviceType == null || mSearchDeviceType.equals(device.getType());
    }

    // -----------------------------------------------------------------------------------------
    // ---- MediaServer
    // -----------------------------------------------------------------------------------------
    private LocalDevice _mediaServer;

    public void addMediaServer(LocalDevice mediaServer) {
        if (mDLNACastService != null && mediaServer != null) {
            if (mDLNACastService.getRegistry().getDevice(mediaServer.getIdentity().getUdn(), true) == null) {
                mDLNACastService.getRegistry().addDevice(mediaServer);
            }
        } else {
            _mediaServer = mediaServer;
        }
    }

    public void removeMediaServer(LocalDevice mediaServer) {
        if (mDLNACastService != null && mediaServer != null) {
            mDLNACastService.getRegistry().removeDevice(mediaServer);
        } else {
            _mediaServer = null;
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- search
    // -----------------------------------------------------------------------------------------
    public void search(DeviceType type, int maxSeconds) {
        mSearchDeviceType = type;

        if (mDLNACastService != null) {
            UpnpService upnpService = mDLNACastService.get();
            //when search device, clear all founded first.
            upnpService.getRegistry().removeAllRemoteDevices();
            upnpService.getControlPoint().search(type == null ? new STAllHeader() : new UDADeviceTypeHeader(type), maxSeconds);
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- action
    // -----------------------------------------------------------------------------------------
    @Override
    public void cast(Device<?, ?, ?> device, ICast object) {

        // check device has been connected.
        if (mControlImpl != null) {
            // the device is casting! should not recast and syc control status.
            // the device is casting! should not recast and syc control status.
            // the device is casting! should not recast and syc control status.
            // if (mControlImpl.isCasting(device, object.getUri())) {
            //     syc status
            //     return;
            // }
            mControlImpl.stop();
        }
        mControlImpl = new ControlImpl(mDLNACastService.getControlPoint(), device, mActionEventCallbackMap, mSubscriptionListener);
        mControlImpl.cast(device, object);
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
    public boolean isCasting(Device<?, ?, ?> device, @Nullable String uri) {
        return mControlImpl != null && mControlImpl.isCasting(device, uri);
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

    // -----------------------------------------------------------------------------------------
    // ---- Callback
    // -----------------------------------------------------------------------------------------
    public void registerActionCallbacks(IServiceAction.IServiceActionCallback<?>... callbacks) {
        _innerRegisterActionCallback(callbacks);
    }

    public void unregisterActionCallbacks() {
        if (mActionEventCallbackMap.size() > 0) {
            mActionEventCallbackMap.clear();
        }
    }

    private void _innerRegisterActionCallback(IServiceAction.IServiceActionCallback<?>... callbacks) {
        if (callbacks != null && callbacks.length > 0) {
            for (IServiceAction.IServiceActionCallback<?> callback : callbacks) {
                if (callback instanceof ICastInterface.CastEventListener) {
                    mActionEventCallbackMap.put(IServiceAction.ServiceAction.CAST.name(), callback);
                } else if (callback instanceof ICastInterface.PlayEventListener) {
                    mActionEventCallbackMap.put(IServiceAction.ServiceAction.PLAY.name(), callback);
                } else if (callback instanceof ICastInterface.PauseEventListener) {
                    mActionEventCallbackMap.put(IServiceAction.ServiceAction.PAUSE.name(), callback);
                } else if (callback instanceof ICastInterface.StopEventListener) {
                    mActionEventCallbackMap.put(IServiceAction.ServiceAction.STOP.name(), callback);
                } else if (callback instanceof ICastInterface.SeekToEventListener) {
                    mActionEventCallbackMap.put(IServiceAction.ServiceAction.SEEK_TO.name(), callback);
                }
            }
        }
    }

    private ICastInterface.ISubscriptionListener mSubscriptionListener;

    public void registerSubscriptionListener(ICastInterface.ISubscriptionListener listener) {
        mSubscriptionListener = listener;
    }

    // -----------------------------------------------------------------------------------------
    // ---- query
    // -----------------------------------------------------------------------------------------
    public void getMediaInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<MediaInfo> listener) {
        new QueryRequest.MediaInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(mDLNACastService.getControlPoint(), listener);
    }

    public void getPositionInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<PositionInfo> listener) {
        new QueryRequest.PositionInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(mDLNACastService.getControlPoint(), listener);
    }

    public void getTransportInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<TransportInfo> listener) {
        new QueryRequest.TransportInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(mDLNACastService.getControlPoint(), listener);
    }

    public void getVolumeInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<Integer> listener) {
        new QueryRequest.VolumeInfoRequest(device.findService(SERVICE_RENDERING_CONTROL)).execute(mDLNACastService.getControlPoint(), listener);
    }

    public void getContent(Device<?, ?, ?> device, ContentType contentType, ICastInterface.GetInfoListener<DIDLContent> listener) {
        new QueryRequest.BrowseContentRequest(device.findService(SERVICE_CONTENT_DIRECTORY), contentType.id).execute(mDLNACastService.getControlPoint(), listener);
    }
}
