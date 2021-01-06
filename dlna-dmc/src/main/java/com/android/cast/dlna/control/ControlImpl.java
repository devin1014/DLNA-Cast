package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.CastObject;
import com.android.cast.dlna.util.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class ControlImpl implements ICastInterface.IControl {

    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private final AndroidUpnpService mService;
    private final IServiceFactory mServiceFactory;
    private final Device<?, ?, ?> mDevice;
    private SyncDataManager mSyncDataManager;

    public ControlImpl(@NonNull AndroidUpnpService upnpService, @NonNull Device<?, ?, ?> device, CastObject castObject) {
        mService = upnpService;
        mDevice = device;
        mServiceFactory = new IServiceFactory.ServiceFactoryImpl(upnpService.getControlPoint(), device);
        mServiceFactory.getAvService().cast(new ServiceAction.IServiceActionCallback<String>() {
            @Override
            public void onSuccess(String result) {
                sync();
            }

            @Override
            public void onFailed(String errMsg) {
            }
        }, castObject.url, "");
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

    private final ICastInterface.ICastInfoListener<TransportInfo> mTransportInfoListener = info -> {
        mLogger.i(String.format("TransportInfo: [%s] [%s] [%s]", info.getCurrentTransportStatus(), info.getCurrentTransportState(), info.getCurrentSpeed()));
    };

    private final ICastInterface.ICastInfoListener<MediaInfo> mMediaInfoListener = info -> {
        mLogger.i(String.format("onMediaChanged: %s", info.getCurrentURI()));
    };

    private final ICastInterface.ICastInfoListener<PositionInfo> mPositionInfoListener = info -> {
        mLogger.i(String.format("onPositionChanged: %s", info));
    };

    private final ICastInterface.ICastInfoListener<Integer> mVolumeInfoListener = integer -> {
        mLogger.i(String.format("onVolumeChanged: %s", integer));
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
