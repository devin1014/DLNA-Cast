package com.android.cast.dlna.controller.action;


import android.os.Handler;
import android.os.Looper;

import com.android.cast.dlna.util.ILogger;
import com.android.cast.dlna.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

/**
 *
 */
public abstract class BaseServiceActionFactory {
    private final ILogger mLogger = new DefaultLoggerImpl(this);
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    protected final void notifySuccess(final ActionCallbackListener listener, final ActionInvocation<?> invocation, final Object... received) {
        notify(() -> listener.success(invocation, received));
    }

    protected final void notifyFailure(final ActionCallbackListener listener, final ActionInvocation<?> invocation, final UpnpResponse operation, final String defaultMsg) {
        mLogger.w(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation, defaultMsg));
        notify(() -> listener.failure(invocation, operation, defaultMsg));
    }

    private void notify(Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }
}
