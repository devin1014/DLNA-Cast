package com.android.cast.dlna.control;

import androidx.annotation.CallSuper;

import com.android.cast.dlna.ILogger;
import com.android.cast.dlna.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;

/**
 *
 */
class DefaultSubscriptionCallback extends SubscriptionCallback implements ICastInterface.ISubscriptionListener {

    protected final ILogger mLogger = new DefaultLoggerImpl(this);
    protected final ICastInterface.ISubscriptionListener mEventCallback;

    public DefaultSubscriptionCallback(Service service, int requestedDurationSeconds) {
        this(service, requestedDurationSeconds, null);
    }

    public DefaultSubscriptionCallback(Service service, int requestedDurationSeconds, ICastInterface.ISubscriptionListener eventCallback) {
        super(service, requestedDurationSeconds);
        mEventCallback = eventCallback;
    }

    @Override
    @CallSuper
    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
        mLogger.e(String.format("[%s GENASubscription failed]: %s, %s", subscription.getService().getServiceType().getType(), responseStatus, defaultMsg));
        if (mEventCallback != null) {
            mEventCallback.onSubscriptionFinished(subscription, responseStatus, defaultMsg);
        }
        onSubscriptionFinished(subscription, responseStatus, defaultMsg);
    }

    @Override
    @CallSuper
    protected void established(GENASubscription subscription) {
        mLogger.i(String.format("[%s] [established]", subscription.getService().getServiceType().getType()));
        if (mEventCallback != null) {
            mEventCallback.onSubscriptionEstablished(subscription);
        }
        onSubscriptionEstablished(subscription);
    }

    @Override
    @CallSuper
    protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
        mLogger.i(String.format("[%s GENASubscription ended]: %s, %s", subscription.getService().getServiceType().getType(), responseStatus, reason));
        if (mEventCallback != null) {
            mEventCallback.onSubscriptionFinished(subscription, responseStatus, reason != null ? reason.toString() : "");
        }
        onSubscriptionFinished(subscription, responseStatus, reason != null ? reason.toString() : "");
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
        if (mEventCallback != null) {
            mEventCallback.onSubscriptionEventReceived(subscription);
        }
        onSubscriptionEventReceived(subscription);
    }

    @Override
    public void onSubscriptionEstablished(GENASubscription<?> subscription) {
    }

    @Override
    public void onSubscriptionEventReceived(GENASubscription<?> subscription) {
    }

    @Override
    public void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg) {
    }
}
