package com.android.cast.dlna.control;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.cast.dlna.controller.CastObject;
import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class ControlImpl implements IConnect, IControl {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private final AndroidUpnpService mService;
    private final IServiceFactory mServiceFactory;
    private final Device<?, ?, ?> mDevice;

    public ControlImpl(@NonNull AndroidUpnpService upnpService, @NonNull CastDevice castDevice) {
        mService = upnpService;
        mDevice = castDevice.getDevice();
        mServiceFactory = new IServiceFactory.ServiceFactoryImpl(upnpService.getControlPoint(), castDevice.getDevice());
    }

    private volatile boolean mConnected = false;
    private SyncDataManager mSyncDataManager;

    @Override
    public void connect(@Nullable IConnectCallback callback) {
        disconnect();
        mSyncDataManager = new SyncDataManager();
        mSyncDataManager.sync(mService.getControlPoint(), mDevice, new SyncDataManager.SubscriptionCallback() {
            @Override
            public void onSubscriptionSuccess() {
                mConnected = true;
                if (callback != null) mHandler.post(() -> callback.onDeviceConnected(mDevice));
            }

            @Override
            public void onSubscriptionFailed(String msg) {
                mConnected = false;
                if (callback != null) mHandler.post(() -> callback.onDeviceDisconnected(mDevice, msg));
            }
        }, mTransportInfoListener, mMediaInfoListener, mPositionInfoListener, mVolumeInfoListener);
    }

    @Override
    public void disconnect() {
        mConnected = false;
        if (mSyncDataManager != null) mSyncDataManager.release();
    }

    @Override
    public boolean isConnected(@Nullable Device<?, ?, ?> device) {
        return mDevice != null && mConnected && (device == null || device.getIdentity().getUdn().equals(mDevice.getIdentity().getUdn()));
    }

    private final ICastInfoListener<TransportInfo> mTransportInfoListener = info -> {
        mLogger.i(String.format("TransportInfo: [%s] [%s] [%s]", info.getCurrentTransportStatus(), info.getCurrentTransportState(), info.getCurrentSpeed()));
    };

    private final ICastInfoListener<MediaInfo> mMediaInfoListener = info -> {
        mLogger.i(String.format("onMediaChanged: %s", info.getCurrentURI()));
    };

    private final ICastInfoListener<PositionInfo> mPositionInfoListener = info -> {
        mLogger.i(String.format("onPositionChanged: %s", info));
    };

    private final ICastInfoListener<Integer> mVolumeInfoListener = integer -> {
        mLogger.i(String.format("onVolumeChanged: %s", integer));
    };

    @Override
    public void cast(CastObject castObject) {
        mServiceFactory.getAvService().cast(null, castObject.url, "");
    }

    @Override
    public void play() {
        mServiceFactory.getAvService().play(null);
    }

    @Override
    public void pause() {
        mServiceFactory.getAvService().pause(null);
    }

    @Override
    public void stop() {
        mServiceFactory.getAvService().stop(null);
    }

    @Override
    public void seekTo(long position) {
        mServiceFactory.getAvService().seek(null, position);
    }

    @Override
    public void setVolume(int percent) {
        mServiceFactory.getRenderService().setVolume(null, percent);
    }

    @Override
    public void setMute(boolean mute) {
        mServiceFactory.getRenderService().setMute(null, mute);
    }

    @Override
    public void setBrightness(int percent) {
        mServiceFactory.getRenderService().setBrightness(null, percent);
    }
}
