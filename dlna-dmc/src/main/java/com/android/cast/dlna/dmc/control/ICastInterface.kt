package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.ContentType
import com.android.cast.dlna.core.ICast
import com.android.cast.dlna.dmc.control.IServiceAction.IServiceActionCallback
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.TransportState

interface ICastInterface {
    // ------------------------------------------------------------------
    // ---- control
    // ------------------------------------------------------------------
    interface IControl {
        fun cast(device: Device<*, *, *>, cast: ICast)
        fun isCasting(device: Device<*, *, *>?): Boolean
        fun isCasting(device: Device<*, *, *>?, uri: String?): Boolean
        fun stop()
        fun play()
        fun pause()

        /**
         * @param position, current watch time(ms)
         */
        fun seekTo(position: Long)
        fun setVolume(percent: Int)
        fun setMute(mute: Boolean)
        fun setBrightness(percent: Int)
    }

    // ------------------------------------------------------------------
    // ---- GetInfo Listener
    // ------------------------------------------------------------------
    interface IGetInfo {
        fun getMediaInfo(device: Device<*, *, *>, listener: GetInfoListener<MediaInfo>?)
        fun getPositionInfo(device: Device<*, *, *>, listener: GetInfoListener<PositionInfo>?)
        fun getTransportInfo(device: Device<*, *, *>, listener: GetInfoListener<TransportInfo>?)
        fun getVolumeInfo(device: Device<*, *, *>, listener: GetInfoListener<Int>?)
        fun getContent(device: Device<*, *, *>, contentType: ContentType?, listener: GetInfoListener<DIDLContent>?)
    }

    interface GetInfoListener<T> {
        fun onGetInfoResult(t: T?, errMsg: String?)
    }

    // ------------------------------------------------------------------
    // ---- Event Listener
    // ------------------------------------------------------------------
    interface CastEventListener : IServiceActionCallback<String>
    interface PlayEventListener : IServiceActionCallback<Void>
    interface PauseEventListener : IServiceActionCallback<Void>
    interface StopEventListener : IServiceActionCallback<Void>
    interface SeekToEventListener : IServiceActionCallback<Long>

    // ------------------------------------------------------------------
    // ---- Subscriber Listener
    // ------------------------------------------------------------------
    interface ISubscriptionListener {
        fun onSubscriptionTransportStateChanged(event: TransportState)
    }
}