package com.liuwei.android.upnpcast.controller.action;

import com.liuwei.android.upnpcast.controller.BaseCastEventSubscription.AvTransportSubscription;
import com.liuwei.android.upnpcast.controller.BaseCastEventSubscription.EventCallbackListener;
import com.liuwei.android.upnpcast.controller.ICastControlListener;
import com.liuwei.android.upnpcast.util.CastUtils;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

/**
 */
public interface IAVServiceActionFactory
{
    SetAVTransportURI setCastAction(ActionCallbackListener listener, String uri, String metadata);

    Play playAction(ActionCallbackListener listener);

    Pause pauseAction(ActionCallbackListener listener);

    Stop stopAction(ActionCallbackListener listener);

    Seek seekAction(ActionCallbackListener listener, final long position);

    GetPositionInfo getPositionInfoAction(ActionCallbackListener listener);

    GetMediaInfo getMediaInfo(ActionCallbackListener listener);

    GetTransportInfo getTransportInfo(ActionCallbackListener listener);

    SubscriptionCallback subscriptionCallback(ICastControlListener listener, EventCallbackListener eventCallbackListener);

    // ---------------------------------------------------------------------------------------------------------
    // Implement
    // ---------------------------------------------------------------------------------------------------------
    class AvServiceActionFactory extends BaseServiceActionFactory implements IAVServiceActionFactory
    {
        private final Service mAvService;

        public AvServiceActionFactory(Service service)
        {
            mAvService = service;
        }

        @Override
        public SetAVTransportURI setCastAction(final ActionCallbackListener listener, final String uri, final String metadata)
        {
            return new SetAVTransportURI(mAvService, uri, metadata)
            {
                @Override
                public void success(final ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation, uri, metadata);
                }

                @Override
                public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public Play playAction(final ActionCallbackListener listener)
        {
            return new Play(mAvService)
            {
                @Override
                public void success(final ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation);
                }

                @Override
                public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public Pause pauseAction(final ActionCallbackListener listener)
        {
            return new Pause(mAvService)
            {
                @Override
                public void success(final ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation);
                }

                @Override
                public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public Stop stopAction(final ActionCallbackListener listener)
        {
            return new Stop(mAvService)
            {
                @Override
                public void success(final ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation);
                }

                @Override
                public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public Seek seekAction(final ActionCallbackListener listener, final long position)
        {
            return new Seek(mAvService, CastUtils.getStringTime(position))
            {
                @Override
                public void success(final ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation, position);
                }

                @Override
                public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public GetPositionInfo getPositionInfoAction(final ActionCallbackListener listener)
        {
            return new GetPositionInfo(mAvService)
            {
                @Override
                public void received(ActionInvocation invocation, final PositionInfo positionInfo)
                {
                    notifySuccess(listener, invocation, positionInfo);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public GetMediaInfo getMediaInfo(final ActionCallbackListener listener)
        {
            return new GetMediaInfo(mAvService)
            {
                @Override
                public void received(ActionInvocation invocation, final MediaInfo mediaInfo)
                {
                    notifySuccess(listener, invocation, mediaInfo);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public GetTransportInfo getTransportInfo(final ActionCallbackListener listener)
        {
            return new GetTransportInfo(mAvService)
            {
                @Override
                public void received(ActionInvocation invocation, TransportInfo transportInfo)
                {
                    notifySuccess(listener, invocation, transportInfo);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public SubscriptionCallback subscriptionCallback(ICastControlListener listener, EventCallbackListener eventCallbackListener)
        {
            return new AvTransportSubscription(mAvService, listener, eventCallbackListener);
        }
    }
}
