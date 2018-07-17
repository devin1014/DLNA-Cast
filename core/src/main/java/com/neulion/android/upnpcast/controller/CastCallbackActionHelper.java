package com.neulion.android.upnpcast.controller;

import android.os.Handler;
import android.os.Looper;

import com.neulion.android.upnpcast.controller.BaseCastEventSubscription.AvTransportSubscription;
import com.neulion.android.upnpcast.controller.BaseCastEventSubscription.RenderSubscription;
import com.neulion.android.upnpcast.controller.action.GetBrightness;
import com.neulion.android.upnpcast.controller.action.SetBrightness;
import com.neulion.android.upnpcast.util.CastUtils;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;


/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 15:18
 */
class CastCallbackActionHelper
{
    private ILogger mLogger = new DefaultLoggerImpl(getClass().getSimpleName());

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ICastEventListener mCastEventListener;

    private Service mAVTransportService;

    private Service mRenderControlService;

    CastCallbackActionHelper(ICastEventListener listener)
    {
        mCastEventListener = listener;
    }

    public void setAVTransportService(Service castService)
    {
        mAVTransportService = castService;
    }

    public void setRenderControlService(Service renderControlService)
    {
        mRenderControlService = renderControlService;
    }

    public SetAVTransportURI setAvTransportAction(final CastObject castObject)
    {
        return new SetAVTransportURI(mAVTransportService, castObject.url, CastUtils.getMetadata(castObject))
        {
            @Override
            public void success(ActionInvocation invocation)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onCast(castObject);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, final String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);

                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onError(defaultMsg);
                    }
                });
            }
        };
    }

    public Play setPlayAction()
    {
        return new Play(mAVTransportService)
        {
            @Override
            public void success(ActionInvocation invocation)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onStart();
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public Pause setPauseAction()
    {
        return new Pause(mAVTransportService)
        {
            @Override
            public void success(ActionInvocation invocation)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onPause();
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public Stop setStopAction()
    {
        return new Stop(mAVTransportService)
        {
            @Override
            public void success(ActionInvocation invocation)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onStop();
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public Seek setSeekAction(final long position)
    {
        return new Seek(mAVTransportService, CastUtils.getStringTime(position))
        {
            @Override
            public void success(ActionInvocation invocation)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onSeekTo(position);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public SetVolume setVolumeAction(final int volume)
    {
        return new SetVolume(mRenderControlService, volume)
        {
            @Override
            public void success(ActionInvocation invocation)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onVolume(volume);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public SetBrightness setBrightnessAction(final int percent)
    {
        return new SetBrightness(mRenderControlService, percent)
        {
            @Override
            public void success(final ActionInvocation invocation)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onBrightness(percent);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public SetMute setMuteAction(boolean mute)
    {
        return new SetMute(mRenderControlService, mute)
        {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public GetMute getMuteAction()
    {
        return new GetMute(mRenderControlService)
        {
            @Override
            public void received(ActionInvocation actionInvocation, final boolean currentMute)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onVolume(0);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public GetVolume getVolumeAction()
    {
        return new GetVolume(mRenderControlService)
        {
            @Override
            public void received(ActionInvocation actionInvocation, final int currentVolume)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onVolume(currentVolume);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public GetBrightness getBrightnessAction()
    {
        return new GetBrightness(mRenderControlService)
        {
            @Override
            public void received(ActionInvocation actionInvocation, final int brightness)
            {
                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onBrightness(brightness);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    private int mTrackIndex = 0;

    public GetPositionInfo getPositionInfoAction()
    {
        return new GetPositionInfo(mAVTransportService)
        {
            @Override
            public void received(ActionInvocation invocation, final PositionInfo positionInfo)
            {
                mTrackIndex++;

                if (mTrackIndex % 10 == 0)
                {
                    mTrackIndex = 0;

                    mLogger.d(String.format("[%s][%s/%s]", invocation.getAction().getName(), positionInfo.getRelTime(), positionInfo.getTrackDuration()));
                }

                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mCastEventListener.onUpdatePositionInfo(positionInfo);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
            {
                logErrorMsg(invocation, operation, defaultMsg);
            }
        };
    }

    public SubscriptionCallback getAVTransportSubscription(ICastEventListener listener)
    {
        return new AvTransportSubscription(mAVTransportService, new CastControlListenerWrapper(listener));
    }

    public SubscriptionCallback getRenderSubscription(ICastEventListener listener)
    {
        return new RenderSubscription(mRenderControlService, new CastControlListenerWrapper(listener));
    }

    public int getCastStatus()
    {
        return ((CastControlListenerWrapper) mCastEventListener).getCastStatus();
    }

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

    private void logErrorMsg(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
    {
        mLogger.w(String.format("[%s][%s][%s]", invocation.getAction().getName(), operation, defaultMsg));
    }
}
