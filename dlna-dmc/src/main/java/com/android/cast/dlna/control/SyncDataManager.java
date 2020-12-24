package com.android.cast.dlna.control;

import android.os.SystemClock;

import com.android.cast.dlna.DLNACastManager;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class SyncDataManager {

    private final ControlPoint mControlPoint;
    private final Service<?, ?> mAvService;
    private final Service<?, ?> mRendererService;
    private final IEventCallback.ITransportChangedCallback mTransportCallback;
    private final IEventCallback.IVolumeChangedCallback mVolumeCallback;

    public SyncDataManager(ControlPoint controlPoint, Device<?, ?, ?> device,
                           IEventCallback.ITransportChangedCallback transportCallback,
                           IEventCallback.IVolumeChangedCallback volumeCallback) {
        mControlPoint = controlPoint;
        mTransportCallback = transportCallback;
        mVolumeCallback = volumeCallback;
        mAvService = device.findService(DLNACastManager.SERVICE_AV_TRANSPORT);
        mRendererService = device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL);
    }

    private BaseSubscription mAvSubscription;
    private BaseSubscription mRendererSubscription;

    public void sync() {
        release();
        mControlPoint.execute(mAvSubscription = new AvTransportSubscription(mControlPoint, mAvService, mTransportCallback));
        mControlPoint.execute(mRendererSubscription = new RendererSubscription(mControlPoint, mRendererService, mVolumeCallback));
    }

    public void release() {
        if (mAvSubscription != null) mAvSubscription.stop();
        if (mRendererSubscription != null) mRendererSubscription.stop();
    }

    // ------------------------------------------------------------------------
    // ---- Subscription
    // ------------------------------------------------------------------------
    private static abstract class BaseSubscription extends DefaultSubscriptionCallback {

        protected final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        protected final ControlPoint controlPoint;
        protected final IEventCallback callback;

        public BaseSubscription(ControlPoint controlPoint, Service service, int requestedDurationSeconds, IEventCallback callback) {
            super(service, requestedDurationSeconds);
            this.controlPoint = controlPoint;
            this.callback = callback;
        }

        public abstract void stop();
    }

    // ------------------------------------------------------------------------
    // ---- AvTransport
    // ------------------------------------------------------------------------
    private static final class AvTransportSubscription extends BaseSubscription {

        private SyncTransportRunnable syncTransportRunnable;

        public AvTransportSubscription(ControlPoint controlPoint, Service service, IEventCallback.ITransportChangedCallback callback) {
            super(controlPoint, service, 300, callback);
        }

        @Override
        public void onSubscriptionEstablished(GENASubscription<?> subscription) {
            stop();
            mExecutorService.execute(syncTransportRunnable = new SyncTransportRunnable(callback));
        }

        @Override
        public void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg) {
            stop();
        }

        @Override
        public void stop() {
            if (syncTransportRunnable != null) syncTransportRunnable.stop();
        }

        private final class SyncTransportRunnable implements Runnable {

            private final IEventCallback.ITransportChangedCallback transportCallback;
            private final TransportInfo[] transportInfoArray = new TransportInfo[1];

            private long timeStamp = 0;
            private volatile boolean stopped = false;

            public SyncTransportRunnable(IEventCallback transportCallback) {
                this.transportCallback = (IEventCallback.ITransportChangedCallback) transportCallback;
            }

            public void stop() {
                stopped = true;
            }

            @Override
            public void run() {
                if (getService().getAction("GetTransportInfo") == null) {
                    mLogger.e("this service not support 'GetTransportInfo' action.");
                    return;
                }

                while (!stopped) {
                    if (SystemClock.uptimeMillis() - timeStamp >= 1000) {
                        timeStamp = SystemClock.uptimeMillis();
                        Future<?> getTransportFuture = controlPoint.execute(new GetTransportInfo(getService()) {
                            @Override
                            public void received(ActionInvocation invocation, TransportInfo transportInfo) {
                                transportInfoArray[0] = transportInfo;
                            }

                            @Override
                            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                                transportInfoArray[0] = null;
                            }
                        });

                        try {
                            if (stopped) {
                                getTransportFuture.cancel(true);
                            } else {
                                getTransportFuture.get(1000, TimeUnit.MILLISECONDS);
                            }
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            e.printStackTrace();
                            transportInfoArray[0] = null;
                        }

                        if (transportInfoArray[0] != null && !stopped) {
                            if (transportCallback != null) {
                                transportCallback.onTransportChanged(transportInfoArray[0]);
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // ---- Renderer
    // ------------------------------------------------------------------------
    private static final class RendererSubscription extends BaseSubscription {

        private SyncRendererRunnable syncRunnable;

        public RendererSubscription(ControlPoint controlPoint, Service service, IEventCallback.IVolumeChangedCallback callback) {
            super(controlPoint, service, 300, callback);
        }

        @Override
        public void onSubscriptionEstablished(GENASubscription<?> subscription) {
            stop();
            mExecutorService.execute(syncRunnable = new SyncRendererRunnable(callback));
        }

        @Override
        public void onSubscriptionFinished(GENASubscription<?> subscription, UpnpResponse responseStatus, String defaultMsg) {
            stop();
        }

        @Override
        public void stop() {
            if (syncRunnable != null) syncRunnable.stop();
        }

        private final class SyncRendererRunnable implements Runnable {

            private final IEventCallback.IVolumeChangedCallback callback;
            private final int[] volumeArray = {-1};

            private long timeStamp = 0;
            private volatile boolean stopped = false;

            public SyncRendererRunnable(IEventCallback callback) {
                this.callback = (IEventCallback.IVolumeChangedCallback) callback;
            }

            public void stop() {
                stopped = true;
            }

            @Override
            public void run() {
                if (getService().getAction("GetVolume") == null) {
                    mLogger.e("this service not support 'GetVolume' action.");
                    return;
                }

                while (!stopped) {
                    if (SystemClock.uptimeMillis() - timeStamp >= 1000) {
                        timeStamp = SystemClock.uptimeMillis();
                        Future<?> getVolumeFuture = controlPoint.execute(new GetVolume(getService()) {
                            @Override
                            public void received(ActionInvocation actionInvocation, int currentVolume) {
                                volumeArray[0] = currentVolume;
                            }

                            @Override
                            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                                volumeArray[0] = -1;
                            }
                        });

                        try {
                            if (stopped) {
                                getVolumeFuture.cancel(true);
                            } else {
                                getVolumeFuture.get(1000, TimeUnit.MILLISECONDS);
                            }
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            e.printStackTrace();
                            volumeArray[0] = -1;
                        }

                        if (volumeArray[0] != -1 && !stopped) {
                            if (callback != null) {
                                callback.onVolumeChanged(volumeArray[0]);
                            }
                        }
                    }
                }
            }
        }
    }

}
