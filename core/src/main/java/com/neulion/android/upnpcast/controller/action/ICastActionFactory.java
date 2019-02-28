package com.neulion.android.upnpcast.controller.action;

import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.android.upnpcast.controller.action.IAVServiceActionFactory.AvServiceActionFactory;
import com.neulion.android.upnpcast.controller.action.IRenderServiceActionFactory.RenderServiceActionFactory;
import com.neulion.android.upnpcast.device.CastDevice;

/**
 */
public interface ICastActionFactory
{
    IAVServiceActionFactory getAvService();

    IRenderServiceActionFactory getRenderService();

    // ------------------------------------------------------------------------------------------
    // Implement
    // ------------------------------------------------------------------------------------------
    class CastActionFactory implements ICastActionFactory
    {
        private IAVServiceActionFactory mAvService;

        private IRenderServiceActionFactory mRenderService;

        public CastActionFactory(CastDevice castDevice)
        {
            mAvService = new AvServiceActionFactory(castDevice.getDevice().findService(NLUpnpCastManager.SERVICE_AV_TRANSPORT));

            mRenderService = new RenderServiceActionFactory(castDevice.getDevice().findService(NLUpnpCastManager.SERVICE_RENDERING_CONTROL));
        }

        @Override
        public IAVServiceActionFactory getAvService()
        {
            return mAvService;
        }

        @Override
        public IRenderServiceActionFactory getRenderService()
        {
            return mRenderService;
        }
    }
}
