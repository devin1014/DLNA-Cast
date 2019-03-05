package com.liuwei.android.upnpcast.controller;

import android.os.Handler;
import android.os.Looper;

import com.liuwei.android.upnpcast.util.ILogger;
import com.liuwei.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import java.util.Timer;
import java.util.TimerTask;

/**
 */
public abstract class BaseSession implements ICastSession
{
    protected final ILogger mLogger = new DefaultLoggerImpl(this);

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Timer mTimer;

    private int mIndex;

    protected void startTimer(long delay, long interval)
    {
        stopTimer();

        mLogger.i("start");

        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                mIndex++;

                onInterval(mIndex);
            }
        }, delay, interval);
    }

    protected abstract void onInterval(int index);

    protected void stopTimer()
    {
        if (mTimer != null)
        {
            mTimer.cancel();

            mLogger.i("stop");
        }

        mTimer = null;

        mIndex = 0;
    }

    protected final void notifyRunnable(Runnable r)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            r.run();
        }
        else
        {
            mHandler.post(r);
        }
    }

    @Override
    public boolean isRunning()
    {
        return mTimer != null;
    }
}
