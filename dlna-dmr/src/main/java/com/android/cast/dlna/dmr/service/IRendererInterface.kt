package com.android.cast.dlna.dmr.service

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes
import org.fourthline.cling.support.avtransport.AVTransportException
import org.fourthline.cling.support.model.*

/**
 *
 */
interface IRendererInterface {
    interface IControl {
        val instanceId: UnsignedIntegerFourBytes
    }

    // -------------------------------------------------------------------------------------------
    // - AvTransport
    // -------------------------------------------------------------------------------------------
    interface IAVTransportControl : IControl {
        @Throws(AVTransportException::class)
        fun setAVTransportURI(currentURI: String?, currentURIMetaData: String?)
        fun setNextAVTransportURI(nextURI: String?, nextURIMetaData: String?)
        fun setPlayMode(newPlayMode: String?)
        fun setRecordQualityMode(newRecordQualityMode: String?)

        @Throws(AVTransportException::class)
        fun play(speed: String?)

        @Throws(AVTransportException::class)
        fun pause()

        @Throws(AVTransportException::class)
        fun seek(unit: String?, target: String?)
        fun previous()
        operator fun next()

        @Throws(AVTransportException::class)
        fun stop()
        fun record()

        @get:Throws(Exception::class)
        val currentTransportActions: Array<TransportAction>?
        val deviceCapabilities: DeviceCapabilities?
        val mediaInfo: MediaInfo?
        val positionInfo: PositionInfo?
        val transportInfo: TransportInfo?
        val transportSettings: TransportSettings?
    }

    // -------------------------------------------------------------------------------------------
    // - Audio
    // -------------------------------------------------------------------------------------------
    interface IAudioControl : IControl {
        fun setMute(channelName: String, desiredMute: Boolean)
        fun getMute(channelName: String): Boolean
        fun setVolume(channelName: String, desiredVolume: UnsignedIntegerTwoBytes)
        fun getVolume(channelName: String): UnsignedIntegerTwoBytes
    }
}