package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.AVServiceExecutorImpl
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.RendererServiceExecutorImpl
import com.android.cast.dlna.dmc.control.IServiceAction.IAVServiceAction
import com.android.cast.dlna.dmc.control.IServiceAction.IRendererServiceAction
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Device

/**
 *
 */
internal interface IServiceFactory {
    val avService: IAVServiceAction
    val renderService: IRendererServiceAction

    // ------------------------------------------------------------------------------------------
    // Implement
    // ------------------------------------------------------------------------------------------
    class ServiceFactoryImpl(controlPoint: ControlPoint, device: Device<*, *, *>) : IServiceFactory {
        private val avAction: IAVServiceAction
        private val renderAction: IRendererServiceAction

        init {
            val avService = device.findService(DLNACastManager.SERVICE_AV_TRANSPORT)
            avAction = AVServiceExecutorImpl(controlPoint, avService)
            val rendererService = device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL)
            renderAction = RendererServiceExecutorImpl(controlPoint, rendererService)
        }

        override val avService: IAVServiceAction = avAction
        override val renderService: IRendererServiceAction = renderAction
    }
}