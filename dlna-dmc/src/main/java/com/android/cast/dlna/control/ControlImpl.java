package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.util.ILogger;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.TransportInfo;

public class ControlImpl implements IControl, IEventCallback.ITransportChangedCallback, IEventCallback.IVolumeChangedCallback {

    private final AndroidUpnpService mService;
    private final Service<?, ?> mAvService;
    private final Service<?, ?> mRendererService;
    private final SyncDataManager mSyncDataManager;
    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);

    public ControlImpl(@NonNull AndroidUpnpService upnpService, @NonNull Device<?, ?, ?> device) {
        mService = upnpService;
        mAvService = device.findService(DLNACastManager.SERVICE_AV_TRANSPORT);
        mRendererService = device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL);
        mSyncDataManager = new SyncDataManager(upnpService.getControlPoint(), device, this, this);
    }

    @Override
    public void sync() {
        mSyncDataManager.sync();
    }

    @Override
    public void release() {
        mSyncDataManager.release();
    }

    @Override
    public void onTransportChanged(TransportInfo info) {
        mLogger.i(String.format("TransportInfo: [%s] [%s] [%s]", info.getCurrentTransportStatus(), info.getCurrentTransportState(), info.getCurrentSpeed()));
        // this called in background thread.
    }

    @Override
    public void onVolumeChanged(int volume) {
        mLogger.i(String.format("onVolumeChanged: %s", volume));
        // this called in background thread.
    }
}
