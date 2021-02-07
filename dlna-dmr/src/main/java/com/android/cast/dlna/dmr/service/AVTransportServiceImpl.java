package com.android.cast.dlna.dmr.service;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;

import java.util.Map;

public class AVTransportServiceImpl extends AbstractAVTransportService {
    private final Map<UnsignedIntegerFourBytes, IRendererInterface.IAVTransport> mRendererMediaControl;
    private final UnsignedIntegerFourBytes[] mUnsignedIntegerFourBytes;

    public AVTransportServiceImpl(LastChange lastChange, Map<UnsignedIntegerFourBytes, IRendererInterface.IAVTransport> rendererMediaControl) {
        super(lastChange);
        mRendererMediaControl = rendererMediaControl;
        mUnsignedIntegerFourBytes = new UnsignedIntegerFourBytes[rendererMediaControl.size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : mRendererMediaControl.keySet()) {
            mUnsignedIntegerFourBytes[i] = id;
            i++;
        }
    }

    private IRendererInterface.IAVTransportControl getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        IRendererInterface.IAVTransportControl player = mRendererMediaControl.get(instanceId);
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return mUnsignedIntegerFourBytes;
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return getInstance(instanceId).getCurrentTransportActions();
    }

    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getDeviceCapabilities();
    }

    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getMediaInfo();
    }

    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getPositionInfo();
    }

    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getTransportInfo();
    }

    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getTransportSettings();
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).next();
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).pause();
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String arg1) throws AVTransportException {
        getInstance(instanceId).play(arg1);
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).previous();
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).record();
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String arg1, String arg2) throws AVTransportException {
        getInstance(instanceId).seek(arg1, arg2);
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String arg1, String arg2) throws AVTransportException {
        getInstance(instanceId).setAVTransportURI(arg1, arg2);
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId, String arg1, String arg2) throws AVTransportException {
        getInstance(instanceId).setNextAVTransportURI(arg1, arg2);
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String arg1) throws AVTransportException {
        getInstance(instanceId).setPlayMode(arg1);
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String arg1) throws AVTransportException {
        getInstance(instanceId).setRecordQualityMode(arg1);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).stop();
    }

}
