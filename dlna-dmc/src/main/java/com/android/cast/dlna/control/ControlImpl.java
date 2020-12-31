package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class ControlImpl implements IConnect, IControl {

    private final AndroidUpnpService mService;
    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);

    public ControlImpl(@NonNull AndroidUpnpService upnpService) {
        mService = upnpService;
        // mAvService = mDevice.findService(DLNACastManager.SERVICE_AV_TRANSPORT);
        // mRendererService = mDevice.findService(DLNACastManager.SERVICE_RENDERING_CONTROL);
        // mSyncDataManager = new SyncDataManager(upnpService.getControlPoint(), mDevice, callback, mTransportInfoListener, mMediaInfoListener, mPositionInfoListener, mVolumeInfoListener);
    }

    // private final Service<?, ?> mAvService;
    // private final Service<?, ?> mRendererService;
    private SyncDataManager mSyncDataManager;
    private Device<?, ?, ?> mDevice;

    @Override
    public void connect(@NonNull CastDevice device, @NonNull IConnectCallback callback) {
        if (mSyncDataManager != null) mSyncDataManager.release();
        mSyncDataManager = new SyncDataManager();
        mSyncDataManager.sync(mService.getControlPoint(), device.getDevice(), new SyncDataManager.SubscriptionCallback() {
            @Override
            public void onSubscriptionSuccess() {
                mDevice = device.getDevice();
                callback.onDeviceConnected(device);
            }

            @Override
            public void onSubscriptionFailed(String msg) {
                callback.onDeviceDisconnected(device, msg);
                mDevice = null;
            }
        }, mTransportInfoListener, mMediaInfoListener, mPositionInfoListener, mVolumeInfoListener);
    }

    @Override
    public void disconnect() {
        if (mSyncDataManager != null) mSyncDataManager.release();
    }

    @Override
    public boolean isConnected() {
        return mDevice != null;
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
}
