package com.neulion.android.upnpcast.controller.action;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-11
 * Time: 11:01
 */
public abstract class SetBrightness extends ActionCallback
{
    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public SetBrightness(Service service, long newBrightness)
    {
        super(new ActionInvocation(service.getAction("SetBrightness")));
        getActionInvocation().setInput("InstanceID", new UnsignedIntegerFourBytes(0));
        //getActionInvocation().setInput("Channel", Channel.Master.toString());
        getActionInvocation().setInput("DesiredBrightness", new UnsignedIntegerTwoBytes(newBrightness));
    }

    @Override
    public void success(ActionInvocation invocation)
    {
    }
}
