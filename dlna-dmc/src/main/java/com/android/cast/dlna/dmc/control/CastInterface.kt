package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.ContentType
import com.android.cast.dlna.core.ICast
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo

// ------------------------------------------------------------------
// ---- control
// ------------------------------------------------------------------
interface Control {
    fun cast(device: Device<*, *, *>, cast: ICast)
    fun isCasting(device: Device<*, *, *>?): Boolean
    fun isCasting(device: Device<*, *, *>?, uri: String?): Boolean
    fun stop()
    fun play()
    fun pause()
    fun seekTo(millSeconds: Long)
    fun setVolume(percent: Int)
    fun setMute(mute: Boolean)
    fun setBrightness(percent: Int)
}

// ------------------------------------------------------------------
// ---- GetInfo Listener
// ------------------------------------------------------------------
interface GetInfo {
    fun getMediaInfo(device: Device<*, *, *>, listener: GetInfoListener<MediaInfo>?)
    fun getPositionInfo(device: Device<*, *, *>, listener: GetInfoListener<PositionInfo>?)
    fun getTransportInfo(device: Device<*, *, *>, listener: GetInfoListener<TransportInfo>?)
    fun getVolumeInfo(device: Device<*, *, *>, listener: GetInfoListener<Int>?)
    fun getContent(device: Device<*, *, *>, contentType: ContentType, listener: GetInfoListener<DIDLContent>?)
}

interface GetInfoListener<T> {
    fun onGetInfoResult(t: T?, errMsg: String?)
}

// ------------------------------------------------------------------
// ---- Event Listener
// ------------------------------------------------------------------
interface CastEventListener : ServiceActionCallback<String>
interface PlayEventListener : ServiceActionCallback<String>
interface PauseEventListener : ServiceActionCallback<String>
interface StopEventListener : ServiceActionCallback<String>
interface SeekToEventListener : ServiceActionCallback<Long>

// ------------------------------------------------------------------
// ---- Subscriber Listener
// ------------------------------------------------------------------
interface SubscriptionListener {
    fun onSubscriptionTransportStateChanged(event: EventedValue<*>)
}