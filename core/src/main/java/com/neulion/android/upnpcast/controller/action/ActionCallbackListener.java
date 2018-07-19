package com.neulion.android.upnpcast.controller.action;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-17
 * Time: 18:06
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
