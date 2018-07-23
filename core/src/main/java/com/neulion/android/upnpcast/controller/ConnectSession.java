package com.neulion.android.upnpcast.controller;

import com.neulion.android.upnpcast.controller.action.ActionCallbackListener;
import com.neulion.android.upnpcast.controller.action.ICastActionFactory;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.TransportInfo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-17
 * Time: 17:04
 */
public class ConnectSession extends BaseSession
{
    private static final int TASK_COUNT = 3;
    private static final int POSITION_INTERVAL = 60 * 1000; // 1min
    private ControlPoint mControlPoint;
    private ICastActionFactory mCastActionFactory;
    private ConnectSessionCallback mListener;
    private CountDownLatch mCountDownLatch;

    public interface ConnectSessionCallback
    {
        void onCastSession(TransportInfo transportInfo, MediaInfo mediaInfo, int volume);

        void onCastSessionTimeout();
    }

    public ConnectSession(ControlPoint controlPoint, ICastActionFactory factory, ConnectSessionCallback listener)
    {
        mControlPoint = controlPoint;

        mCastActionFactory = factory;

        mListener = listener;

        mLogger.d("created");
    }

    @Override
    public void start()
    {
        startTimer(0, POSITION_INTERVAL);
    }

    @Override
    public void stop()
    {
        stopTimer();
    }

    @Override
    protected void onInterval(int index)
    {
        mCountDownLatch = new CountDownLatch(TASK_COUNT);

        checkConnection();

        getMediaInfo();

        getVolume();

        try
        {
            mCountDownLatch.await(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        notifyRunnable(new Runnable()
        {
            @Override
            public void run()
            {
                if (mTransportInfo != null)
                {
                    mListener.onCastSession(mTransportInfo, mMediaInfo, mCurrentVolume);
                }
                else
                {
                    mListener.onCastSessionTimeout();
                }
            }
        });
    }

    private TransportInfo mTransportInfo = null;

    private void checkConnection()
    {
        ActionCallback action = mCastActionFactory.getAvService().getTransportInfo(new ActionCallbackListener()
        {
            @Override
            public void success(ActionInvocation invocation, Object... received)
            {
                TransportInfo transportInfo = (TransportInfo) received[0];

                mTransportInfo = transportInfo;

                mLogger.d(String.format("getTransportInfo:[%s][%s]", transportInfo.getCurrentTransportStatus().getValue(), transportInfo.getCurrentTransportState().getValue()));

                mCountDownLatch.countDown();
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                mTransportInfo = null;

                mLogger.e(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation != null ? operation.getStatusMessage() : "", defaultMsg));

                mCountDownLatch.countDown();
            }
        });

        mControlPoint.execute(action);
    }

    private MediaInfo mMediaInfo = null;

    private void getMediaInfo()
    {
        ActionCallback action = mCastActionFactory.getAvService().getMediaInfo(new ActionCallbackListener()
        {
            @Override
            public void success(ActionInvocation invocation, Object... received)
            {
                MediaInfo mediaInfo = (MediaInfo) received[0];

                mMediaInfo = mediaInfo;

                mLogger.d(String.format("getMediaInfo:[%s][%s]", mediaInfo.getCurrentURI(), mediaInfo.getMediaDuration()));

                mCountDownLatch.countDown();
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                mMediaInfo = null;

                mLogger.e(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation != null ? operation.getStatusMessage() : "", defaultMsg));

                mCountDownLatch.countDown();
            }
        });

        mControlPoint.execute(action);
    }

    private int mCurrentVolume = -1;

    private void getVolume()
    {
        ActionCallback action = mCastActionFactory.getRenderService().getVolumeAction(new ActionCallbackListener()
        {
            @Override
            public void success(ActionInvocation invocation, Object... received)
            {
                int volume = (int) received[0];

                mCurrentVolume = volume;

                mLogger.d(String.format("getVolume:[%s]", volume));

                mCountDownLatch.countDown();
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                mCurrentVolume = -1;

                mLogger.e(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation != null ? operation.getStatusMessage() : "", defaultMsg));

                mCountDownLatch.countDown();
            }
        });

        mControlPoint.execute(action);
    }
}
