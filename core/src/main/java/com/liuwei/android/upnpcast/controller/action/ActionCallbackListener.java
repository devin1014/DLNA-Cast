package com.liuwei.android.upnpcast.controller.action;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

/**
 */
public abstract class ActionCallbackListener
{
    public void success(ActionInvocation invocation, Object... received)
    {
    }

    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
    {
    }
}
