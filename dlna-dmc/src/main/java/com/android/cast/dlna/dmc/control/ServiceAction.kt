package com.android.cast.dlna.dmc.control

import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo

enum class ServiceAction(val action: String) {
    CAST("cast"),
    PLAY("play"),
    PAUSE("pause"),
    STOP("stop"),
    SEEK_TO("seekTo"),
    SET_VOLUME("setVolume"),
    SET_MUTE("setMute"),
    SET_BRIGHTNESS("setBrightness");
}

interface ServiceActionCallback<T> {
    fun onResponse(response: ActionResponse<T>)
}

// --------------------------------------------------------------------------------
// ---- AvService
// --------------------------------------------------------------------------------
interface AvTransportServiceAction {
    fun cast(listener: ServiceActionCallback<String>?, uri: String, metadata: String?)
    fun play(listener: ServiceActionCallback<String>?)
    fun pause(listener: ServiceActionCallback<String>?)
    fun stop(listener: ServiceActionCallback<String>?)
    fun seek(listener: ServiceActionCallback<Long>?, position: Long)
    fun getPositionInfo(listener: ServiceActionCallback<PositionInfo>?)
    fun getMediaInfo(listener: ServiceActionCallback<MediaInfo>?)
    fun getTransportInfo(listener: ServiceActionCallback<TransportInfo>?)
}

// --------------------------------------------------------------------------------
// ---- RendererService
// --------------------------------------------------------------------------------
interface RendererServiceAction {
    fun setVolume(listener: ServiceActionCallback<Int>?, volume: Int)
    fun getVolume(listener: ServiceActionCallback<Int>?)
    fun setMute(listener: ServiceActionCallback<Boolean>?, mute: Boolean)
    fun isMute(listener: ServiceActionCallback<Boolean>?)
    fun setBrightness(listener: ServiceActionCallback<Int>?, percent: Int)
    fun getBrightness(listener: ServiceActionCallback<Int>?)
}