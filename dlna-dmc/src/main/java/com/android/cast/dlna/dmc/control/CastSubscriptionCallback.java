package com.android.cast.dlna.dmc.control;

import androidx.annotation.CallSuper;

import com.android.cast.dlna.dmc.DLNACastManager;
import com.orhanobut.logger.Logger;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChangeParser;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.util.Map;

/**
 *
 */
final class CastSubscriptionCallback extends SubscriptionCallback {

    protected final ICastInterface.ISubscriptionListener mEventCallback;

    public CastSubscriptionCallback(Service service, int requestedDurationSeconds, ICastInterface.ISubscriptionListener eventCallback) {
        super(service, requestedDurationSeconds);
        mEventCallback = eventCallback;
    }

    @Override
    @CallSuper
    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
        Logger.e("[%s GENASubscription failed]: %s, %s", subscription.getService().getServiceType().getType(), responseStatus, defaultMsg);
    }

    @Override
    @CallSuper
    protected void established(GENASubscription subscription) {
        Logger.i("[%s] [established]", subscription.getService().getServiceType().getType());
    }

    @Override
    @CallSuper
    protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
        Logger.i("[%s GENASubscription ended]: %s, %s", subscription.getService().getServiceType().getType(), responseStatus, reason);
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        Logger.w("[%s GENASubscription eventsMissed]: %s", subscription.getService().getServiceType().getType(), numberOfMissedEvents);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    protected void eventReceived(GENASubscription subscription) {
        if (subscription.getCurrentValues() != null) {
            Logger.i("[%s GENASubscription eventReceived]\ncurrentValues: %s", subscription.getService().getServiceType().getType(), subscription.getCurrentValues());
        } else {
            Logger.i("[%s GENASubscription eventReceived]", subscription.getService().getServiceType().getType());
        }
        Map<String, StateVariableValue<?>> map = subscription.getCurrentValues();
        if (map != null && map.size() > 0) {
            if (map.containsKey("LastChange")) {
                LastChangeParser parser = getLastChangeParser();
                if (parser != null) {
                    Object value = map.get("LastChange").getValue();
                    try {
                        EventedValue<?> event = parser.parse((String) value).getInstanceIDs().get(0).getValues().get(0);
                        if (event instanceof AVTransportVariable.TransportState) {
                            mEventCallback.onSubscriptionTransportStateChanged(((AVTransportVariable.TransportState) event).getValue());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private LastChangeParser getLastChangeParser() {
        if (getService().getServiceType().getType().equals(DLNACastManager.SERVICE_AV_TRANSPORT.getType())) {
            return new AVTransportLastChangeParser();
        } else if (getService().getServiceType().getType().equals(DLNACastManager.SERVICE_RENDERING_CONTROL.getType())) {
            return new RenderingControlLastChangeParser();
        } else {
            return null;
        }
    }
}
