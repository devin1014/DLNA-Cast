package com.android.cast.dlna.controller.action;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.controller.action.IAVServiceActionFactory.AvServiceActionFactory;
import com.android.cast.dlna.controller.action.IRenderServiceActionFactory.RenderServiceActionFactory;

import org.fourthline.cling.model.meta.Device;

/**
 *
 */
public interface ICastActionFactory {
    IAVServiceActionFactory getAvService();

    IRenderServiceActionFactory getRenderService();

    // ------------------------------------------------------------------------------------------
    // Implement
    // ------------------------------------------------------------------------------------------
    final class CastActionFactory implements ICastActionFactory {
        private final IAVServiceActionFactory mAvService;
        private final IRenderServiceActionFactory mRenderService;

        public CastActionFactory(Device<?, ?, ?> device) {
            mAvService = new AvServiceActionFactory(device.findService(DLNACastManager.SERVICE_AV_TRANSPORT));
            mRenderService = new RenderServiceActionFactory(device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL));
        }

        @Override
        public IAVServiceActionFactory getAvService() {
            return mAvService;
        }

        @Override
        public IRenderServiceActionFactory getRenderService() {
            return mRenderService;
        }
    }
}
