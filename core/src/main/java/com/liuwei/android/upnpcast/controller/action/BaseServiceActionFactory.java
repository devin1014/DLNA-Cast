package com.liuwei.android.upnpcast.controller.action;


import android.os.Handler;
import android.os.Looper;

import com.liuwei.android.upnpcast.util.ILogger;
import com.liuwei.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

/**
 */
public abstract class BaseServiceActionFactory
{
    private ILogger mLogger = new DefaultLoggerImpl(this);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    protected final void notifySuccess(final ActionCallbackListener listener, final ActionInvocation invocation, final Object... received)
    {
        notify(new Runnable()
        {
            @Override
            public void run()
            {
                listener.success(invocation, received);
            }
        });
    }

    protected final void notifyFailure(final ActionCallbackListener listener, final ActionInvocation invocation, final UpnpResponse operation, final String defaultMsg)
    {
        logErrorMsg(invocation, operation, defaultMsg);

        notify(new Runnable()
        {
            @Override
            public void run()
            {
                listener.failure(invocation, operation, defaultMsg);
            }
        });
    }

    private void notify(Runnable runnable)
    {
        if (Looper.myLooper() != Looper.getMainLooper())
        {
            mHandler.post(runnable);
        }
        else
        {
            runnable.run();
        }
    }

    private void logErrorMsg(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
    {
        mLogger.w(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation, defaultMsg));
    }
}
