package com.android.cast.dlna.dmc.control

import android.text.TextUtils
import com.android.cast.dlna.core.ICast
import com.android.cast.dlna.core.Utils.getMetadata
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.AVServiceExecutorImpl
import com.android.cast.dlna.dmc.control.BaseServiceExecutor.RendererServiceExecutorImpl
import com.android.cast.dlna.dmc.control.ServiceAction.CAST
import com.android.cast.dlna.dmc.control.ServiceAction.PAUSE
import com.android.cast.dlna.dmc.control.ServiceAction.PLAY
import com.android.cast.dlna.dmc.control.ServiceAction.SEEK_TO
import com.android.cast.dlna.dmc.control.ServiceAction.SET_BRIGHTNESS
import com.android.cast.dlna.dmc.control.ServiceAction.SET_MUTE
import com.android.cast.dlna.dmc.control.ServiceAction.SET_VOLUME
import com.android.cast.dlna.dmc.control.ServiceAction.STOP
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser

class CastControlImpl(
    controlPoint: ControlPoint,
    private val device: Device<*, *, *>,
    private val callbackMap: Map<String, ServiceActionCallback<*>>,
    subscriptionListener: SubscriptionListener?,
) : CastControl {

    private val avTransportService: AvTransportServiceAction
    private val renderService: RendererServiceAction

    private var uri: String? = null

    init {
        avTransportService = AVServiceExecutorImpl(controlPoint, device.findService(DLNACastManager.SERVICE_AV_TRANSPORT))

        (avTransportService as BaseServiceExecutor).subscribe(
            subscriptionCallback = object : SubscriptionListener {
                override fun onSubscriptionTransportStateChanged(event: EventedValue<*>) {
                    subscriptionListener?.onSubscriptionTransportStateChanged(event)
                }
            },
            lastChangeParser = AVTransportLastChangeParser()
        )

        renderService = RendererServiceExecutorImpl(controlPoint, device.findService(DLNACastManager.SERVICE_RENDERING_CONTROL))

        (renderService as BaseServiceExecutor).subscribe(
            subscriptionCallback = object : SubscriptionListener {
                override fun onSubscriptionTransportStateChanged(event: EventedValue<*>) {
                    subscriptionListener?.onSubscriptionTransportStateChanged(event)
                }
            },
            lastChangeParser = RenderingControlLastChangeParser()
        )
    }

    override fun cast(device: Device<*, *, *>, cast: ICast) {
        uri = cast.uri
        avTransportService.cast(cast.uri, getMetadata(cast), object : CastEventListener {
            override fun onResponse(response: ActionResponse<String>) {
                getCallback<String>(CAST)?.onResponse(response)
            }
        })
    }

    override fun isCasting(device: Device<*, *, *>?): Boolean {
        return this.device == device
    }

    override fun isCasting(device: Device<*, *, *>?, uri: String?): Boolean {
        return if (TextUtils.isEmpty(uri)) isCasting(device) else isCasting(device) && uri != null && uri == this.uri
    }

    override fun stop() {
        avTransportService.stop(getCallback(STOP))
    }

    override fun play() {
        avTransportService.play(getCallback(PLAY))
    }

    override fun pause() {
        avTransportService.pause(getCallback(PAUSE))
    }

    override fun seekTo(millSeconds: Long) {
        avTransportService.seek(millSeconds, getCallback(SEEK_TO))
    }

    override fun setVolume(percent: Int) {
        renderService.setVolume(percent, getCallback(SET_VOLUME))
    }

    override fun setMute(mute: Boolean) {
        renderService.setMute(mute, getCallback(SET_MUTE))
    }

    override fun setBrightness(percent: Int) {
        renderService.setBrightness(percent, getCallback(SET_BRIGHTNESS))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getCallback(action: ServiceAction): ServiceActionCallback<T>? {
        return callbackMap[action.name] as? ServiceActionCallback<T>
    }
}