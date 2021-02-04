package com.android.cast.dlna.dms.localservice;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
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

public class RendererAVTransportService extends AbstractAVTransportService {
    private final Map<UnsignedIntegerFourBytes, IRendererInterface.IAVTransport> mRendererMediaControl;
    private final UnsignedIntegerFourBytes[] mUnsignedIntegerFourBytes;

    public RendererAVTransportService(LastChange lastChange, Map<UnsignedIntegerFourBytes, IRendererInterface.IAVTransport> rendererMediaControl) {
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

    @Override
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "PlayMedia", stateVariable = "PossiblePlaybackStorageMedia", getterName = "getPlayMediaString"),
            @UpnpOutputArgument(name = "RecMedia", stateVariable = "PossibleRecordStorageMedia", getterName = "getRecMediaString"),
            @UpnpOutputArgument(name = "RecQualityModes", stateVariable = "PossibleRecordQualityModes", getterName = "getRecQualityModesString")})
    public DeviceCapabilities getDeviceCapabilities(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        return getInstance(arg0).getDeviceCapabilities();
    }

    @Override
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "NrTracks", stateVariable = "NumberOfTracks", getterName = "getNumberOfTracks"),
            @UpnpOutputArgument(name = "MediaDuration", stateVariable = "CurrentMediaDuration", getterName = "getMediaDuration"),
            @UpnpOutputArgument(name = "CurrentURI", stateVariable = "AVTransportURI", getterName = "getCurrentURI"),
            @UpnpOutputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData", getterName = "getCurrentURIMetaData"),
            @UpnpOutputArgument(name = "NextURI", stateVariable = "NextAVTransportURI", getterName = "getNextURI"),
            @UpnpOutputArgument(name = "NextURIMetaData", stateVariable = "NextAVTransportURIMetaData", getterName = "getNextURIMetaData"),
            @UpnpOutputArgument(name = "PlayMedium", stateVariable = "PlaybackStorageMedium", getterName = "getPlayMedium"),
            @UpnpOutputArgument(name = "RecordMedium", stateVariable = "RecordStorageMedium", getterName = "getRecordMedium"),
            @UpnpOutputArgument(name = "WriteStatus", stateVariable = "RecordMediumWriteStatus", getterName = "getWriteStatus")})
    public MediaInfo getMediaInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        return getInstance(arg0).getMediaInfo();
    }

    @Override
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "Track", stateVariable = "CurrentTrack", getterName = "getTrack"),
            @UpnpOutputArgument(name = "TrackDuration", stateVariable = "CurrentTrackDuration", getterName = "getTrackDuration"),
            @UpnpOutputArgument(name = "TrackMetaData", stateVariable = "CurrentTrackMetaData", getterName = "getTrackMetaData"),
            @UpnpOutputArgument(name = "TrackURI", stateVariable = "CurrentTrackURI", getterName = "getTrackURI"),
            @UpnpOutputArgument(name = "RelTime", stateVariable = "RelativeTimePosition", getterName = "getRelTime"),
            @UpnpOutputArgument(name = "AbsTime", stateVariable = "AbsoluteTimePosition", getterName = "getAbsTime"),
            @UpnpOutputArgument(name = "RelCount", stateVariable = "RelativeCounterPosition", getterName = "getRelCount"),
            @UpnpOutputArgument(name = "AbsCount", stateVariable = "AbsoluteCounterPosition", getterName = "getAbsCount")})
    public PositionInfo getPositionInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        return getInstance(arg0).getPositionInfo();
    }

    @Override
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "CurrentTransportState", stateVariable = "TransportState", getterName = "getCurrentTransportState"),
            @UpnpOutputArgument(name = "CurrentTransportStatus", stateVariable = "TransportStatus", getterName = "getCurrentTransportStatus"),
            @UpnpOutputArgument(name = "CurrentSpeed", stateVariable = "TransportPlaySpeed", getterName = "getCurrentSpeed")})
    public TransportInfo getTransportInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        return getInstance(arg0).getTransportInfo();
    }

    @Override
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "PlayMode", stateVariable = "CurrentPlayMode", getterName = "getPlayMode"),
            @UpnpOutputArgument(name = "RecQualityMode", stateVariable = "CurrentRecordQualityMode", getterName = "getRecQualityMode")})
    public TransportSettings getTransportSettings(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        return getInstance(arg0).getTransportSettings();
    }

    @Override
    @UpnpAction
    public void next(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        getInstance(arg0).next();
    }

    @Override
    @UpnpAction
    public void pause(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        getInstance(arg0).pause();
    }

    @Override
    @UpnpAction
    public void play(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
                     @UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String arg1) throws AVTransportException {
        getInstance(arg0).play(arg1);
    }

    @Override
    @UpnpAction
    public void previous(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        getInstance(arg0).previous();
    }

    @Override
    @UpnpAction
    public void record(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        getInstance(arg0).record();
    }

    @Override
    @UpnpAction
    public void seek(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
                     @UpnpInputArgument(name = "Unit", stateVariable = "A_ARG_TYPE_SeekMode") String arg1,
                     @UpnpInputArgument(name = "Target", stateVariable = "A_ARG_TYPE_SeekTarget") String arg2) throws AVTransportException {
        getInstance(arg0).seek(arg1, arg2);
    }

    @Override
    @UpnpAction
    public void setAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
                                  @UpnpInputArgument(name = "CurrentURI", stateVariable = "AVTransportURI") String arg1,
                                  @UpnpInputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2) throws AVTransportException {
        getInstance(arg0).setAVTransportURI(arg1, arg2);
    }

    @Override
    @UpnpAction
    public void setNextAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
                                      @UpnpInputArgument(name = "NextURI", stateVariable = "AVTransportURI") String arg1,
                                      @UpnpInputArgument(name = "NextURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2) throws AVTransportException {
        getInstance(arg0).setNextAVTransportURI(arg1, arg2);
    }

    @Override
    @UpnpAction
    public void setPlayMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
                            @UpnpInputArgument(name = "NewPlayMode", stateVariable = "CurrentPlayMode") String arg1) throws AVTransportException {
        getInstance(arg0).setPlayMode(arg1);
    }

    @Override
    @UpnpAction
    public void setRecordQualityMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
                                     @UpnpInputArgument(name = "NewRecordQualityMode", stateVariable = "CurrentRecordQualityMode") String arg1) throws AVTransportException {
        getInstance(arg0).setRecordQualityMode(arg1);
    }

    @Override
    @UpnpAction
    public void stop(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        getInstance(arg0).stop();
    }

}
