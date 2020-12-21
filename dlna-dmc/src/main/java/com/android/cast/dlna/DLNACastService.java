package com.android.cast.dlna;

import android.content.Intent;

import com.android.cast.dlna.util.ILogger;
import com.android.cast.dlna.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.registry.Registry;

/**
 *
 */
public class DLNACastService extends AndroidUpnpServiceImpl implements AndroidUpnpService {
    private final ILogger mLogger = new DefaultLoggerImpl(this);

    @Override
    public void onCreate() {
        mLogger.i(String.format("[%s] onCreate", getClass().getSimpleName()));
        org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());
        super.onCreate();
        binder = new DLNACastBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLogger.i(String.format("[%s] onStartCommand: %s , %s", getClass().getSimpleName(), intent, flags));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mLogger.w(String.format("[%s] onDestroy", getClass().getSimpleName()));
        super.onDestroy();
    }

    @Override
    protected UpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration() {
            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                return new ServiceType[]{DLNACastManager.SERVICE_RENDERING_CONTROL, DLNACastManager.SERVICE_AV_TRANSPORT};
            }
        };
    }

    @Override
    public UpnpService get() {
        return binder.get();
    }

    @Override
    public UpnpServiceConfiguration getConfiguration() {
        return binder.getConfiguration();
    }

    @Override
    public Registry getRegistry() {
        return binder.getRegistry();
    }

    @Override
    public ControlPoint getControlPoint() {
        return binder.getControlPoint();
    }

    // -------------------------------------------------------------------------------------
    // DLNACast service binder
    // -------------------------------------------------------------------------------------
    final class DLNACastBinder extends Binder {
        public AndroidUpnpService getService() {
            return DLNACastService.this;
        }
    }
}
