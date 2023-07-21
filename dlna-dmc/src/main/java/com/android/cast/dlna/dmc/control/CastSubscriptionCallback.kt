package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.core.Logger
import org.fourthline.cling.controlpoint.SubscriptionCallback
import org.fourthline.cling.model.gena.CancelReason
import org.fourthline.cling.model.gena.GENASubscription
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState
import org.fourthline.cling.support.lastchange.LastChangeParser

/**
 *
 */
internal class CastSubscriptionCallback(
    service: Service<*, *>?,
    requestedDurationSeconds: Int = 1800, // Cling default 1800
    private val lastChangeParser: LastChangeParser,
    private val callback: SubscriptionListener,
) : SubscriptionCallback(service, requestedDurationSeconds) {

    private val logger = Logger.create("SubscriptionCallback")

    override fun failed(subscription: GENASubscription<*>, responseStatus: UpnpResponse?, exception: Exception?, defaultMsg: String?) {
        logger.e("${getTag(subscription)} failed:${responseStatus}, $exception, $defaultMsg")
    }

    override fun established(subscription: GENASubscription<*>) {
        logger.i("${getTag(subscription)} established")
    }


    override fun ended(subscription: GENASubscription<*>, reason: CancelReason?, responseStatus: UpnpResponse?) {
        logger.w("${getTag(subscription)} ended: $reason, $responseStatus")
    }

    override fun eventsMissed(subscription: GENASubscription<*>, numberOfMissedEvents: Int) {
        logger.w("${getTag(subscription)} eventsMissed: $numberOfMissedEvents")
    }

    override fun eventReceived(subscription: GENASubscription<*>) {
        logger.i("${getTag(subscription)} eventReceived: ${subscription.currentValues.keys}")
        val lastChangeEventValue = subscription.currentValues["LastChange"]?.value.toString()
        try {
            val events = lastChangeParser.parse(lastChangeEventValue)?.instanceIDs?.firstOrNull()?.values
            events?.forEach { value ->
                logger.i("    value : $value")
                //TODO: remove the special class
                if (value is TransportState) {
                    callback.onSubscriptionTransportStateChanged(value.value)
                }
            }
        } catch (e: Exception) {
            logger.w("${getTag(subscription)} currentValues: ${subscription.currentValues}")
            e.printStackTrace()
        }
    }

    private fun getTag(subscription: GENASubscription<*>) = "[${subscription.service.serviceType.type}](${subscription.subscriptionId.replace("uuid:", "")})"
}