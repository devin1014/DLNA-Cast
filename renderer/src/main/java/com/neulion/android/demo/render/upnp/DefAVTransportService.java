package com.neulion.android.demo.render.upnp;

import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.seamless.http.HttpFetch;
import org.seamless.util.URIUtil;

import java.net.URI;
import java.util.Map;

public class DefAVTransportService extends AbstractAVTransportService
{
    private ILogger mLogger = new DefaultLoggerImpl(this);

    private Map<UnsignedIntegerFourBytes, DefMediaControl> mMediaControlMap;

    public DefAVTransportService(LastChange lastChange, Map<UnsignedIntegerFourBytes, DefMediaControl> mediaControlMap)
    {
        super(lastChange);

        mMediaControlMap = mediaControlMap;
    }

    private DefMediaControl getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException
    {
        DefMediaControl player = mMediaControlMap.get(instanceId);

        if (player == null)
        {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }

        return player;
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds()
    {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[mMediaControlMap.size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : mMediaControlMap.keySet())
        {
            ids[i] = id;
            i++;
        }
        return ids;
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception
    {
        return getInstance(instanceId).getCurrentTransportActions();
    }

    @Override
    @UpnpAction(out = {@UpnpOutputArgument(name = "PlayMedia", stateVariable = "PossiblePlaybackStorageMedia", getterName = "getPlayMediaString"),
            @UpnpOutputArgument(name = "RecMedia", stateVariable = "PossibleRecordStorageMedia", getterName = "getRecMediaString"),
            @UpnpOutputArgument(name = "RecQualityModes", stateVariable = "PossibleRecordQualityModes", getterName = "getRecQualityModesString")})
    public DeviceCapabilities getDeviceCapabilities(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        getInstance(arg0);
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    @UpnpAction(out = {@UpnpOutputArgument(name = "NrTracks", stateVariable = "NumberOfTracks", getterName = "getNumberOfTracks"),
            @UpnpOutputArgument(name = "MediaDuration", stateVariable = "CurrentMediaDuration", getterName = "getMediaDuration"),
            @UpnpOutputArgument(name = "CurrentURI", stateVariable = "AVTransportURI", getterName = "getCurrentURI"),
            @UpnpOutputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData", getterName = "getCurrentURIMetaData"),
            @UpnpOutputArgument(name = "NextURI", stateVariable = "NextAVTransportURI", getterName = "getNextURI"),
            @UpnpOutputArgument(name = "NextURIMetaData", stateVariable = "NextAVTransportURIMetaData", getterName = "getNextURIMetaData"),
            @UpnpOutputArgument(name = "PlayMedium", stateVariable = "PlaybackStorageMedium", getterName = "getPlayMedium"),
            @UpnpOutputArgument(name = "RecordMedium", stateVariable = "RecordStorageMedium", getterName = "getRecordMedium"),
            @UpnpOutputArgument(name = "WriteStatus", stateVariable = "RecordMediumWriteStatus", getterName = "getWriteStatus")})
    public MediaInfo getMediaInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        mLogger.d("getMediaInfo called");
        return getInstance(arg0).getCurrentMediaInfo();
    }

    @Override
    @UpnpAction(out = {@UpnpOutputArgument(name = "Track", stateVariable = "CurrentTrack", getterName = "getTrack"),
            @UpnpOutputArgument(name = "TrackDuration", stateVariable = "CurrentTrackDuration", getterName = "getTrackDuration"),
            @UpnpOutputArgument(name = "TrackMetaData", stateVariable = "CurrentTrackMetaData", getterName = "getTrackMetaData"),
            @UpnpOutputArgument(name = "TrackURI", stateVariable = "CurrentTrackURI", getterName = "getTrackURI"),
            @UpnpOutputArgument(name = "RelTime", stateVariable = "RelativeTimePosition", getterName = "getRelTime"),
            @UpnpOutputArgument(name = "AbsTime", stateVariable = "AbsoluteTimePosition", getterName = "getAbsTime"),
            @UpnpOutputArgument(name = "RelCount", stateVariable = "RelativeCounterPosition", getterName = "getRelCount"),
            @UpnpOutputArgument(name = "AbsCount", stateVariable = "AbsoluteCounterPosition", getterName = "getAbsCount")})
    public PositionInfo getPositionInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        mLogger.d("getPositionInfo called");
        return getInstance(arg0).getCurrentPositionInfo();
    }

    @Override
    @UpnpAction(out = {@UpnpOutputArgument(name = "CurrentTransportState", stateVariable = "TransportState", getterName = "getCurrentTransportState"),
            @UpnpOutputArgument(name = "CurrentTransportStatus", stateVariable = "TransportStatus", getterName = "getCurrentTransportStatus"),
            @UpnpOutputArgument(name = "CurrentSpeed", stateVariable = "TransportPlaySpeed", getterName = "getCurrentSpeed")})
    public TransportInfo getTransportInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        mLogger.d("getTransportInfo called");
        return getInstance(arg0).getCurrentTransportInfo();
    }

    @Override
    @UpnpAction(out = {@UpnpOutputArgument(name = "PlayMode", stateVariable = "CurrentPlayMode", getterName = "getPlayMode"),
            @UpnpOutputArgument(name = "RecQualityMode", stateVariable = "CurrentRecordQualityMode", getterName = "getRecQualityMode")})
    public TransportSettings getTransportSettings(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        getInstance(arg0);
        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    @UpnpAction
    public void next(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        mLogger.i("### TODO: Not implemented: Next");
    }

    @Override
    @UpnpAction
    public void pause(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        mLogger.d("pause is called");
        getInstance(arg0).pause();

    }

    @Override
    @UpnpAction
    public void play(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String arg1)
            throws AVTransportException
    {
        getInstance(arg0).play();
    }

    @Override
    @UpnpAction
    public void previous(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        mLogger.i("### TODO: Not implemented: Previous");
    }

    @Override
    @UpnpAction
    public void record(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        mLogger.i("### TODO: Not implemented: Record");
    }

    @Override
    @UpnpAction
    public void seek(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "Unit", stateVariable = "A_ARG_TYPE_SeekMode") String arg1,
                     @UpnpInputArgument(name = "Target", stateVariable = "A_ARG_TYPE_SeekTarget") String arg2) throws AVTransportException
    {
        // TODO Auto-generated method stub

        final DefMediaControl player = getInstance(arg0);
        SeekMode seekMode;
        try
        {
            seekMode = SeekMode.valueOrExceptionOf(arg1);

            if (!seekMode.equals(SeekMode.REL_TIME))
            {
                throw new IllegalArgumentException();
            }

            // arg2 is in format of "hh:mm:ss"
            mLogger.d("seek target = " + arg2);
            mLogger.d("seek target = " + ModelUtil.fromTimeString(arg2));
            player.seekTo(((Long) ModelUtil.fromTimeString(arg2)).intValue() * 1000);

        }
        catch (IllegalArgumentException ex)
        {
            throw new AVTransportException(AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + arg1);
        }
    }

    @Override
    @UpnpAction
    public void setAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "CurrentURI", stateVariable = "AVTransportURI") String arg1,
                                  @UpnpInputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2) throws AVTransportException
    {

        URI uri;
        try
        {
            uri = new URI(arg1);
        }
        catch (Exception ex)
        {
            throw new AVTransportException(ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed");
        }

        if (arg1.startsWith("http:"))
        {
            try
            {
                HttpFetch.validate(URIUtil.toURL(uri));
            }
            catch (Exception ex)
            {
                throw new AVTransportException(AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.getMessage());
            }
        }
        else if (!arg1.startsWith("file:"))
        {
            throw new AVTransportException(ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported");
        }

        getInstance(arg0).setURI(uri);

    }

    @Override
    @UpnpAction
    public void setNextAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "NextURI", stateVariable = "AVTransportURI") String arg1,
                                      @UpnpInputArgument(name = "NextURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2) throws AVTransportException
    {
        mLogger.i("### TODO: Not implemented: SetNextAVTransportURI");
    }

    @Override
    @UpnpAction
    public void setPlayMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "NewPlayMode", stateVariable = "CurrentPlayMode") String arg1)
            throws AVTransportException
    {
        mLogger.i("### TODO: Not implemented: SetPlayMode");
    }

    @Override
    @UpnpAction
    public void setRecordQualityMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
                                     @UpnpInputArgument(name = "NewRecordQualityMode", stateVariable = "CurrentRecordQualityMode") String arg1) throws AVTransportException
    {
        mLogger.i("### TODO: Not implemented: SetRecordQualityMode");
    }

    @Override
    @UpnpAction
    public void stop(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException
    {
        getInstance(arg0).stop();
    }

}
