package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.jetbrains.annotations.NotNull;

public class ControlImpl implements IControl {

    private final AndroidUpnpService mService;
    private final Device<?, ?, ?> mDevice;
    private final Service<?, ?> mAvService;
    private final Service<?, ?> mRendererService;
    private final SyncDataManager mSyncDataManager;
    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);

    private final ICastInfoListener<TransportInfo> mTransportInfoListener = new ICastInfoListener<TransportInfo>() {
        @Override
        public void onChanged(@NotNull TransportInfo info) {
            mLogger.i(String.format("TransportInfo: [%s] [%s] [%s]", info.getCurrentTransportStatus(), info.getCurrentTransportState(), info.getCurrentSpeed()));
        }
    };

    private final ICastInfoListener<MediaInfo> mMediaInfoListener = new ICastInfoListener<MediaInfo>() {
        @Override
        public void onChanged(@NotNull MediaInfo info) {
            mLogger.i(String.format("onMediaChanged: %s", info.getCurrentURI()));
        }
    };

    private final ICastInfoListener<PositionInfo> mPositionInfoListener = new ICastInfoListener<PositionInfo>() {
        @Override
        public void onChanged(@NotNull PositionInfo info) {
            mLogger.i(String.format("onPositionChanged: %s", info));
        }
    };

    private final ICastInfoListener<Integer> mVolumeInfoListener = new ICastInfoListener<Integer>() {
        @Override
        public void onChanged(@NotNull Integer integer) {
            mLogger.i(String.format("onVolumeChanged: %s", integer));
        }
    };

    public ControlImpl(@NonNull AndroidUpnpService upnpService, @NonNull CastDevice device, @NonNull SyncDataManager.SubscriptionCallback callback) {
        mService = upnpService;
        mDevice = device.getDevice();
        mAvService = mDevice.findService(DLNACastManager.SERVICE_AV_TRANSPORT);
        mRendererService = mDevice.findService(DLNACastManager.SERVICE_RENDERING_CONTROL);
        mSyncDataManager = new SyncDataManager(upnpService.getControlPoint(), mDevice, callback, mTransportInfoListener, mMediaInfoListener, mPositionInfoListener, mVolumeInfoListener);
    }

    public void sync() {
        mSyncDataManager.sync();
    }

    public void release() {
        mSyncDataManager.release();
    }

}
