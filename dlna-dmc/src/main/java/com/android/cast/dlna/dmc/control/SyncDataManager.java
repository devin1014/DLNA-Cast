package com.android.cast.dlna.dmc.control;

import com.android.cast.dlna.dmc.DLNACastManager;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

import java.util.concurrent.atomic.AtomicInteger;

final class SyncDataManager {

    interface SubscriptionCallback {
        void onSubscriptionSuccess();

        void onSubscriptionFailed(String msg);
    }

    public SyncDataManager() {
    }

    private final AtomicInteger mSyncNumber = new AtomicInteger();
    private BaseSubscription mAvSubscription;
    private BaseSubscription mRendererSubscription;

    public synchronized void sync(ControlPoint controlPoint,
                                  Device<?, ?, ?> device,
                                  SubscriptionCallback subscriptionCallback) {
        release();
        final int currentNumber = mSyncNumber.incrementAndGet();
        SubscriptionCallback callbackImp = new SubscriptionCallback() {

            @Override
            public void onSubscriptionSuccess() {
                if (currentNumber == mSyncNumber.get() && subscriptionCallback != null) {
                    subscriptionCallback.onSubscriptionSuccess();
                }
            }

            @Override
            public void onSubscriptionFailed(String msg) {
                if (currentNumber == mSyncNumber.get() && subscriptionCallback != null) {
                    subscriptionCallback.onSubscriptionFailed(msg);
                }
            }
        };
        Service<?, ?> avService = device.findService(DLNACastManager.SERVICE_AV_TRANSPORT);
        Service<?, ?> rendererService = device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL);
        controlPoint.execute(mAvSubscription = new AvTransportSubscription(avService, callbackImp));
        controlPoint.execute(mRendererSubscription = new RendererSubscription(rendererService));
    }

    public synchronized void release() {
        mSyncNumber.incrementAndGet();
        if (mAvSubscription != null) mAvSubscription.stop();
        if (mRendererSubscription != null) mRendererSubscription.stop();
    }

    // ------------------------------------------------------------------------
    // ---- Subscription
    // ------------------------------------------------------------------------
    private static abstract class BaseSubscription extends DefaultSubscriptionCallback {

        public BaseSubscription(Service service, int requestedDurationSeconds) {
            super(service, requestedDurationSeconds);
        }

        public abstract void stop();
    }

    // ------------------------------------------------------------------------
    // ---- AvTransport
    // ------------------------------------------------------------------------
    private static final class AvTransportSubscription extends BaseSubscription {

        private final SubscriptionCallback subscriptionCallback;

        public AvTransportSubscription(Service service,
                                       SubscriptionCallback subscriptionCallback) {
            super(service, 300);
            this.subscriptionCallback = subscriptionCallback;
        }

        @Override
        public void onSubscriptionEstablished(GENASubscription<?> subscription) {
            subscriptionCallback.onSubscriptionSuccess();
            stop();
        }

        @Override
        public void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg) {
            subscriptionCallback.onSubscriptionFailed(defaultMsg);
            stop();
        }

        @Override
        public void stop() {
        }
    }

    // ------------------------------------------------------------------------
    // ---- Renderer
    // ------------------------------------------------------------------------
    private static final class RendererSubscription extends BaseSubscription {

        public RendererSubscription(Service service) {
            super(service, 300);
        }

        @Override
        public void onSubscriptionEstablished(GENASubscription<?> subscription) {
            stop();
        }

        @Override
        public void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg) {
            stop();
        }

        @Override
        public void stop() {
        }
    }

}
