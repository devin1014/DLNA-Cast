package com.android.cast.dlna.dms.player;

import com.android.cast.dlna.dms.DLNARendererService;
import com.android.cast.dlna.dms.localservice.RendererAVTransportService;
import com.android.cast.dlna.dms.localservice.RendererAudioControlService;
import com.android.cast.dlna.dms.ILogger;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;

/**
 *
 */
abstract class RendererThread extends Thread {
    DLNARendererService mRendererService;
    private ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private IActivityAliveCallback mCallback;

    RendererThread(IActivityAliveCallback callback, DLNARendererService service) {
        mCallback = callback;

        mRendererService = service;
    }

    @Override
    public void run() {
        mLogger.i(String.format("[%s] running!!!", getClass().getSimpleName()));

        if (mRendererService != null) {
            LocalDevice localDevice = mRendererService.getLocalDevice();

            if (localDevice != null) {
                running(localDevice);
            } else {
                mLogger.w("LocalDevice is NULL!");
            }
        } else {
            mLogger.w("NLUpnpRendererService is NULL!");
        }

        mLogger.i(String.format("[%s] exit!!!", getClass().getSimpleName()));

        mCallback = null;

        mRendererService = null;
    }

    public abstract void running(LocalDevice localDevice);

    boolean isActivityAlive() {
        return mCallback != null && !mCallback.isActivityDestroyed();
    }

    public interface IActivityAliveCallback {
        boolean isActivityDestroyed();
    }

    // ---------------------------------------------------------------------------------------------------
    // - AvControl
    // ---------------------------------------------------------------------------------------------------
    static class AvControlThread extends RendererThread {
        AvControlThread(IActivityAliveCallback activity, DLNARendererService service) {
            super(activity, service);
        }

        @Override
        public void running(LocalDevice localDevice) {
            LastChangeAwareServiceManager lastChangeAwareServiceManager = null;

            for (LocalService service : localDevice.getServices()) {
                if (service != null && service.getManager().getImplementation() instanceof RendererAVTransportService) {
                    lastChangeAwareServiceManager = (LastChangeAwareServiceManager) service.getManager();

                    break;
                }
            }

            while (mRendererService != null && isActivityAlive()) {
                try {
                    Thread.sleep(500);

                    if (lastChangeAwareServiceManager != null) {
                        lastChangeAwareServiceManager.fireLastChange();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class AudioControlThread extends RendererThread {
        AudioControlThread(IActivityAliveCallback activity, DLNARendererService service) {
            super(activity, service);
        }

        @Override
        public void running(LocalDevice localDevice) {
            LastChangeAwareServiceManager lastChangeAwareServiceManager = null;

            for (LocalService service : localDevice.getServices()) {
                if (service != null && service.getManager().getImplementation() instanceof RendererAudioControlService) {
                    lastChangeAwareServiceManager = (LastChangeAwareServiceManager) service.getManager();

                    break;
                }
            }

            while (mRendererService != null && isActivityAlive()) {
                try {
                    Thread.sleep(500);

                    if (lastChangeAwareServiceManager != null) {
                        lastChangeAwareServiceManager.fireLastChange();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
