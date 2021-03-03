package com.android.cast.dlna.dmc.control;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.cast.dlna.core.ICast;
import com.android.cast.dlna.core.Utils;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;

import java.util.Map;

public class ControlImpl implements ICastInterface.IControl {

    private final IServiceFactory mServiceFactory;
    private final Device<?, ?, ?> mDevice;
    private final Map<String, IServiceAction.IServiceActionCallback<?>> mCallbackMap;
    private String mUri;

    public ControlImpl(@NonNull ControlPoint controlPoint, @NonNull Device<?, ?, ?> device,
                       Map<String, IServiceAction.IServiceActionCallback<?>> map, ICastInterface.ISubscriptionListener subscriptionListener) {
        mDevice = device;
        mCallbackMap = map;
        mServiceFactory = new IServiceFactory.ServiceFactoryImpl(controlPoint, device);
        ((BaseServiceExecutor) mServiceFactory.getAvService()).execute(event -> {
            if (subscriptionListener != null) {
                subscriptionListener.onSubscriptionTransportStateChanged(event);
            }
        });
        ((BaseServiceExecutor) mServiceFactory.getRenderService()).execute(event -> {
            if (subscriptionListener != null) {
                subscriptionListener.onSubscriptionTransportStateChanged(event);
            }
        });
    }

    @Override
    public void cast(Device<?, ?, ?> device, ICast object) {
        mUri = object.getUri();
        mServiceFactory.getAvService().cast(new ICastInterface.CastEventListener() {
            @Override
            public void onSuccess(String result) {
                IServiceAction.IServiceActionCallback<Object> listener = getCallback(IServiceAction.ServiceAction.CAST);
                if (listener != null) listener.onSuccess(result);
            }

            @Override
            public void onFailed(String errMsg) {
                IServiceAction.IServiceActionCallback<Object> listener = getCallback(IServiceAction.ServiceAction.CAST);
                if (listener != null) listener.onFailed(errMsg);
            }
        }, object.getUri(), Utils.getMetadata(object));
    }

    @Override
    public boolean isCasting(Device<?, ?, ?> device) {
        return mDevice.equals(device);
    }

    @Override
    public boolean isCasting(Device<?, ?, ?> device, @Nullable String uri) {
        if (TextUtils.isEmpty(uri)) return isCasting(device);
        return isCasting(device) && uri != null && uri.equals(mUri);
    }

    @Override
    public void stop() {
        mServiceFactory.getAvService().stop(getCallback(IServiceAction.ServiceAction.STOP));
    }

    @Override
    public void play() {
        mServiceFactory.getAvService().play(getCallback(IServiceAction.ServiceAction.PLAY));
    }

    @Override
    public void pause() {
        mServiceFactory.getAvService().pause(getCallback(IServiceAction.ServiceAction.PAUSE));
    }

    @Override
    public void seekTo(long position) {
        mServiceFactory.getAvService().seek(getCallback(IServiceAction.ServiceAction.SEEK_TO), position);
    }

    @Override
    public void setVolume(int percent) {
        mServiceFactory.getRenderService().setVolume(getCallback(IServiceAction.ServiceAction.SET_VOLUME), percent);
    }

    @Override
    public void setMute(boolean mute) {
        mServiceFactory.getRenderService().setMute(getCallback(IServiceAction.ServiceAction.SET_MUTE), mute);
    }

    @Override
    public void setBrightness(int percent) {
        mServiceFactory.getRenderService().setBrightness(getCallback(IServiceAction.ServiceAction.SET_BRIGHTNESS), percent);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> IServiceAction.IServiceActionCallback<T> getCallback(IServiceAction.ServiceAction action) {
        Object result = mCallbackMap.get(action.name());
        if (result == null) return null;
        return (IServiceAction.IServiceActionCallback<T>) result;
    }
}
