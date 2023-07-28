package com.android.cast.dlna.dmc.control

import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.AVServiceExecutorImpl
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.ContentServiceExecutorImpl
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.RendererServiceExecutorImpl
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser

class CastControlImpl(
    controlPoint: ControlPoint,
    device: Device<*, *, *>,
    listener: OnDeviceControlListener,
) : DeviceControl {

    private val avTransportService: AVServiceExecutorImpl
    private val renderService: RendererServiceExecutorImpl
    private val contentService: ContentServiceExecutorImpl
    var released = false

    init {
        avTransportService = AVServiceExecutorImpl(controlPoint, device.findService(DLNACastManager.SERVICE_TYPE_AV_TRANSPORT))
        avTransportService.subscribe(object : SubscriptionListener {
            override fun failed(subscriptionId: String?) {
                if (!released) listener.onDisconnected(device)
            }

            override fun established(subscriptionId: String?) {
                if (!released) listener.onConnected(device)
            }

            override fun ended(subscriptionId: String?) {
                if (!released) listener.onDisconnected(device)
            }

            override fun onReceived(subscriptionId: String?, event: EventedValue<*>) {
                if (!released) listener.onEventChanged(event)
            }
        }, AVTransportLastChangeParser())
        renderService = RendererServiceExecutorImpl(controlPoint, device.findService(DLNACastManager.SERVICE_TYPE_RENDERING_CONTROL))
        renderService.subscribe(object : SubscriptionListener {}, RenderingControlLastChangeParser())
        contentService = ContentServiceExecutorImpl(controlPoint, device.findService(DLNACastManager.SERVICE_TYPE_CONTENT_DIRECTORY))
        //TODO: check the parser
        contentService.subscribe(object : SubscriptionListener {}, AVTransportLastChangeParser())
    }

    // --------------------------------------------------------
    // ---- AvTransport ---------------------------------------
    // --------------------------------------------------------
    override fun setAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>?) {
        super.setAVTransportURI(uri, title, callback)
        avTransportService.setAVTransportURI(uri, title, callback)
    }

    override fun setNextAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>?) {
        super.setNextAVTransportURI(uri, title, callback)
        avTransportService.setNextAVTransportURI(uri, title, callback)
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

    // --------------------------------------------------------
    // ---- Renderer ------------------------------------------
    // --------------------------------------------------------
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

    // --------------------------------------------------------
    // ---- Content ------------------------------------------
    // --------------------------------------------------------
    override fun browse(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {
        super.browse(containerId, callback)
        contentService.browse(containerId, callback)
    }

    override fun search(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {
        super.search(containerId, callback)
        contentService.search(containerId, callback)
    }
}