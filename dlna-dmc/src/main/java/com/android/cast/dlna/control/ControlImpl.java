package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.CastObject;
import com.android.cast.dlna.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class ControlImpl implements IServiceAction.IServiceActionCallback<String>, ICastInterface.IControl {

    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private final AndroidUpnpService mService;
    private final IServiceFactory mServiceFactory;
    private final Device<?, ?, ?> mDevice;
    private SyncDataManager mSyncDataManager;

    public ControlImpl(@NonNull AndroidUpnpService upnpService, @NonNull Device<?, ?, ?> device, CastObject castObject) {
        mService = upnpService;
        mDevice = device;
        mServiceFactory = new IServiceFactory.ServiceFactoryImpl(upnpService.getControlPoint(), device);
        mServiceFactory.getAvService().cast(this, castObject.url, "");
    }

    @Override
    public void onSuccess(String result) {
        sync();
    }

    @Override
    public void onFailed(String errMsg) {
        //TODO
    }

    private void sync() {
        release();
        mSyncDataManager = new SyncDataManager();
        mSyncDataManager.sync(mService.getControlPoint(), mDevice, new SyncDataManager.SubscriptionCallback() {
            @Override
            public void onSubscriptionSuccess() {
            }

            @Override
            public void onSubscriptionFailed(String msg) {
            }
        }, mTransportInfoListener, mMediaInfoListener, mPositionInfoListener, mVolumeInfoListener);
    }

    public void release() {
        if (mSyncDataManager != null) mSyncDataManager.release();
    }

    @SuppressWarnings("Convert2Lambda")
    private final ICastInterface.ICastInfoListener<TransportInfo> mTransportInfoListener = new ICastInterface.ICastInfoListener<TransportInfo>() {
        @Override
        public void onChanged(@NonNull TransportInfo info) {
            mLogger.i(String.format("TransportInfo: [%s] [%s] [%s]", info.getCurrentTransportStatus(), info.getCurrentTransportState(), info.getCurrentSpeed()));
        }
    };

    @SuppressWarnings("Convert2Lambda")
    private final ICastInterface.ICastInfoListener<MediaInfo> mMediaInfoListener = new ICastInterface.ICastInfoListener<MediaInfo>() {
        @Override
        public void onChanged(@NonNull MediaInfo mediaInfo) {
            mLogger.i(String.format("onMediaChanged: %s", mediaInfo.getCurrentURI()));
        }
    };

    @SuppressWarnings("Convert2Lambda")
    private final ICastInterface.ICastInfoListener<PositionInfo> mPositionInfoListener = new ICastInterface.ICastInfoListener<PositionInfo>() {
        @Override
        public void onChanged(@NonNull PositionInfo positionInfo) {
            mLogger.i(String.format("onPositionChanged: %s", positionInfo));
        }
    };

    @SuppressWarnings("Convert2Lambda")
    private final ICastInterface.ICastInfoListener<Integer> mVolumeInfoListener = new ICastInterface.ICastInfoListener<Integer>() {
        @Override
        public void onChanged(@NonNull Integer integer) {
            mLogger.i(String.format("onVolumeChanged: %s", integer));
        }
    };

    @Override
    public boolean isCasting(Device<?, ?, ?> device) {
        return mDevice != null && mDevice.equals(device);
    }

    @Override
    public void stop() {
        release();
        mServiceFactory.getAvService().stop(null);
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
