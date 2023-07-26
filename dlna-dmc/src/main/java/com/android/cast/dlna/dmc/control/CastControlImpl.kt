package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.AVServiceExecutorImpl
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.RendererServiceExecutorImpl
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser

class CastControlImpl(
    controlPoint: ControlPoint,
    device: Device<*, *, *>,
    subscriptionListener: SubscriptionListener,
) : DeviceControl {

    private val avTransportService: AVServiceExecutorImpl
    private val renderService: RendererServiceExecutorImpl

    init {
        avTransportService = AVServiceExecutorImpl(controlPoint, device.findService(DLNACastManager.SERVICE_AV_TRANSPORT))
        avTransportService.subscribe(subscriptionListener, AVTransportLastChangeParser())
        renderService = RendererServiceExecutorImpl(controlPoint, device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL))
        renderService.subscribe(subscriptionListener, RenderingControlLastChangeParser())
    }

    // ---- AvTransport ------------------------------------------
    override fun cast(uri: String, title: String, callback: ServiceActionCallback<String>?) {
        super.cast(uri, title, callback)
        avTransportService.cast(uri, title, callback)
    }

    override fun play(callback: ServiceActionCallback<String>?) {
        super.play(callback)
        avTransportService.play(callback)
    }

    override fun pause(callback: ServiceActionCallback<String>?) {
        super.pause(callback)
        avTransportService.pause(callback)
    }

    override fun seek(millSeconds: Long, callback: ServiceActionCallback<Long>?) {
        super.seek(millSeconds, callback)
        avTransportService.seek(millSeconds, callback)
    }

    override fun stop(callback: ServiceActionCallback<String>?) {
        super.stop(callback)
        avTransportService.stop(callback)
    }

    override fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?) {
        super.getMediaInfo(callback)
        avTransportService.getMediaInfo(callback)
    }

    override fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?) {
        super.getPositionInfo(callback)
        avTransportService.getPositionInfo(callback)
    }

    override fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?) {
        super.getTransportInfo(callback)
        avTransportService.getTransportInfo(callback)
    }

    // ---- Renderer ------------------------------------------
    override fun setVolume(volume: Int, callback: ServiceActionCallback<Int>?) {
        super.setVolume(volume, callback)
        renderService.setVolume(volume, callback)
    }

    override fun getVolume(callback: ServiceActionCallback<Int>?) {
        super.getVolume(callback)
        renderService.getVolume(callback)
    }

    override fun setMute(mute: Boolean, callback: ServiceActionCallback<Boolean>?) {
        super.setMute(mute, callback)
        renderService.setMute(mute, callback)
    }

    override fun isMute(callback: ServiceActionCallback<Boolean>?) {
        super.isMute(callback)
        renderService.isMute(callback)
    }
}