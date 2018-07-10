package com.neulion.android.upnpcast.controller;

import android.os.Handler;
import android.os.Looper;

import com.neulion.android.upnpcast.util.CastUtils;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.util.Map;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-04
 * Time: 15:47
 */
public class BaseCastEventSubscription extends SubscriptionCallback
{
    protected ILogger mLogger;

    protected ICastControlListener mControlListener;

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    BaseCastEventSubscription(Service service, ICastControlListener listener)
    {
        super(service);

        mControlListener = listener;

        mLogger = new DefaultLoggerImpl(getClass().getSimpleName());
    }

    @Override
    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg)
    {
        mLogger.e("failed:" + defaultMsg);
    }

    @Override
    protected void established(GENASubscription subscription)
    {
        mLogger.w("established:" + subscription);
    }

    @Override
    protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus)
    {
        mLogger.w("end:" + subscription);
    }

    @Override
    protected void eventReceived(GENASubscription subscription)
    {
        mLogger.d("eventReceived:" + subscription);
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents)
    {
        mLogger.d("eventsMissed:" + subscription);
    }

    void notifyCallback(Runnable runnable)
    {
        if (Thread.currentThread() != Looper.getMainLooper().getThread())
        {
            mHandler.post(runnable);
        }
        else
        {
            runnable.run();
        }
    }

    // -------------------------------------------------------------------------------------------
    // Control listener
    // -------------------------------------------------------------------------------------------
    public static class AvTransportSubscription extends BaseCastEventSubscription
    {
        AvTransportSubscription(Service service, ICastControlListener listener)
        {
            super(service, listener);
        }

        @Override
        protected void eventReceived(GENASubscription subscription)
        {
            super.eventReceived(subscription);

            mLogger.d("currentValues:" + subscription.getCurrentValues());

            Map currentValues = subscription.getCurrentValues();

            if (currentValues != null && currentValues.containsKey("LastChange"))
            {
                String lastChange = currentValues.get("LastChange").toString();

                try
                {
                    doAVTransportChange(lastChange);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        private void doAVTransportChange(String value) throws Exception
        {
            LastChange lastChange = new LastChange(new AVTransportLastChangeParser(), value);

            //Parse TransportState value.
            AVTransportVariable.TransportState transportState = lastChange.getEventedValue(0, AVTransportVariable.TransportState.class);

            if (transportState != null)
            {
                TransportState ts = transportState.getValue();

                if (ts == TransportState.PLAYING)
                {
                    notifyCallback(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mControlListener.onStart();
                        }
                    });
                }
                else if (ts == TransportState.PAUSED_PLAYBACK)
                {
                    notifyCallback(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mControlListener.onPause();
                        }
                    });
                }
                else if (ts == TransportState.STOPPED)
                {
                    notifyCallback(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mControlListener.onStop();
                        }
                    });
                }
                else if (ts == TransportState.TRANSITIONING)
                {
                    //TODO:
                }

                return;
            }

            //RelativeTimePosition
            String position;

            AVTransportVariable.RelativeTimePosition relativeTimePosition = lastChange.getEventedValue(0, AVTransportVariable.RelativeTimePosition.class);

            if (relativeTimePosition != null)
            {
                position = lastChange.getEventedValue(0, AVTransportVariable.RelativeTimePosition.class).getValue();

                final int intTime = CastUtils.getIntTime(position);

                notifyCallback(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mControlListener.onSeekTo(intTime);
                    }
                });
            }
        }
    }

    public static class RenderSubscription extends BaseCastEventSubscription
    {
        RenderSubscription(Service service, ICastControlListener listener)
        {
            super(service, listener);
        }

        @Override
        protected void eventReceived(GENASubscription subscription)
        {
            super.eventReceived(subscription);

            Map currentValues = subscription.getCurrentValues();

            if (currentValues != null)
            {
                if (currentValues.containsKey("LastChange"))
                {
                    String lastChangeValue = currentValues.get("LastChange").toString();

                    mLogger.d("LastChange:" + lastChangeValue);

                    LastChange lastChange;

                    try
                    {
                        lastChange = new LastChange(new RenderingControlLastChangeParser(), lastChangeValue);
                        //获取音量 volume
                        if (lastChange.getEventedValue(0, RenderingControlVariable.Volume.class) != null)
                        {
                            final int volume = lastChange.getEventedValue(0, RenderingControlVariable.Volume.class).getValue().getVolume();

                            notifyCallback(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    mControlListener.onVolume(volume);
                                }
                            });
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
