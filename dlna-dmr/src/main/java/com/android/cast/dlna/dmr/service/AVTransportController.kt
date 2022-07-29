package com.android.cast.dlna.dmr.service

import android.content.Context
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.dmr.DLNARendererActivity
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
    override val instanceId: UnsignedIntegerFourBytes,
    private val mediaControl: RenderControl
) : IAVTransportControl {

    companion object {
        private val TRANSPORT_ACTION_STOPPED = arrayOf(Play)
        private val TRANSPORT_ACTION_PLAYING = arrayOf(Stop, Pause, Seek)
        private val TRANSPORT_ACTION_PAUSE_PLAYBACK = arrayOf(Play, Seek, Stop)
    }

    private val mApplicationContext: Context = context.applicationContext
    override val transportInfo = TransportInfo()
    override val transportSettings = TransportSettings()
    private var mOriginPositionInfo = PositionInfo()
    override var mediaInfo = MediaInfo()
        private set

    constructor(context: Context, control: RenderControl) : this(context, UnsignedIntegerFourBytes(0), control)

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
        get() = PositionInfo(mOriginPositionInfo, mediaControl.position / 1000, mediaControl.duration / 1000)

    @Throws(AVTransportException::class)
    override fun setAVTransportURI(currentURI: String?, currentURIMetaData: String?) {
        try {
            URI(currentURI)
        } catch (ex: Exception) {
            throw AVTransportException(INVALID_ARGS, "CurrentURI can not be null or malformed")
        }
        mediaInfo = MediaInfo(currentURI, currentURIMetaData, UnsignedIntegerFourBytes(1), "", NETWORK)
        mOriginPositionInfo = PositionInfo(1, currentURIMetaData, currentURI)
        DLNARendererActivity.startActivity(mApplicationContext, currentURI)
    }

    override fun setNextAVTransportURI(nextURI: String?, nextURIMetaData: String?) {}
    override fun play(speed: String?) {
        mediaControl.play()
    }

    override fun pause() {
        mediaControl.pause()
    }

    @Throws(AVTransportException::class)
    override fun seek(unit: String?, target: String?) {
        val seekMode = SeekMode.valueOrExceptionOf(unit)
        if (seekMode != REL_TIME) {
            throw AVTransportException(SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: $unit")
        }
        val position = Utils.getIntTime(target)
        mediaControl.seek(position)
    }

    override fun stop() = mediaControl.stop()

    override fun previous() {}
    override fun next() {}
    override fun record() {}
    override fun setPlayMode(newPlayMode: String?) {}
    override fun setRecordQualityMode(newRecordQualityMode: String?) {}

}