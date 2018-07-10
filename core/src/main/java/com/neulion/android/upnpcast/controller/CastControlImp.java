package com.neulion.android.upnpcast.controller;

import android.support.annotation.IntDef;

import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.android.upnpcast.device.CastDevice;
import com.neulion.android.upnpcast.service.NLUpnpCastService;
import com.neulion.android.upnpcast.util.UpnpCastUtil;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 11:33
 */
public class CastControlImp implements ICastControl
{
    public static final int IDLE = 0;
    public static final int CASTING = 1;
    public static final int PLAY = 2;
    public static final int PAUSE = 3;
    public static final int STOP = 4;
    public static final int BUFFER = 5;
    public static final int ERROR = 6;

    @IntDef({IDLE, CASTING, PLAY, PAUSE, STOP, BUFFER, ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CastStatus
    {
    }

    private ControlPoint mControlPoint;

    private CastControlActionHelper mActionHelper;

    private CastDevice mCastDevice;

    private SubscriptionCallback mAvTransportSubscription;

    private SubscriptionCallback mRenderSubscription;

    private Timer mTimer;

    @CastStatus
    private int mCastStatus = IDLE;

    public CastControlImp(NLUpnpCastService service, ICastControlListener listener)
    {
        mControlPoint = service.getControlPoint();

        mActionHelper = new CastControlActionHelper(new CastControlListenerWrapper(listener));
    }

    public void setNLUpnpCastService(NLUpnpCastService service)
    {
        //todo:check!
        mControlPoint = service.getControlPoint();
    }

    @Override
    public void connect(CastDevice castDevice)
    {
        mCastDevice = castDevice;

        mActionHelper.setAVTransportService(getCastAVService());

        mActionHelper.setRenderControlService(getCastRenderService());

        syncCasting();
    }

    @Override
    public void cast(CastObject castObject)
    {
        if (checkCastObject())
        {
            String metadata = UpnpCastUtil.pushMediaToRender(castObject.url, castObject.id, castObject.name, castObject.duration);

            mControlPoint.execute(mActionHelper.getCastOpenAction(castObject.url, metadata));
        }
    }

    @Override
    public void start()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mActionHelper.getCastPlayAction());
        }
    }

    @Override
    public void pause()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mActionHelper.getCastPauseAction());
        }
    }

    @Override
    public void stop()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mActionHelper.getCastStopAction());
        }
    }

    @Override
    public void seekTo(int position)
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mActionHelper.getCastSeekAction(position));
        }
    }

    @Override
    public void setVolume(int percent)
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mActionHelper.getCastVolumeAction(percent));
        }
    }

    @Override
    public int getCastStatus()
    {
        return mCastStatus;
    }

    public void syncCasting()
    {
        if (checkCastObject())
        {
            mControlPoint.execute(mActionHelper.getCastMediaInfo());
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
            mControlPoint.execute(mActionHelper.getCastPositionInfo());
        }
    }

    // --------------------------------------------------------------------------------
    // control listener
    // --------------------------------------------------------------------------------
    class CastControlListenerWrapper implements ICastControlListener
    {
        private ICastControlListener mListener;

        CastControlListenerWrapper(ICastControlListener listener)
        {
            mListener = listener;
        }

        @Override
        public void onOpen(String url)
        {
            mCastStatus = CASTING;

            if (mListener != null)
            {
                mListener.onOpen(url);
            }

            registerCastSubscription();

            startTimer();
        }

        @Override
        public void onStart()
        {
            mCastStatus = PLAY;

            if (mListener != null)
            {
                mListener.onStart();
            }
        }

        @Override
        public void onPause()
        {
            mCastStatus = PAUSE;

            if (mListener != null)
            {
                mListener.onPause();
            }
        }

        @Override
        public void onStop()
        {
            mCastStatus = STOP;

            if (mListener != null)
            {
                mListener.onStop();
            }

            unregisterCastSubscription();

            stopTimer();
        }

        @Override
        public void onSeekTo(long position)
        {
            if (mListener != null)
            {
                mListener.onSeekTo(position);
            }
        }

        @Override
        public void onError(String errorMsg)
        {
            mCastStatus = ERROR;

            if (mListener != null)
            {
                mListener.onError(errorMsg);
            }

            unregisterCastSubscription();

            stopTimer();
        }

        @Override
        public void onVolume(long volume)
        {
            if (mListener != null)
            {
                mListener.onVolume(volume);
            }
        }

        @Override
        public void onSyncMediaInfo(CastDevice castDevice, MediaInfo mediaInfo)
        {
            if (mListener != null)
            {
                mListener.onSyncMediaInfo(mCastDevice, mediaInfo);
            }
        }

        @Override
        public void onMediaPositionInfo(PositionInfo positionInfo)
        {
            if (mListener != null)
            {
                mListener.onMediaPositionInfo(positionInfo);
            }
        }

        private void registerCastSubscription()
        {
            unregisterCastSubscription();

            mControlPoint.execute(mAvTransportSubscription = mActionHelper.getAVTransportSubscription());

            mControlPoint.execute(mRenderSubscription = mActionHelper.getRenderSubscription());
        }

        private void unregisterCastSubscription()
        {
            if (mAvTransportSubscription != null)
            {
                mAvTransportSubscription.end();
            }

            if (mRenderSubscription != null)
            {
                mRenderSubscription.end();
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
            }, 1000, 1000);
        }

        private void stopTimer()
        {
            if (mTimer != null)
            {
                mTimer.cancel();
            }
        }
    }
}
