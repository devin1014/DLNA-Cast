package com.android.cast.dlna.dmc.control

import android.text.TextUtils
import com.android.cast.dlna.core.ICast
import com.android.cast.dlna.core.Utils.getMetadata
import com.android.cast.dlna.dmc.control.ServiceFactory.ServiceFactoryImpl
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
import org.fourthline.cling.support.model.TransportState

class ControlImpl(
    controlPoint: ControlPoint,
    private val device: Device<*, *, *>,
    private val callbackMap: Map<String, ServiceActionCallback<*>>,
    subscriptionListener: SubscriptionListener?,
) : Control {

    private val serviceFactory: ServiceFactory
    private var uri: String? = null

    init {
        serviceFactory = ServiceFactoryImpl(controlPoint, device)
        (serviceFactory.avService as BaseServiceExecutor).execute(object : SubscriptionListener {
            override fun onSubscriptionTransportStateChanged(event: TransportState) {
                subscriptionListener?.onSubscriptionTransportStateChanged(event)
            }
        })
        (serviceFactory.renderService as BaseServiceExecutor).execute(object : SubscriptionListener {
            override fun onSubscriptionTransportStateChanged(event: TransportState) {
                subscriptionListener?.onSubscriptionTransportStateChanged(event)
            }
        })
    }

    override fun cast(device: Device<*, *, *>, cast: ICast) {
        uri = cast.uri
        serviceFactory.avService.cast(object : CastEventListener {
            override fun onResponse(response: ActionResponse<String>) {
                getCallback<String>(CAST)?.onResponse(response)
            }
        }, cast.uri, getMetadata(cast))
    }

    override fun isCasting(device: Device<*, *, *>?): Boolean {
        return this.device == device
    }

    override fun isCasting(device: Device<*, *, *>?, uri: String?): Boolean {
        return if (TextUtils.isEmpty(uri)) isCasting(device) else isCasting(device) && uri != null && uri == this.uri
    }

    override fun stop() {
        serviceFactory.avService.stop(getCallback(STOP))
    }

    override fun play() {
        serviceFactory.avService.play(getCallback(PLAY))
    }

    override fun pause() {
        serviceFactory.avService.pause(getCallback(PAUSE))
    }

    override fun seekTo(millSeconds: Long) {
        serviceFactory.avService.seek(getCallback(SEEK_TO), millSeconds)
    }

    override fun setVolume(percent: Int) {
        serviceFactory.renderService.setVolume(getCallback(SET_VOLUME), percent)
    }

    override fun setMute(mute: Boolean) {
        serviceFactory.renderService.setMute(getCallback(SET_MUTE), mute)
    }

    override fun setBrightness(percent: Int) {
        serviceFactory.renderService.setBrightness(getCallback(SET_BRIGHTNESS), percent)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getCallback(action: ServiceAction): ServiceActionCallback<T>? {
        return callbackMap[action.name] as? ServiceActionCallback<T>
    }
}