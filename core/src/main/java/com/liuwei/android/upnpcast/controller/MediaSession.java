package com.liuwei.android.upnpcast.controller;

import com.liuwei.android.upnpcast.controller.BaseCastEventSubscription.EventCallbackListener;
import com.liuwei.android.upnpcast.controller.action.ActionCallbackListener;
import com.liuwei.android.upnpcast.controller.action.ICastActionFactory;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.model.PositionInfo;

/**
 */
public class MediaSession extends BaseSession
{
    private static final int POSITION_INTERVAL = 500;

    private ControlPoint mControlPoint;

    private ICastActionFactory mCastActionFactory;

    private ICastControlListener mControlListener;

    private SubscriptionCallback mAvServiceSubscription;

    private SubscriptionCallback mRenderServiceSubscription;

    public MediaSession(ControlPoint controlPoint, ICastActionFactory factory, ICastControlListener listener)
    {
        mControlPoint = controlPoint;

        mCastActionFactory = factory;

        mControlListener = listener;

        mControlPoint.execute(mAvServiceSubscription = factory.getAvService().subscriptionCallback(listener, mEventCallbackListener));

        mControlPoint.execute(mRenderServiceSubscription = factory.getRenderService().subscriptionCallback(listener, mEventCallbackListener));
    }

    public void start()
    {
        stopTimer();

        startTimer(POSITION_INTERVAL, POSITION_INTERVAL);
    }

    public void stop()
    {
        if (mAvServiceSubscription != null)
        {
            mAvServiceSubscription.end();

            mAvServiceSubscription = null;
        }

        if (mRenderServiceSubscription != null)
        {
            mRenderServiceSubscription.end();

            mRenderServiceSubscription = null;
        }

        stopTimer();
    }

    @Override
    protected void onInterval(int index)
    {
        updateMediaPosition(index);
    }

    private void updateMediaPosition(final int index)
    {
        if (mControlPoint != null)
        {
            ActionCallback action = mCastActionFactory.getAvService().getPositionInfoAction(new ActionCallbackListener()
            {
                @Override
                public void success(ActionInvocation invocation, Object... received)
                {
                    final PositionInfo positionInfo = (PositionInfo) received[0];

                    if (index % 10 == 0)
                    {
                        mLogger.d(positionInfo.toString());
                    }

                    notifyRunnable(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mControlListener.onUpdatePositionInfo(positionInfo);
                        }
                    });

                    if (!mSubscriptionCallback)
                    {
                        //TODO
                    }
                }
            });

            mControlPoint.execute(action);
        }
    }

    private boolean mSubscriptionCallback = false;

    private EventCallbackListener mEventCallbackListener = new EventCallbackListener()
    {
        @Override
        public void established(GENASubscription subscription)
        {
            mSubscriptionCallback = true;
        }

        @Override
        public void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus)
        {
            mSubscriptionCallback = false;
        }

        @Override
        public void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg)
        {
            mSubscriptionCallback = false;
        }
    };
}
