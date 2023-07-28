package com.android.cast.dlna.dmc.control

import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo

interface ServiceActionCallback<T> {
    fun onSuccess(result: T)
    fun onFailure(msg: String)
}

// --------------------------------------------------------------------------------
// ---- AvService
// --------------------------------------------------------------------------------
interface AvTransportServiceAction {
    fun setAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<Unit>? = null)
    fun setNextAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<Unit>? = null)
    fun play(callback: ServiceActionCallback<Unit>? = null)
    fun pause(callback: ServiceActionCallback<Unit>? = null)
    fun stop(callback: ServiceActionCallback<Unit>? = null)
    fun seek(millSeconds: Long, callback: ServiceActionCallback<Unit>? = null)
    fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?)
    fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?)
    fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?)
}

// --------------------------------------------------------------------------------
// ---- RendererService
// --------------------------------------------------------------------------------
interface RendererServiceAction {
    fun setVolume(volume: Int, callback: ServiceActionCallback<Unit>? = null)
    fun getVolume(callback: ServiceActionCallback<Int>?)
    fun setMute(mute: Boolean, callback: ServiceActionCallback<Unit>? = null)
    fun getMute(callback: ServiceActionCallback<Boolean>?)
}

// --------------------------------------------------------------------------------
// ---- ContentService
// --------------------------------------------------------------------------------
interface ContentServiceAction {
    fun browse(containerId: String, callback: ServiceActionCallback<DIDLContent>?)
    fun search(containerId: String, callback: ServiceActionCallback<DIDLContent>?)
}