package com.android.cast.dlna.control;

import androidx.annotation.Nullable;

import com.android.cast.dlna.ICast;

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;

public interface ICastInterface {

    // ------------------------------------------------------------------
    // ---- control
    // ------------------------------------------------------------------
    interface IControl {
        void cast(Device<?, ?, ?> device, ICast object);

        boolean isCasting(Device<?, ?, ?> device);

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
    // ---- subscription
    // ------------------------------------------------------------------
    interface ISubscriptionListener {
        void onSubscriptionEstablished(GENASubscription<?> subscription);

        void onSubscriptionEventReceived(GENASubscription<?> subscription);

        void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg);
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
}
