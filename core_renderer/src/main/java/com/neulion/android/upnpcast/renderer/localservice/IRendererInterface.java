package com.neulion.android.upnpcast.renderer.localservice;

import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-25
 * Time: 19:06
 */
public interface IRendererInterface
{
    interface IAVTransportControl
    {
        void setAVTransportURI(@UpnpInputArgument(name = "CurrentURI", stateVariable = "AVTransportURI") String currentURI,
                               @UpnpInputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData") String currentURIMetaData) throws AVTransportException;

        void setNextAVTransportURI(@UpnpInputArgument(name = "NextURI", stateVariable = "AVTransportURI") String nextURI,
                                   @UpnpInputArgument(name = "NextURIMetaData", stateVariable = "AVTransportURIMetaData") String nextURIMetaData);

        void setPlayMode(@UpnpInputArgument(name = "NewPlayMode", stateVariable = "CurrentPlayMode") String newPlayMode);

        void setRecordQualityMode(@UpnpInputArgument(name = "NewRecordQualityMode", stateVariable = "CurrentRecordQualityMode") String newRecordQualityMode);

        void play(@UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String speed) throws AVTransportException;

        void pause() throws AVTransportException;

        void seek(@UpnpInputArgument(name = "Unit", stateVariable = "A_ARG_TYPE_SeekMode") String unit,
                  @UpnpInputArgument(name = "Target", stateVariable = "A_ARG_TYPE_SeekTarget") String target) throws AVTransportException;

        void previous();

        void next();

        void stop() throws AVTransportException;

        void record();

        TransportAction[] getCurrentTransportActions() throws Exception;

        DeviceCapabilities getDeviceCapabilities();

        MediaInfo getMediaInfo();

        PositionInfo getPositionInfo();

        TransportInfo getTransportInfo();

        TransportSettings getTransportSettings();
    }

    interface IAVTransportUpdate
    {
        void setCurrentPosition(long position);

        void setDuration(long duration);
    }

    interface IAVTransport extends IAVTransportControl, IAVTransportUpdate
    {
    }
}
