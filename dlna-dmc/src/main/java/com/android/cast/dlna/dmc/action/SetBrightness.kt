package com.android.cast.dlna.dmc.action

import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes

/**
 *
 */
abstract class SetBrightness(
    service: Service<*, *>,
    newBrightness: Long
) : ActionCallback(ActionInvocation(service.getAction("SetBrightness"))) {
    init {
        getActionInvocation().setInput("InstanceID", UnsignedIntegerFourBytes(0))
        //getActionInvocation().setInput("Channel", Channel.Master.toString());
        getActionInvocation().setInput("DesiredBrightness", UnsignedIntegerTwoBytes(newBrightness))
    }

    override fun success(invocation: ActionInvocation<*>?) {}
}