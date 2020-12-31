package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.DLNACastManager;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
                                  SubscriptionCallback subscriptionCallback,
                                  ICastInfoListener<?>... listeners) {
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
        controlPoint.execute(mAvSubscription = new AvTransportSubscription(avService, callbackImp, listeners));
        controlPoint.execute(mRendererSubscription = new RendererSubscription(rendererService, listeners));
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

        private final ExecutorService mExecutorService = Executors.newCachedThreadPool();

        public BaseSubscription(Service service, int requestedDurationSeconds) {
            super(service, requestedDurationSeconds);
        }

        protected final ExecutorService getExecutorService() {
            return mExecutorService;
        }

        public abstract void stop();

        protected <T> ICastInfoListener<T> findListener(Class<T> classType, @NonNull ICastInfoListener<?>... listeners) {
            for (ICastInfoListener<?> l : listeners) {
                for (Type type : l.getClass().getGenericInterfaces()) {
                    if (type instanceof ParameterizedType && ((ParameterizedType) type).getActualTypeArguments() != null) {
                        Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        if (classType.getName().equals(((Class<?>) actualType).getName())) {
                            //noinspection unchecked
                            return (ICastInfoListener<T>) l;
                        }
                    }
                }
            }
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // ---- AvTransport
    // ------------------------------------------------------------------------
    private static final class AvTransportSubscription extends BaseSubscription {

        private SyncTransportInfo transportInfo;
        private SyncPositionInfo positionInfo;
        private SyncMediaInfo mediaInfo;
        private final SubscriptionCallback subscriptionCallback;
        private ICastInfoListener<TransportInfo> transportListener;
        private final ICastInfoListener<PositionInfo> positionListener;
        private final ICastInfoListener<MediaInfo> mediaListener;

        public AvTransportSubscription(Service service,
                                       SubscriptionCallback subscriptionCallback,
                                       ICastInfoListener<?>... listeners) {
            super(service, 300);
            this.subscriptionCallback = subscriptionCallback;
            this.transportListener = findListener(TransportInfo.class, listeners);
            this.positionListener = findListener(PositionInfo.class, listeners);
            this.mediaListener = findListener(MediaInfo.class, listeners);
        }

        @Override
        public void onSubscriptionEstablished(GENASubscription<?> subscription) {
            subscriptionCallback.onSubscriptionSuccess();
            stop();
            getExecutorService().execute(transportInfo = new SyncTransportInfo(getControlPoint(), getService(), transportInfoICastInfoListener));
            getExecutorService().execute(positionInfo = new SyncPositionInfo(getControlPoint(), getService(), positionListener));
            getExecutorService().execute(mediaInfo = new SyncMediaInfo(getControlPoint(), getService(), mediaListener));
        }

        @Override
        public void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg) {
            subscriptionCallback.onSubscriptionFailed(defaultMsg);
            stop();
        }

        private final ICastInfoListener<TransportInfo> transportInfoICastInfoListener = info -> {
            if (transportListener != null) {
                transportListener.onChanged(info);
            }
        };

        @Override
        public void stop() {
            if (transportInfo != null) transportInfo.stop();
            if (positionInfo != null) positionInfo.stop();
            if (mediaInfo != null) mediaInfo.stop();
        }
    }

    // ------------------------------------------------------------------------
    // ---- Renderer
    // ------------------------------------------------------------------------
    private static final class RendererSubscription extends BaseSubscription {

        private SyncVolumeInfo syncRunnable;
        private final ICastInfoListener<Integer> listener;

        public RendererSubscription(Service service, ICastInfoListener<?>... listeners) {
            super(service, 300);
            listener = findListener(Integer.class, listeners);
        }

        @Override
        public void onSubscriptionEstablished(GENASubscription<?> subscription) {
            stop();
            syncRunnable = new SyncVolumeInfo(getControlPoint(), getService(), listener);
            getExecutorService().execute(syncRunnable);
        }

        @Override
        public void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg) {
            stop();
        }

        @Override
        public void stop() {
            if (syncRunnable != null) syncRunnable.stop();
        }
    }

}
