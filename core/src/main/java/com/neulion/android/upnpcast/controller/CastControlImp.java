package com.neulion.android.upnpcast.controller;

import android.os.Handler;
import android.os.Looper;

import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.android.upnpcast.device.CastDevice;
import com.neulion.android.upnpcast.service.NLUpnpCastService;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

import java.util.Timer;
import java.util.TimerTask;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 11:33
 */
public class CastControlImp implements ICastControl
{
    private static final int POSITION_INTERVAL = 500;

    private ILogger mLogger = new DefaultLoggerImpl(getClass().getSimpleName());

    private ControlPoint mControlPoint;

    private CastCallbackActionHelper mCallbackActionHelper;

    private CastDevice mCastDevice;

    private SubscriptionCallback mAvTransportSubscription;

    private SubscriptionCallback mRenderSubscription;

    private Timer mTimer;

    private ICastEventListener mCastEventListener;

    public CastControlImp(NLUpnpCastService service, ICastEventListener listener)
    {
        mLogger.d("new CastControlImp()");

        mControlPoint = service.getControlPoint();

        mCallbackActionHelper = new CastCallbackActionHelper(mCastEventListener = new CastControlListener(listener));
    }

    public void setNLUpnpCastService(NLUpnpCastService service)
    {
        if (service != null)
        {
            mControlPoint = service.getControlPoint();

            syncInfo();
        }
        else
        {
            unregisterCastSubscription();
        }
    }

    @Override
    public void connect(CastDevice castDevice)
    {
        if (mCastDevice != null)
        {
            disconnect();
        }

        mLogger.d(String.format("####connect [%s@%s]", castDevice.getName(), Integer.toHexString(castDevice.hashCode())));

        mCastDevice = castDevice;

        mCallbackActionHelper.setAVTransportService(getCastAVService());

        mCallbackActionHelper.setRenderControlService(getCastRenderService());

        syncInfo();
    }

    @Override
    public void disconnect()
    {
        if (mCastDevice != null)
        {
            mLogger.w(String.format("####disconnect [%s@%s]", mCastDevice.getName(), Integer.toHexString(mCastDevice.hashCode())));
        }

        final CastDevice device = mCastDevice;

        mCastDevice = null;

        unregisterCastSubscription();

        if (mCastEventListener != null)
        {
            if (device != null)
            {
                mCastEventListener.onDisconnect();
            }
        }
    }

    @Override
    public boolean isConnected()
    {
        return checkCastObject(); //TODO:
    }

    @Override
    public void cast(CastObject castObject)
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.setAvTransportAction(castObject));
        }
    }

    @Override
    public void start()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.setPlayAction());
        }
    }

    @Override
    public void pause()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.setPauseAction());
        }
    }

    @Override
    public void stop()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.setStopAction());
        }
    }

    @Override
    public void seekTo(long position)
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.setSeekAction(position));
        }
    }

    @Override
    public void setVolume(int percent)
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.setVolumeAction(percent));
        }
    }

    @Override
    public void setBrightness(int percent)
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.setBrightnessAction(percent));
        }
    }

    @Override
    public int getCastStatus()
    {
        return mCallbackActionHelper.getCastStatus();
    }

    private void syncInfo()
    {
        if (checkCastObject())
        {
            if (mCastEventListener != null)
            {
                mCastEventListener.onConnecting(mCastDevice);
            }

            checkTransportInfo();
        }
    }

    private void checkTransportInfo()
    {
        mControlPoint.execute(new GetTransportInfo(getCastAVService())
        {
            @Override
            public void received(ActionInvocation invocation, final TransportInfo transportInfo)
            {
                mLogger.d(String.format("[%s][%s][%s]",
                        actionInvocation.getAction().getName(), transportInfo.getCurrentTransportStatus().getValue(), transportInfo.getCurrentTransportState().getValue()));

                if (TransportState.PLAYING.getValue().equals(transportInfo.getCurrentTransportState().getValue()) ||
                        TransportState.PAUSED_PLAYBACK.getValue().equals(transportInfo.getCurrentTransportState().getValue()))
                {
                    if (checkCastObject())
                    {
                        registerCastSubscription();

                        checkMediaInfo(transportInfo);

                        mControlPoint.execute(mCallbackActionHelper.getVolumeAction());
                    }
                }
                else
                {
                    notifyCallback(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (mCastEventListener != null)
                            {
                                mCastEventListener.onConnected(mCastDevice, transportInfo, null);
                            }
                        }
                    });
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (mCastEventListener != null)
                        {
                            mCastEventListener.onDisconnect();
                        }
                    }
                });
            }
        });
    }

    private void checkMediaInfo(final TransportInfo transportInfo)
    {
        if (checkCastObject())
        {
            mControlPoint.execute(new GetMediaInfo(getCastAVService())
            {
                @Override
                public void received(ActionInvocation invocation, final MediaInfo mediaInfo)
                {
                    mLogger.d(String.format("[%s][%s][%s][%s]",
                            actionInvocation.getAction().getName(), mediaInfo.getCurrentURI(), mediaInfo.getMediaDuration(), mediaInfo.getCurrentURIMetaData()));

                    notifyCallback(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (mCastEventListener != null)
                            {
                                mCastEventListener.onConnected(mCastDevice, transportInfo, mediaInfo);
                            }
                        }
                    });
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    mLogger.w(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation, defaultMsg));

                    notifyCallback(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (mCastEventListener != null)
                            {
                                mCastEventListener.onConnected(mCastDevice, transportInfo, null);
                            }
                        }
                    });
                }
            });
        }
    }

    private boolean checkCastObject()
    {
        return mCastDevice != null && mControlPoint != null;
    }

    private Service getCastAVService()
    {
        return mCastDevice.getDevice().findService(NLUpnpCastManager.SERVICE_AV_TRANSPORT);
    }

    private Service getCastRenderService()
    {
        return mCastDevice.getDevice().findService(NLUpnpCastManager.SERVICE_RENDERING_CONTROL);
    }

    private void updateMediaPosition()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mCallbackActionHelper.getPositionInfoAction());
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void notifyCallback(Runnable runnable)
    {
        if (mCastEventListener != null)
        {
            if (Thread.currentThread() != Looper.getMainLooper().getThread())
            {
                if (mHandler != null && mCastEventListener != null)
                {
                    mHandler.post(runnable);
                }
            }
            else
            {
                runnable.run();
            }
        }
    }

    private void registerCastSubscription()
    {
        unregisterCastSubscription(); //FIXME,add retry register

        //av transport
        mAvTransportSubscription = mCallbackActionHelper.getAVTransportSubscription(mCastEventListener);

        mControlPoint.execute(mAvTransportSubscription);

        //render
        mRenderSubscription = mCallbackActionHelper.getRenderSubscription(mCastEventListener);

        mControlPoint.execute(mRenderSubscription);

        startTimer();
    }

    private void unregisterCastSubscription()
    {
        stopTimer();

        if (mAvTransportSubscription != null)
        {
            mAvTransportSubscription.end();

            mAvTransportSubscription = null;
        }

        if (mRenderSubscription != null)
        {
            mRenderSubscription.end();

            mRenderSubscription = null;
        }
    }

    private void startTimer()
    {
        stopTimer();

        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                updateMediaPosition();
            }
        }, POSITION_INTERVAL, POSITION_INTERVAL);

        mLogger.i("startTimer");
    }

    private void stopTimer()
    {
        if (mTimer != null)
        {
            mTimer.cancel();

            mLogger.i("stopTimer");

            mTimer = null;
        }
    }

    // --------------------------------------------------------------------------------
    // control listener
    // --------------------------------------------------------------------------------
    class CastControlListener extends CastControlListenerWrapper
    {
        CastControlListener(ICastEventListener listener)
        {
            super(listener);
        }

        @Override
        public void onCast(CastObject castObject)
        {
            super.onCast(castObject);

            registerCastSubscription();
        }

        @Override
        public void onStop()
        {
            super.onStop();

            unregisterCastSubscription();
        }

        @Override
        public void onError(String errorMsg)
        {
            super.onError(errorMsg);

            unregisterCastSubscription();
        }
    }
}
