package com.android.cast.dlna.dmc.control;

import androidx.annotation.Nullable;

import com.android.cast.dlna.core.ICast;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.TransportState;

public interface ICastInterface {

    // ------------------------------------------------------------------
    // ---- control
    // ------------------------------------------------------------------
    interface IControl {
        void cast(Device<?, ?, ?> device, ICast object);

        boolean isCasting(Device<?, ?, ?> device);

        boolean isCasting(Device<?, ?, ?> device, @Nullable String uri);

        void stop();

        void play();

        void pause();

        /**
         * @param position, current watch time(ms)
         */
        void seekTo(long position);

        void setVolume(int percent);

        void setMute(boolean mute);

        void setBrightness(int percent);
    }

    // ------------------------------------------------------------------
    // ---- GetInfo Listener
    // ------------------------------------------------------------------
    interface GetInfoListener<T> {
        void onGetInfoResult(@Nullable T t, @Nullable String errMsg);
    }

    // ------------------------------------------------------------------
    // ---- Event Listener
    // ------------------------------------------------------------------
    interface CastEventListener extends IServiceAction.IServiceActionCallback<String> {
    }

    interface PlayEventListener extends IServiceAction.IServiceActionCallback<Void> {
    }

    interface PauseEventListener extends IServiceAction.IServiceActionCallback<Void> {
    }

    interface StopEventListener extends IServiceAction.IServiceActionCallback<Void> {
    }

    interface SeekToEventListener extends IServiceAction.IServiceActionCallback<Long> {
    }

    // ------------------------------------------------------------------
    // ---- Subscriber Listener
    // ------------------------------------------------------------------
    interface ISubscriptionListener {
        void onSubscriptionTransportStateChanged(TransportState event);
    }
}
