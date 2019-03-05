package com.liuwei.android.upnpcast.controller.action;

import com.liuwei.android.upnpcast.controller.BaseCastEventSubscription.EventCallbackListener;
import com.liuwei.android.upnpcast.controller.BaseCastEventSubscription.RenderSubscription;
import com.liuwei.android.upnpcast.controller.ICastControlListener;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

/**
 */
public interface IRenderServiceActionFactory
{
    SetVolume setVolumeAction(ActionCallbackListener listener, final int volume);

    GetVolume getVolumeAction(ActionCallbackListener listener);

    SetMute setMuteAction(ActionCallbackListener listener, boolean mute);

    GetMute getMuteAction(ActionCallbackListener listener);

    SetBrightness setBrightnessAction(ActionCallbackListener listener, final int percent);

    GetBrightness getBrightnessAction(ActionCallbackListener listener);

    SubscriptionCallback subscriptionCallback(ICastControlListener listener, EventCallbackListener eventCallbackListener);

    // ---------------------------------------------------------------------------------------------------------
    // Implement
    // ---------------------------------------------------------------------------------------------------------
    class RenderServiceActionFactory extends BaseServiceActionFactory implements IRenderServiceActionFactory
    {
        private final Service mRenderService;

        public RenderServiceActionFactory(Service service)
        {
            mRenderService = service;
        }

        @Override
        public SetVolume setVolumeAction(final ActionCallbackListener listener, final int volume)
        {
            return new SetVolume(mRenderService, volume)
            {
                @Override
                public void success(ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation, volume);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public GetVolume getVolumeAction(final ActionCallbackListener listener)
        {
            return new GetVolume(mRenderService)
            {
                @Override
                public void received(ActionInvocation invocation, final int currentVolume)
                {
                    notifySuccess(listener, invocation, currentVolume);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public SetMute setMuteAction(final ActionCallbackListener listener, final boolean mute)
        {
            return new SetMute(mRenderService, mute)
            {
                @Override
                public void success(ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation, mute);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public GetMute getMuteAction(final ActionCallbackListener listener)
        {
            return new GetMute(mRenderService)
            {
                @Override
                public void received(ActionInvocation invocation, final boolean currentMute)
                {
                    notifySuccess(listener, invocation, currentMute);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public SetBrightness setBrightnessAction(final ActionCallbackListener listener, final int percent)
        {
            return new SetBrightness(mRenderService, percent)
            {
                @Override
                public void success(final ActionInvocation invocation)
                {
                    notifySuccess(listener, invocation);
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
                {
                    notifyFailure(listener, invocation, operation, defaultMsg);
                }
            };
        }

        @Override
        public GetBrightness getBrightnessAction(final ActionCallbackListener listener)
        {
            return new GetBrightness(mRenderService)
            {
                @Override
                public void received(ActionInvocation invocation, final int brightness)
                {
                    notifySuccess(listener, invocation, brightness);
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
            return new RenderSubscription(mRenderService, listener, eventCallbackListener);
        }
    }
}
