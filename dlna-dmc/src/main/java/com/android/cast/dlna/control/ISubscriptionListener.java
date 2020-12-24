package com.android.cast.dlna.control;

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;

public interface ISubscriptionListener {
    void onSubscriptionEstablished(GENASubscription<?> subscription);

    void onSubscriptionEventReceived(GENASubscription<?> subscription);

    void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg);
}