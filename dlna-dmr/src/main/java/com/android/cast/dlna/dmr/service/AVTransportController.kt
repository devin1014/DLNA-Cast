package com.android.cast.dlna.dmr.service

import android.content.Context
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.dmr.RenderControl
import org.fourthline.cling.model.ModelUtil
import org.fourthline.cling.support.avtransport.AVTransportException
import org.fourthline.cling.support.model.*
import org.fourthline.cling.support.model.TransportAction.*

class AVTransportController(override val applicationContext: Context) : AvTransportControl {
    companion object {
        private val TRANSPORT_ACTION_STOPPED = arrayOf(Play)
        private val TRANSPORT_ACTION_PLAYING = arrayOf(Stop, Pause, Seek)
        private val TRANSPORT_ACTION_PAUSE_PLAYBACK = arrayOf(Play, Seek, Stop)
    }

    var mediaControl: RenderControl? = null
        set(value) {
            if (value != null) {
                _mediaInfo = MediaInfo(currentURI, currentURIMetaData)
                _positionInfo = PositionInfo(0, currentURIMetaData, currentURI)
            } else {
                mediaControl?.stop()
                _mediaInfo = MediaInfo()
                _positionInfo = PositionInfo()
            }
            field = value
        }
    private var _positionInfo = PositionInfo()
    private var _mediaInfo = MediaInfo()

    override val logger = Logger.create("AVTransportController")
    override val transportSettings = TransportSettings()
    override val deviceCapabilities: DeviceCapabilities = DeviceCapabilities(arrayOf(StorageMedium.UNKNOWN))
    override val transportInfo
        get() = mediaControl?.let { ctrl ->
            TransportInfo(ctrl.getState().toTransportState(), TransportStatus.OK, "1")
        } ?: TransportInfo()
    override val mediaInfo
        get() = _mediaInfo
    override val positionInfo: PositionInfo
        get() = mediaControl?.let { ctrl ->
            val duration = ModelUtil.toTimeString(ctrl.duration / 1000)
            val realTime = ModelUtil.toTimeString(ctrl.currentPosition / 1000)
            PositionInfo(0, duration, currentURI, realTime, realTime)
        } ?: PositionInfo()
    override val currentTransportActions: Array<TransportAction>
        get() = when (transportInfo.currentTransportState) {
            TransportState.PLAYING -> TRANSPORT_ACTION_PLAYING
            TransportState.PAUSED_PLAYBACK -> TRANSPORT_ACTION_PAUSE_PLAYBACK
            else -> TRANSPORT_ACTION_STOPPED
        }

    private var currentURI: String? = null
    private var currentURIMetaData: String? = null

    @Throws(AVTransportException::class)
    override fun setAVTransportURI(currentURI: String, currentURIMetaData: String?) {
        super.setAVTransportURI(currentURI, currentURIMetaData)
        this.currentURI = currentURI
        this.currentURIMetaData = currentURIMetaData
    }

    private var nextURI: String? = null
    private var nextURIMetaData: String? = null

    override fun setNextAVTransportURI(nextURI: String, nextURIMetaData: String?) {
        super.setNextAVTransportURI(nextURI, nextURIMetaData)
        this.nextURI = nextURI
        this.nextURIMetaData = nextURIMetaData
    }

    override fun play(speed: String?) {
        super.play(speed)
        mediaControl?.play()
    }

    override fun pause() {
        super.pause()
        mediaControl?.pause()
    }

    override fun seek(unit: String?, target: String?) {
        super.seek(unit, target)
        try {
            mediaControl?.seek(ModelUtil.fromTimeString(target) * 1000)
        } catch (e: Exception) {
            logger.w("seek failed: $e")
        }
    }

    override fun next() {
        super.next()
        if (nextURI != null && nextURIMetaData != null) {
            previousURI = currentURI
            previousURIMetaData = currentURIMetaData
            setAVTransportURI(nextURI!!, nextURIMetaData)
        }
        nextURI = null
        nextURIMetaData = null
    }

    private var previousURI: String? = null
    private var previousURIMetaData: String? = null
    override fun previous() {
        super.previous()
        if (previousURI != null && previousURIMetaData != null) {
            nextURI = currentURI
            nextURIMetaData = currentURIMetaData
            setAVTransportURI(previousURI!!, previousURIMetaData)
        }
        previousURI = null
        previousURIMetaData = null
    }

    override fun stop() {
        super.stop()
        mediaControl?.stop()
        _mediaInfo = MediaInfo()
        _positionInfo = PositionInfo()
    }
}
