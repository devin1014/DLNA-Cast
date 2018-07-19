package com.neulion.android.upnpcast.controller;

import com.neulion.android.upnpcast.controller.action.ActionCallbackListener;
import com.neulion.android.upnpcast.controller.action.ICastActionFactory;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

import java.util.Timer;
import java.util.TimerTask;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-17
 * Time: 17:04
 */
public class ConnectSession extends BaseSession
{
    private static final int POSITION_INTERVAL = 60 * 1000; // 1min
    private ControlPoint mControlPoint;
    private ICastActionFactory mCastActionFactory;
    private ConnectSessionCallback mListener;
    private Timer mConnectRetryTimer;

    public interface ConnectSessionCallback
    {
        void onCastSession(TransportInfo transportInfo, MediaInfo mediaInfo);

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
        checkConnection();
    }

    private int mConnectErrorCount = 0;

    private void checkConnection()
    {
        ActionCallback action = mCastActionFactory.getAvService().getTransportInfo(new ActionCallbackListener()
        {
            @Override
            public void success(ActionInvocation invocation, Object... received)
            {
                TransportInfo transportInfo = (TransportInfo) received[0];

                mLogger.d(String.format("getTransportInfo:[%s][%s]", transportInfo.getCurrentTransportStatus().getValue(), transportInfo.getCurrentTransportState().getValue()));

                if (TransportState.PLAYING.getValue().equals(transportInfo.getCurrentTransportState().getValue()) ||
                        TransportState.PAUSED_PLAYBACK.getValue().equals(transportInfo.getCurrentTransportState().getValue()))
                {
                    getMediaInfo(transportInfo);
                }
                else
                {
                    mListener.onCastSession(transportInfo, null);
                }

                if (mConnectRetryTimer != null)
                {
                    mConnectRetryTimer.cancel();

                    mConnectRetryTimer = null;
                }

                mConnectErrorCount = 0;
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                mLogger.e(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation != null ? operation.getStatusMessage() : "", defaultMsg));

                mConnectErrorCount++;

                if (mConnectRetryTimer != null)
                {
                    mConnectRetryTimer.cancel();
                }

                if (mConnectErrorCount >= 3)
                {
                    mListener.onCastSessionTimeout();
                }
                else
                {
                    mConnectRetryTimer = new Timer();

                    mConnectRetryTimer.schedule(mTimerTask, 3 * 1000);
                }
            }
        });

        mControlPoint.execute(action);
    }

    private void getMediaInfo(final TransportInfo transportInfo)
    {
        ActionCallback action = mCastActionFactory.getAvService().getMediaInfo(new ActionCallbackListener()
        {
            @Override
            public void success(ActionInvocation invocation, Object... received)
            {
                MediaInfo mediaInfo = (MediaInfo) received[0];

                mLogger.d(String.format("getMediaInfo:[%s][%s]", mediaInfo.getCurrentURI(), mediaInfo.getMediaDuration()));

                mListener.onCastSession(transportInfo, mediaInfo);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                mLogger.e(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation != null ? operation.getStatusMessage() : "", defaultMsg));

                mListener.onCastSession(transportInfo, null);
            }
        });

        mControlPoint.execute(action);
    }

    private TimerTask mTimerTask = new TimerTask()
    {
        @Override
        public void run()
        {
            checkConnection();
        }
    };
}
