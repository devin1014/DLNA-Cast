package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.AVServiceExecutorImpl
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.RendererServiceExecutorImpl
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Device

/**
 *
 */
internal interface ServiceFactory {
    val avService: AVServiceAction
    val renderService: RendererServiceAction

    // ------------------------------------------------------------------------------------------
    // Implement
    // ------------------------------------------------------------------------------------------
    class ServiceFactoryImpl(controlPoint: ControlPoint, device: Device<*, *, *>) : ServiceFactory {
        private val avAction: AVServiceAction
        private val renderAction: RendererServiceAction

        init {
            val avService = device.findService(DLNACastManager.SERVICE_AV_TRANSPORT)
            avAction = AVServiceExecutorImpl(controlPoint, avService)
            val rendererService = device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL)
            renderAction = RendererServiceExecutorImpl(controlPoint, rendererService)
        }

        override val avService: AVServiceAction = avAction
        override val renderService: RendererServiceAction = renderAction
    }
}