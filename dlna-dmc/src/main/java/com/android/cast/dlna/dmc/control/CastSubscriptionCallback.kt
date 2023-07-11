package com.android.cast.dlna.dmc.control

import androidx.annotation.CallSuper
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.ICastInterface.ISubscriptionListener
import com.orhanobut.logger.Logger
import org.fourthline.cling.controlpoint.SubscriptionCallback
import org.fourthline.cling.model.gena.CancelReason
import org.fourthline.cling.model.gena.GENASubscription
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState
import org.fourthline.cling.support.lastchange.LastChangeParser
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser

/**
 *
 */
internal open class CastSubscriptionCallback(
    service: Service<*, *>?,
    requestedDurationSeconds: Int,
    private val eventCallback: ISubscriptionListener?
) : SubscriptionCallback(service, requestedDurationSeconds) {

    @CallSuper
    override fun failed(subscription: GENASubscription<*>, responseStatus: UpnpResponse?, exception: Exception?, defaultMsg: String?) {
        Logger.e("[%s GENASubscription failed]: %s, %s", subscription.service.serviceType.type, responseStatus, defaultMsg)
    }

    @CallSuper
    override fun established(subscription: GENASubscription<*>) {
        Logger.i("[%s] [established]", subscription.service.serviceType.type)
    }

    @CallSuper
    override fun ended(subscription: GENASubscription<*>, reason: CancelReason?, responseStatus: UpnpResponse?) {
        Logger.i("[%s GENASubscription ended]: %s, %s", subscription.service.serviceType.type, responseStatus, reason)
    }

    override fun eventsMissed(subscription: GENASubscription<*>, numberOfMissedEvents: Int) {
        Logger.w("[%s GENASubscription eventsMissed]: %s", subscription.service.serviceType.type, numberOfMissedEvents)
    }

    override fun eventReceived(subscription: GENASubscription<*>) {
        if (subscription.currentValues != null) {
            Logger.i("[%s GENASubscription eventReceived]\ncurrentValues: %s", subscription.service.serviceType.type, subscription.currentValues)
        } else {
            Logger.i("[%s GENASubscription eventReceived]", subscription.service.serviceType.type)
        }
        val map = subscription.currentValues
        if (map != null && map.isNotEmpty()) {
            if (map.containsKey("LastChange")) {
                val parser = lastChangeParser
                if (parser != null) {
                    val value = map["LastChange"]!!.value
                    try {
                        val event = parser.parse(value as String).instanceIDs.firstOrNull()?.values?.get(0)
                        if (event is TransportState) {
                            eventCallback?.onSubscriptionTransportStateChanged(event.value)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private val lastChangeParser: LastChangeParser?
        get() = when (getService().serviceType.type) {
            DLNACastManager.SERVICE_AV_TRANSPORT.type -> AVTransportLastChangeParser()
            DLNACastManager.SERVICE_RENDERING_CONTROL.type -> RenderingControlLastChangeParser()
            else -> null
        }
}