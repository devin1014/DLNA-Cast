package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.Logger
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.renderingcontrol.lastchange.EventedValueChannelMute
import org.fourthline.cling.support.renderingcontrol.lastchange.EventedValueChannelVolume

interface DeviceControl : AvTransportServiceAction, RendererServiceAction {
    override fun getLogger(): Logger? = null
}

object EmptyDeviceControl : DeviceControl

interface SubscriptionListener {
    fun failed(subscriptionId: String?) {}
    fun established(subscriptionId: String?) {}
    fun ended(subscriptionId: String?) {}
    fun onReceived(subscriptionId: String?, event: EventedValue<*>) {
        when (event) {
            is TransportState -> onAvTransportStateChanged(subscriptionId, event.value)
            is EventedValueChannelVolume -> onRendererVolumeChanged(subscriptionId, event.value.volume)
            is EventedValueChannelMute -> onRendererVolumeMuteChanged(subscriptionId, event.value.mute)
        }
    }

    fun onAvTransportStateChanged(subscriptionId: String?, state: org.fourthline.cling.support.model.TransportState) {}
    fun onRendererVolumeChanged(subscriptionId: String?, volume: Int) {}
    fun onRendererVolumeMuteChanged(subscriptionId: String?, mute: Boolean) {}
}
