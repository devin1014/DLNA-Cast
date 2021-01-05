package com.android.cast.dlna.control;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.controller.CastObject;
import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class ControlImpl implements IConnect, IControl {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private final AndroidUpnpService mService;
    private final Device<?, ?, ?> mDevice;
    private final Service<?, ?> mAvService;
    private final Service<?, ?> mRendererService;

    public ControlImpl(@NonNull AndroidUpnpService upnpService, @NonNull CastDevice castDevice) {
        mService = upnpService;
        mDevice = castDevice.getDevice();
        mAvService = mDevice.findService(DLNACastManager.SERVICE_AV_TRANSPORT);
        mRendererService = mDevice.findService(DLNACastManager.SERVICE_RENDERING_CONTROL);
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
        if (!isConnected(mDevice)) return;
        mService.getControlPoint().execute(new SetAVTransportURI(mAvService, castObject.url, "") {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

            }
        });
    }
}
