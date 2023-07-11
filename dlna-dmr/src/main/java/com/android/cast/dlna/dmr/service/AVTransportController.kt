package com.android.cast.dlna.dmr.service

import android.content.Context
import android.content.Intent
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.dmr.RenderControl
import com.android.cast.dlna.dmr.service.IRendererInterface.IAVTransportControl
import org.fourthline.cling.model.types.ErrorCode.INVALID_ARGS
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED
import org.fourthline.cling.support.avtransport.AVTransportException
import org.fourthline.cling.support.model.*
import org.fourthline.cling.support.model.SeekMode.REL_TIME
import org.fourthline.cling.support.model.StorageMedium.NETWORK
import org.fourthline.cling.support.model.TransportAction.*
import org.fourthline.cling.support.model.TransportState.PAUSED_PLAYBACK
import org.fourthline.cling.support.model.TransportState.PLAYING
import java.net.URI

class AVTransportController(
    context: Context,
    private val mediaControl: RenderControl,
    override val instanceId: UnsignedIntegerFourBytes = UnsignedIntegerFourBytes(0)
) : IAVTransportControl {

    companion object {
        private val TRANSPORT_ACTION_STOPPED = arrayOf(Play)
        private val TRANSPORT_ACTION_PLAYING = arrayOf(Stop, Pause, Seek)
        private val TRANSPORT_ACTION_PAUSE_PLAYBACK = arrayOf(Play, Seek, Stop)
    }

    private val applicationContext: Context = context.applicationContext
    private var originPositionInfo = PositionInfo()

    override val transportInfo = TransportInfo()
    override val transportSettings = TransportSettings()
    override var mediaInfo = MediaInfo()
        private set

    @get:Synchronized
    override val currentTransportActions: Array<TransportAction>
        get() = when (transportInfo.currentTransportState) {
            PLAYING -> TRANSPORT_ACTION_PLAYING
            PAUSED_PLAYBACK -> TRANSPORT_ACTION_PAUSE_PLAYBACK
            else -> TRANSPORT_ACTION_STOPPED
        }
    override val deviceCapabilities: DeviceCapabilities
        get() = DeviceCapabilities(arrayOf(NETWORK))
    override val positionInfo: PositionInfo
        get() = PositionInfo(originPositionInfo, mediaControl.position / 1000, mediaControl.duration / 1000)

    @Throws(AVTransportException::class)
    override fun setAVTransportURI(currentURI: String, currentURIMetaData: String?) {
        try {
            URI(currentURI)
        } catch (ex: Exception) {
            throw AVTransportException(INVALID_ARGS, "CurrentURI can not be null or malformed")
        }
        mediaInfo = MediaInfo(currentURI, currentURIMetaData, UnsignedIntegerFourBytes(1), "", NETWORK)
        originPositionInfo = PositionInfo(1, currentURIMetaData, currentURI)
        applicationContext.startActivity(Intent().apply {
            action = actionSetAvTransport
            putExtra(keyCurrentURI, currentURI)
            putExtra(keyCurrentURIMetaData, currentURIMetaData)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // start from service content,should add 'FLAG_ACTIVITY_NEW_TASK' flag.
        })
    }

    override fun setNextAVTransportURI(nextURI: String, nextURIMetaData: String?) {}
    override fun play(speed: String?) = mediaControl.play()
    override fun pause() = mediaControl.pause()

    @Throws(AVTransportException::class)
    override fun seek(unit: String?, target: String?) {
        val seekMode = SeekMode.valueOrExceptionOf(unit)
        if (seekMode != REL_TIME) {
            throw AVTransportException(SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: $unit")
        }
        mediaControl.seek(Utils.getIntTime(target))
    }

    override fun stop() = mediaControl.stop()
    override fun previous() {}
    override fun next() {}
    override fun record() {}
    override fun setPlayMode(newPlayMode: String?) {}
    override fun setRecordQualityMode(newRecordQualityMode: String?) {}
}