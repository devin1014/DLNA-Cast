package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.Logger
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.renderingcontrol.lastchange.EventedValueChannelMute
import org.fourthline.cling.support.renderingcontrol.lastchange.EventedValueChannelVolume

interface DeviceControl : AvTransportServiceAction, RendererServiceAction, ContentServiceAction {
    override fun getLogger(): Logger? = null
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

object EmptyDeviceControl : DeviceControl

internal interface SubscriptionListener {
    fun failed(subscriptionId: String?) {}
    fun established(subscriptionId: String?) {}
    fun ended(subscriptionId: String?) {}
    fun onReceived(subscriptionId: String?, event: EventedValue<*>) {}
}
