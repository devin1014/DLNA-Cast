package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.Logger
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
    val logger: Logger
    fun cast(uri: String, metadata: String?, callback: ServiceActionCallback<String>?) {
        logger.i("cast: $uri")
    }
    fun play(callback: ServiceActionCallback<String>?) {
        logger.i("play")
    }
    fun pause(callback: ServiceActionCallback<String>?){
        logger.i("pause")
    }
    fun stop(callback: ServiceActionCallback<String>?){
        logger.i("stop")
    }
    fun seek(millSeconds: Long, callback: ServiceActionCallback<Long>?){
        logger.i("seek: $millSeconds")
    }
    fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?)
    fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?)
    fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?)
}

// --------------------------------------------------------------------------------
// ---- RendererService
// --------------------------------------------------------------------------------
interface RendererServiceAction {
    val logger: Logger
    fun setVolume(volume: Int, callback: ServiceActionCallback<Int>?){
        logger.i("setVolume: $volume")
    }
    fun getVolume(callback: ServiceActionCallback<Int>?)
    fun setMute(mute: Boolean, callback: ServiceActionCallback<Boolean>?){
        logger.i("setMute: $mute")
    }
    fun isMute(callback: ServiceActionCallback<Boolean>?)
    fun setBrightness(percent: Int, callback: ServiceActionCallback<Int>?){
        logger.i("setBrightness: $percent")
    }
    fun getBrightness(callback: ServiceActionCallback<Int>?)
}