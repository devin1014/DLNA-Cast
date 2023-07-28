package com.android.cast.dlna.dmc.control

import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.renderingcontrol.lastchange.EventedValueChannelMute
import org.fourthline.cling.support.renderingcontrol.lastchange.EventedValueChannelVolume

interface DeviceControl : AvTransportServiceAction, RendererServiceAction, ContentServiceAction

object EmptyDeviceControl : DeviceControl {
    override fun setAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>?) {}
    override fun setNextAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>?) {}
    override fun play(callback: ServiceActionCallback<String>?) {}
    override fun pause(callback: ServiceActionCallback<String>?) {}
    override fun stop(callback: ServiceActionCallback<String>?) {}
    override fun seek(millSeconds: Long, callback: ServiceActionCallback<Long>?) {}
    override fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?) {}
    override fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?) {}
    override fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?) {}
    override fun setVolume(volume: Int, callback: ServiceActionCallback<Int>?) {}
    override fun getVolume(callback: ServiceActionCallback<Int>?) {}
    override fun setMute(mute: Boolean, callback: ServiceActionCallback<Boolean>?) {}
    override fun isMute(callback: ServiceActionCallback<Boolean>?) {}
    override fun browse(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {}
    override fun search(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {}
}

interface OnDeviceControlListener {
    fun onConnected(device: Device<*, *, *>) {}
    fun onDisconnected(device: Device<*, *, *>) {}
    fun onEventChanged(event: EventedValue<*>) {
        when (event) {
            is TransportState -> onAvTransportStateChanged(event.value)
            is EventedValueChannelVolume -> onRendererVolumeChanged(event.value.volume)
            is EventedValueChannelMute -> onRendererVolumeMuteChanged(event.value.mute)
        }
    }

    fun onAvTransportStateChanged(state: org.fourthline.cling.support.model.TransportState) {}
    fun onRendererVolumeChanged(volume: Int) {}
    fun onRendererVolumeMuteChanged(mute: Boolean) {}
}

internal interface SubscriptionListener {
    fun failed(subscriptionId: String?) {}
    fun established(subscriptionId: String?) {}
    fun ended(subscriptionId: String?) {}
    fun onReceived(subscriptionId: String?, event: EventedValue<*>) {}
}
