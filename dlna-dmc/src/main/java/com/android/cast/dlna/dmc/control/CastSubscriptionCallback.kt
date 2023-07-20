package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.ICastInterface.ISubscriptionListener
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
internal class CastSubscriptionCallback(
    service: Service<*, *>?,
    requestedDurationSeconds: Int,
    private val eventCallback: ISubscriptionListener?,
) : SubscriptionCallback(service, requestedDurationSeconds) {

    private val logger = Logger.create("SubscriptionCallback")
    private val lastChangeParser: LastChangeParser? = when (getService()?.serviceType?.type) {
        DLNACastManager.SERVICE_AV_TRANSPORT.type -> AVTransportLastChangeParser()
        DLNACastManager.SERVICE_RENDERING_CONTROL.type -> RenderingControlLastChangeParser()
        else -> null
    }

    override fun failed(subscription: GENASubscription<*>, responseStatus: UpnpResponse?, exception: Exception?, defaultMsg: String?) {
        logger.e("[${subscription.service.serviceType.type}] failed:${responseStatus}, $defaultMsg")
    }


    override fun established(subscription: GENASubscription<*>) {
        logger.i("[${subscription.service.serviceType.type}] established")
    }


    override fun ended(subscription: GENASubscription<*>, reason: CancelReason?, responseStatus: UpnpResponse?) {
        logger.i("[${subscription.service.serviceType.type}] ended: $responseStatus, $reason")
    }

    override fun eventsMissed(subscription: GENASubscription<*>, numberOfMissedEvents: Int) {
        logger.w("[${subscription.service.serviceType.type}] eventsMissed: $numberOfMissedEvents")
    }

    override fun eventReceived(subscription: GENASubscription<*>) {
        logger.i("[${subscription.service.serviceType.type}] eventReceived\n    events: ${subscription.currentValues.keys}")
        lastChangeParser?.also { parser ->
            val lastChangeEventValue = subscription.currentValues["LastChange"]?.value.toString()
            try {
                val events = parser.parse(lastChangeEventValue)?.instanceIDs?.firstOrNull()?.values
                events?.forEach { value ->
                    logger.i("    value : $value")
                    //TODO: remove the special class
                    if (value is TransportState) {
                        eventCallback?.onSubscriptionTransportStateChanged(value.value)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}