package com.android.cast.dlna.dmc.action

import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.action.ActionException
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.ErrorCode.ACTION_FAILED
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes

/**
 *
 */
abstract class GetBrightness(service: Service<*, *>) : ActionCallback(ActionInvocation(service.getAction("GetBrightness"))) {
    init {
        getActionInvocation().setInput("InstanceID", UnsignedIntegerFourBytes(0))
        //getActionInvocation().setInput("Channel", Channel.Master.toString());
    }

    override fun success(invocation: ActionInvocation<*>) {
        var ok = true
        var brightness = 0
        try {
            brightness = invocation.getOutput("CurrentBrightness").value.toString().toInt() // UnsignedIntegerTwoBytes...
        } catch (ex: Exception) {
            invocation.failure = ActionException(ACTION_FAILED, "Can't parse ProtocolInfo response: $ex", ex)
            failure(invocation, null)
            ok = false
        }
        if (ok) {
            received(invocation, brightness)
        }
    }

    abstract fun received(actionInvocation: ActionInvocation<*>?, brightness: Int)
}