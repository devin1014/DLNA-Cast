package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.CastObject;

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;

public interface ICastInterface {

    // ------------------------------------------------------------------
    // ---- cast
    // ------------------------------------------------------------------
    interface ICast {
        void cast(Device<?, ?, ?> device, CastObject object);

        void stop();
    }

    // ------------------------------------------------------------------
    // ---- control
    // ------------------------------------------------------------------
    interface IControl {
        boolean isCasting(Device<?, ?, ?> device);

        void stop();

        void play();

        void pause();

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
    // ---- event changed
    // ------------------------------------------------------------------
    interface ICastInfoListener<T> {
        void onChanged(@NonNull T t);
    }
}
