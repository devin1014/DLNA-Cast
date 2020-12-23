package com.android.cast.dlna.controller;

import androidx.annotation.CallSuper;

import com.android.cast.dlna.util.ILogger;
import com.android.cast.dlna.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;

/**
 *
 */
public abstract class AbstractSubscriptionCallback extends SubscriptionCallback {

    protected final ILogger mLogger = new DefaultLoggerImpl(this);
    protected final EventCallbackListener mEventCallback;

    AbstractSubscriptionCallback(Service service, int requestedDurationSeconds, EventCallbackListener eventCallback) {
        super(service, requestedDurationSeconds);
        mEventCallback = eventCallback;
    }

    @Override
    @CallSuper
    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
        mLogger.e(String.format("[%s GENASubscription failed]: %s, %s", subscription.getService().getServiceType().getType(), responseStatus, defaultMsg));
        if (mEventCallback != null) {
            mEventCallback.failed(subscription, responseStatus, exception, defaultMsg);
        }
    }

    @Override
    @CallSuper
    protected void established(GENASubscription subscription) {
        mLogger.i(String.format("[%s] [established]", subscription.getService().getServiceType().getType()));
        if (mEventCallback != null) {
            mEventCallback.established(subscription);
        }
    }

    @Override
    @CallSuper
    protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
        mLogger.i(String.format("[%s GENASubscription ended]: %s, %s", subscription.getService().getServiceType().getType(), responseStatus, reason));
        if (mEventCallback != null) {
            mEventCallback.ended(subscription, reason, responseStatus);
        }
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        mLogger.w(String.format("[%s GENASubscription eventsMissed]: %s", subscription.getService().getServiceType().getType(), numberOfMissedEvents));
    }

    @Override
    protected void eventReceived(GENASubscription subscription) {
        mLogger.i(String.format("[%s GENASubscription eventReceived]", subscription.getService().getServiceType().getType()));

        if (subscription.getCurrentValues() != null) {
            mLogger.i(String.format("currentValues: %s", subscription.getCurrentValues()));
        }

        // String lastChange = parseLastChange(subscription);
        //
        // if (!TextUtils.isEmpty(lastChange)) {
        //     LastChange action = null;
        //
        //     try {
        //         action = new LastChange(getLastChangeParser(), lastChange);
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //
        //         mLogger.e(e.getMessage());
        //     }
        //
        //     if (action != null) {
        //         processLastChange(action);
        //     }
        // }
    }

    // protected void processLastChange(@NonNull LastChange lastChange) {
    //     //mLogger.d("\r" + lastChange.toString());
    // }

    // protected abstract LastChangeParser getLastChangeParser();

    // protected String parseLastChange(GENASubscription<?> subscription) {
    //     Map currentValues = subscription.getCurrentValues();
    //
    //     if (currentValues != null && currentValues.containsKey("LastChange")) {
    //         return currentValues.get("LastChange").toString();
    //     }
    //
    //     return null;
    // }

    public interface EventCallbackListener {
        void established(GENASubscription<?> subscription);

        void ended(GENASubscription<?> subscription, CancelReason reason, UpnpResponse responseStatus);

        void failed(GENASubscription<?> subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg);
    }
}
