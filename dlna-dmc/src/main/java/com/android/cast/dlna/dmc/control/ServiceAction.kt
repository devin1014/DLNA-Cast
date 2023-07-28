package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.Logger
import org.fourthline.cling.model.ModelUtil
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo

interface ServiceActionCallback<T> {
    fun onResponse(response: ActionResponse<T>)
}

// --------------------------------------------------------------------------------
// ---- AvService
// --------------------------------------------------------------------------------
interface AvTransportServiceAction {
    fun getLogger(): Logger? = Logger.create("AvTransportService")//SetAVTransportURI
    fun setAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>? = null) {
        getLogger()?.i("setAVTransportURI: $title, $uri")
    }

    fun setNextAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>? = null) {
        getLogger()?.i("setNextAVTransportURI: $title, $uri")
    }

    fun play(callback: ServiceActionCallback<String>? = null) {
        getLogger()?.i("play")
    }

    fun pause(callback: ServiceActionCallback<String>? = null) {
        getLogger()?.i("pause")
    }

    fun stop(callback: ServiceActionCallback<String>? = null) {
        getLogger()?.i("stop")
    }

    fun seek(millSeconds: Long, callback: ServiceActionCallback<Long>? = null) {
        getLogger()?.i("seek: ${ModelUtil.toTimeString(millSeconds / 1000)}")
    }

    fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?) {}
    fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?) {}
    fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?) {}
}

// --------------------------------------------------------------------------------
// ---- RendererService
// --------------------------------------------------------------------------------
interface RendererServiceAction {
    fun getLogger(): Logger? = Logger.create("RendererService")
    fun setVolume(volume: Int, callback: ServiceActionCallback<Int>? = null) {
        getLogger()?.i("setVolume: $volume")
    }

    fun getVolume(callback: ServiceActionCallback<Int>?) {}
    fun setMute(mute: Boolean, callback: ServiceActionCallback<Boolean>? = null) {
        getLogger()?.i("setMute: $mute")
    }

    fun isMute(callback: ServiceActionCallback<Boolean>?) {}
}

// --------------------------------------------------------------------------------
// ---- ContentService
// --------------------------------------------------------------------------------
interface ContentServiceAction {
    fun getLogger(): Logger? = Logger.create("ContentService")
    fun browse(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {}
    fun search(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {}
}