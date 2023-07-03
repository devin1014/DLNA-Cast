package com.android.cast.dlna.dmc.control

import android.os.Handler
import android.os.Looper
import com.android.cast.dlna.core.Utils.getStringTime
import com.android.cast.dlna.dmc.action.GetBrightness
import com.android.cast.dlna.dmc.action.SetBrightness
import com.android.cast.dlna.dmc.control.ICastInterface.ISubscriptionListener
import com.android.cast.dlna.dmc.control.IServiceAction.IAVServiceAction
import com.android.cast.dlna.dmc.control.IServiceAction.IRendererServiceAction
import com.android.cast.dlna.dmc.control.IServiceAction.IServiceActionCallback
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo
import org.fourthline.cling.support.avtransport.callback.Pause
import org.fourthline.cling.support.avtransport.callback.Play
import org.fourthline.cling.support.avtransport.callback.Seek
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import org.fourthline.cling.support.avtransport.callback.Stop
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.renderingcontrol.callback.GetMute
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume
import org.fourthline.cling.support.renderingcontrol.callback.SetMute
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume

/**
 *
 */
internal abstract class BaseServiceExecutor protected constructor(
    private val controlPoint: ControlPoint,
    protected val service: Service<*, *>?
) {
    private val handler = Handler(Looper.getMainLooper())

    protected fun invalidServiceAction(actionName: String?): Boolean = service?.getAction(actionName) == null

    protected fun execute(actionCallback: ActionCallback?) {
        controlPoint.execute(actionCallback)
    }

    fun execute(subscriptionCallback: ISubscriptionListener?) {
        controlPoint.execute(CastSubscriptionCallback(service, 600, subscriptionCallback))
    }

    protected fun <T> notifySuccess(listener: IServiceActionCallback<T>?, t: T) {
        listener?.let { notify { it.onSuccess(t) } }
    }

    protected fun notifyFailure(listener: IServiceActionCallback<*>?, errMsg: String?) {
        listener?.let { notify { it.onFailed(errMsg ?: "error") } }
    }

    private fun notify(runnable: Runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(runnable)
        } else {
            runnable.run()
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Implement
    // ---------------------------------------------------------------------------------------------------------
    internal class AVServiceExecutorImpl(
        controlPoint: ControlPoint,
        service: Service<*, *>?
    ) : BaseServiceExecutor(controlPoint, service), IAVServiceAction {

        override fun cast(listener: IServiceActionCallback<String>?, uri: String, metadata: String?) {
            if (invalidServiceAction("SetAVTransportURI")) return
            execute(object : SetAVTransportURI(service, uri, metadata) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, uri)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun play(listener: IServiceActionCallback<Void?>?) {
            if (invalidServiceAction("Play")) return
            execute(object : Play(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, null)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun pause(listener: IServiceActionCallback<Void?>?) {
            if (invalidServiceAction("Pause")) return
            execute(object : Pause(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, null)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun stop(listener: IServiceActionCallback<Void?>?) {
            if (invalidServiceAction("Stop")) return
            execute(object : Stop(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, null)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun seek(listener: IServiceActionCallback<Long>?, position: Long) {
            if (invalidServiceAction("Seek")) return
            execute(object : Seek(service, getStringTime(position)) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, position)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun getPositionInfo(listener: IServiceActionCallback<PositionInfo>?) {
            if (invalidServiceAction("GetPositionInfo")) return
            execute(object : GetPositionInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, positionInfo: PositionInfo) {
                    notifySuccess(listener, positionInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun getMediaInfo(listener: IServiceActionCallback<MediaInfo>?) {
            if (invalidServiceAction("GetMediaInfo")) return
            execute(object : GetMediaInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, mediaInfo: MediaInfo) {
                    notifySuccess(listener, mediaInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun getTransportInfo(listener: IServiceActionCallback<TransportInfo>?) {
            if (invalidServiceAction("GetTransportInfo")) return
            execute(object : GetTransportInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, transportInfo: TransportInfo) {
                    notifySuccess(listener, transportInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Implement
    // ---------------------------------------------------------------------------------------------------------
    internal class RendererServiceExecutorImpl(
        controlPoint: ControlPoint,
        service: Service<*, *>?
    ) : BaseServiceExecutor(controlPoint, service), IRendererServiceAction {

        override fun setVolume(listener: IServiceActionCallback<Int>?, volume: Int) {
            if (invalidServiceAction("SetVolume")) return
            execute(object : SetVolume(service, volume.toLong()) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, volume)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun getVolume(listener: IServiceActionCallback<Int>?) {
            if (invalidServiceAction("GetVolume")) return
            execute(object : GetVolume(service) {
                override fun received(invocation: ActionInvocation<*>?, currentVolume: Int) {
                    notifySuccess(listener, currentVolume)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun setMute(listener: IServiceActionCallback<Boolean>?, mute: Boolean) {
            if (invalidServiceAction("SetMute")) return
            execute(object : SetMute(service, mute) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, mute)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun isMute(listener: IServiceActionCallback<Boolean>?) {
            if (invalidServiceAction("GetMute")) return
            execute(object : GetMute(service) {
                override fun received(invocation: ActionInvocation<*>?, currentMute: Boolean) {
                    notifySuccess(listener, currentMute)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun setBrightness(listener: IServiceActionCallback<Int>?, percent: Int) {
            if (invalidServiceAction("SetBrightness")) return
            execute(object : SetBrightness(service!!, percent.toLong()) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifySuccess(listener, percent)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }

        override fun getBrightness(listener: IServiceActionCallback<Int>?) {
            if (invalidServiceAction("GetBrightness")) return
            execute(object : GetBrightness(service!!) {
                override fun success(invocation: ActionInvocation<*>) {
                    super.success(invocation)
                }

                override fun received(invocation: ActionInvocation<*>?, brightness: Int) {
                    notifySuccess(listener, brightness)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    notifyFailure(listener, defaultMsg)
                }
            })
        }
    }
}