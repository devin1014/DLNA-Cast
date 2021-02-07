package com.android.cast.dlna.dmr.service;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;

public class AVTransportServiceImpl extends AbstractAVTransportService {

    private final RenderControlManager mRenderControlManager;

    public AVTransportServiceImpl(LastChange lastChange, RenderControlManager renderControlManager) {
        super(lastChange);
        mRenderControlManager = renderControlManager;
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return mRenderControlManager.getAvTransportCurrentInstanceIds();
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return mRenderControlManager.getAvTransportControl(instanceId).getCurrentTransportActions();
    }

    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) {
        return mRenderControlManager.getAvTransportControl(instanceId).getDeviceCapabilities();
    }

    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) {
        return mRenderControlManager.getAvTransportControl(instanceId).getMediaInfo();
    }

    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) {
        return mRenderControlManager.getAvTransportControl(instanceId).getPositionInfo();
    }

    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) {
        return mRenderControlManager.getAvTransportControl(instanceId).getTransportInfo();
    }

    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) {
        return mRenderControlManager.getAvTransportControl(instanceId).getTransportSettings();
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) {
        mRenderControlManager.getAvTransportControl(instanceId).next();
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        mRenderControlManager.getAvTransportControl(instanceId).pause();
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String arg1) throws AVTransportException {
        mRenderControlManager.getAvTransportControl(instanceId).play(arg1);
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) {
        mRenderControlManager.getAvTransportControl(instanceId).previous();
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) {
        mRenderControlManager.getAvTransportControl(instanceId).record();
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String arg1, String arg2) throws AVTransportException {
        mRenderControlManager.getAvTransportControl(instanceId).seek(arg1, arg2);
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String arg1, String arg2) throws AVTransportException {
        mRenderControlManager.getAvTransportControl(instanceId).setAVTransportURI(arg1, arg2);
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId, String arg1, String arg2) {
        mRenderControlManager.getAvTransportControl(instanceId).setNextAVTransportURI(arg1, arg2);
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String arg1) {
        mRenderControlManager.getAvTransportControl(instanceId).setPlayMode(arg1);
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String arg1) {
        mRenderControlManager.getAvTransportControl(instanceId).setRecordQualityMode(arg1);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        mRenderControlManager.getAvTransportControl(instanceId).stop();
    }

}
