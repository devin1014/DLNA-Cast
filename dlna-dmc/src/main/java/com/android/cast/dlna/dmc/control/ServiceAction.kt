package com.android.cast.dlna.dmc.control

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
    fun setAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>? = null)
    fun setNextAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>? = null)
    fun play(callback: ServiceActionCallback<String>? = null)
    fun pause(callback: ServiceActionCallback<String>? = null)
    fun stop(callback: ServiceActionCallback<String>? = null)
    fun seek(millSeconds: Long, callback: ServiceActionCallback<Long>? = null)
    fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?)
    fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?)
    fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?)
}

// --------------------------------------------------------------------------------
// ---- RendererService
// --------------------------------------------------------------------------------
interface RendererServiceAction {
    fun setVolume(volume: Int, callback: ServiceActionCallback<Int>? = null)
    fun getVolume(callback: ServiceActionCallback<Int>?)
    fun setMute(mute: Boolean, callback: ServiceActionCallback<Boolean>? = null)
    fun isMute(callback: ServiceActionCallback<Boolean>?)
}

// --------------------------------------------------------------------------------
// ---- ContentService
// --------------------------------------------------------------------------------
interface ContentServiceAction {
    fun browse(containerId: String, callback: ServiceActionCallback<DIDLContent>?)
    fun search(containerId: String, callback: ServiceActionCallback<DIDLContent>?)
}