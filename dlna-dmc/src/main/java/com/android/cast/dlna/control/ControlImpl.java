package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.ICast;
import com.android.cast.dlna.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;

public class ControlImpl implements IServiceAction.IServiceActionCallback<String>, ICastInterface.IControl {

    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private final AndroidUpnpService mService;
    private final IServiceFactory mServiceFactory;
    private final Device<?, ?, ?> mDevice;
    private SyncDataManager mSyncDataManager;

    public ControlImpl(@NonNull AndroidUpnpService upnpService, @NonNull Device<?, ?, ?> device, ICast castUri) {
        mService = upnpService;
        mDevice = device;
        mServiceFactory = new IServiceFactory.ServiceFactoryImpl(upnpService.getControlPoint(), device);
        mServiceFactory.getAvService().cast(this, castUri.getUri(), "");
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
        });
    }

    public void release() {
        if (mSyncDataManager != null) mSyncDataManager.release();
    }

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
