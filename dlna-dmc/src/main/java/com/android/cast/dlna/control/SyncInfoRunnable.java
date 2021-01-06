package com.android.cast.dlna.control;

import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.cast.dlna.ILogger;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract class SyncInfoRunnable<T> implements Runnable {

    protected static final long DEFAULT_DURATION = 750L;
    protected static final long DEFAULT_TIMEOUT = 1000L;

    private final ControlPoint controlPoint;
    private final Service<?, ?> service;
    private final ICastInterface.ICastInfoListener<T> listener;
    private final Object[] result = new Object[1];
    private final ILogger logger = new ILogger.DefaultLoggerImpl(this);

    private long timeStamp = 0;
    private volatile boolean stopped = false;

    public SyncInfoRunnable(@NonNull ControlPoint controlPoint,
                            @NonNull Service<?, ?> service,
                            @Nullable ICastInterface.ICastInfoListener<T> listener) {
        this.controlPoint = controlPoint;
        this.service = service;
        this.listener = listener;
    }

    public void stop() {
        stopped = true;
    }

    protected long getSyncDuration() {
        return DEFAULT_DURATION;
    }

    protected long getFutureTimeout() {
        return DEFAULT_TIMEOUT;
    }

    protected final ControlPoint getControlPoint() {
        return controlPoint;
    }

    protected final Service<?, ?> getService() {
        return service;
    }

    protected abstract String getActionName();

    protected abstract ActionCallback getAction();

    protected void onFutureComplete() {
        if (listener != null && getResult() != null) {
            listener.onChanged(getResult());
        }
    }

    protected void onFutureException(Exception e) {
        e.printStackTrace();
        setError(e.getLocalizedMessage());
    }

    protected void setResult(@Nullable T t) {
        result[0] = t;
    }

    protected void setError(String errorMsg) {
        setResult(null);
        logger.e(!TextUtils.isEmpty(errorMsg) ? errorMsg : "error");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected T getResult() {
        return (T) result[0];
    }

    @Override
    public final void run() {
        if (TextUtils.isEmpty(getActionName())) {
            logger.e("not find action name!");
            return;
        } else if (service.getAction(getActionName()) == null) {
            logger.e(String.format("this service not support '%s' action.", getActionName()));
            return;
        }

        while (!stopped) {
            if (SystemClock.uptimeMillis() - timeStamp >= getSyncDuration()) {
                timeStamp = SystemClock.uptimeMillis();

                Future<?> future = getControlPoint().execute(getAction());

                try {
                    if (stopped) {
                        future.cancel(true);
                    } else {
                        future.get(getFutureTimeout(), TimeUnit.MILLISECONDS);
                    }
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    onFutureException(e);
                }
                if (!stopped) {
                    onFutureComplete();
                }
            }
        }
    }

}
