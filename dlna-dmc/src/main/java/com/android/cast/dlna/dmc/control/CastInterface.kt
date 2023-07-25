package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.Logger
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState
import org.fourthline.cling.support.lastchange.EventedValue

interface DeviceControl : AvTransportServiceAction, RendererServiceAction {
    override fun getLogger(): Logger? = null
}

object EmptyDeviceControl : DeviceControl

interface SubscriptionListener {
    fun failed(subscriptionId: String?) {}
    fun established(subscriptionId: String?) {}
    fun ended(subscriptionId: String?) {}
    fun onReceived(subscriptionId: String?, event: EventedValue<*>) {
        if (event is TransportState) {
            onTransportStateChanged(subscriptionId, event.value)
        }
    }
    fun onTransportStateChanged(subscriptionId: String?, state: org.fourthline.cling.support.model.TransportState) {}
}
