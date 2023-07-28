package com.android.cast.dlna.dmc.control

import android.os.Handler
import android.os.Looper
import com.android.cast.dlna.core.Utils.getStringTime
import com.android.cast.dlna.dmc.control.action.SetNextAVTransportURI
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
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.contentdirectory.callback.Search
import org.fourthline.cling.support.lastchange.LastChangeParser
import org.fourthline.cling.support.model.BrowseFlag.METADATA
import org.fourthline.cling.support.model.DIDLContent
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
internal abstract class BaseServiceExecutor(
    private val controlPoint: ControlPoint,
    protected val service: Service<*, *>?,
) {
    private val handler = Handler(Looper.getMainLooper())

    protected fun invalidServiceAction(actionName: String?): Boolean = service?.getAction(actionName) == null

    protected fun executeAction(actionCallback: ActionCallback?) {
        controlPoint.execute(actionCallback)
    }

    fun subscribe(subscriptionCallback: SubscriptionListener, lastChangeParser: LastChangeParser) {
        controlPoint.execute(CastSubscriptionCallback(service, callback = subscriptionCallback, lastChangeParser = lastChangeParser))
    }

    protected fun <T> notifyResponse(listener: ServiceActionCallback<T>?, result: T? = null, exception: String? = null) {
        listener?.let { l ->
            notify { l.onResponse(ActionResponse(result, exception)) }
        }
    }

    private fun notify(runnable: Runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(runnable)
        } else {
            runnable.run()
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // AvService
    // ---------------------------------------------------------------------------------------------------------
    internal class AVServiceExecutorImpl(
        controlPoint: ControlPoint,
        service: Service<*, *>?,
    ) : BaseServiceExecutor(controlPoint, service), AvTransportServiceAction {
        override fun setAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>?) {
            super.setAVTransportURI(uri, title, callback)
            if (invalidServiceAction("SetAVTransportURI")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            val metadata = MetadataUtils.create(uri, title)
            getLogger()?.i("setAVTransportURI: $metadata")
            executeAction(object : SetAVTransportURI(service, uri, metadata) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = uri)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "cast failed.")
                }
            })
        }

        override fun setNextAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<String>?) {
            super.setNextAVTransportURI(uri, title, callback)
            if (invalidServiceAction("SetNextAVTransportURI")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            val metadata = MetadataUtils.create(uri, title)
            getLogger()?.i("setNextAVTransportURI: $metadata")
            executeAction(object : SetNextAVTransportURI(service = service, uri = uri, metadata = metadata) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = uri)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "cast failed.")
                }
            })
        }

        override fun play(callback: ServiceActionCallback<String>?) {
            super.play(callback)
            if (invalidServiceAction("Play")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : Play(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = "Play")
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "play failed.")
                }
            })
        }

        override fun pause(callback: ServiceActionCallback<String>?) {
            super.pause(callback)
            if (invalidServiceAction("Pause")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : Pause(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = "Pause")
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "pause failed.")
                }
            })
        }

        override fun stop(callback: ServiceActionCallback<String>?) {
            super.stop(callback)
            if (invalidServiceAction("Stop")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : Stop(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = "Stop")
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "stop failed.")
                }
            })
        }

        override fun seek(millSeconds: Long, callback: ServiceActionCallback<Long>?) {
            super.seek(millSeconds, callback)
            if (invalidServiceAction("Seek")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : Seek(service, getStringTime(millSeconds)) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = millSeconds)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "seek failed.")
                }
            })
        }

        override fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?) {
            if (invalidServiceAction("GetPositionInfo")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : GetPositionInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, positionInfo: PositionInfo) {
                    notifyResponse(callback, result = positionInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "getPosition failed.")
                }
            })
        }

        override fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?) {
            if (invalidServiceAction("GetMediaInfo")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : GetMediaInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, mediaInfo: MediaInfo) {
                    notifyResponse(callback, result = mediaInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "getMedia failed.")
                }
            })
        }

        override fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?) {
            if (invalidServiceAction("GetTransportInfo")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : GetTransportInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, transportInfo: TransportInfo) {
                    notifyResponse(callback, result = transportInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "getTransport failed.")
                }
            })
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // RendererService
    // ---------------------------------------------------------------------------------------------------------
    internal class RendererServiceExecutorImpl(
        controlPoint: ControlPoint,
        service: Service<*, *>?,
    ) : BaseServiceExecutor(controlPoint, service), RendererServiceAction {
        override fun setVolume(volume: Int, callback: ServiceActionCallback<Int>?) {
            super.setVolume(volume, callback)
            if (invalidServiceAction("SetVolume")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : SetVolume(service, volume.toLong()) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = null)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "setVolume failed.")
                }
            })
        }

        override fun getVolume(callback: ServiceActionCallback<Int>?) {
            if (invalidServiceAction("GetVolume")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : GetVolume(service) {
                override fun received(invocation: ActionInvocation<*>?, currentVolume: Int) {
                    notifyResponse(callback, result = currentVolume)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "getVolume failed.")
                }
            })
        }

        override fun setMute(mute: Boolean, callback: ServiceActionCallback<Boolean>?) {
            super.setMute(mute, callback)
            if (invalidServiceAction("SetMute")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : SetMute(service, mute) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(callback, result = mute)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "setMute failed.")
                }
            })
        }

        override fun isMute(callback: ServiceActionCallback<Boolean>?) {
            if (invalidServiceAction("GetMute")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : GetMute(service) {
                override fun received(invocation: ActionInvocation<*>?, currentMute: Boolean) {
                    notifyResponse(callback, result = currentMute)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "isMute failed.")
                }
            })
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // ContentService
    // ---------------------------------------------------------------------------------------------------------
    internal class ContentServiceExecutorImpl(
        controlPoint: ControlPoint,
        service: Service<*, *>?,
    ) : BaseServiceExecutor(controlPoint, service), ContentServiceAction {
        override fun browse(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {
            if (invalidServiceAction("Browse")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : Browse(service, containerId, METADATA, "*", 0, 99) {
                override fun received(actionInvocation: ActionInvocation<out Service<*, *>>?, didl: DIDLContent?) {
                    notifyResponse(callback, result = didl)
                }

                override fun updateStatus(status: Status?) {}

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "browse failed.")
                }
            })
        }

        override fun search(containerId: String, callback: ServiceActionCallback<DIDLContent>?) {
            if (invalidServiceAction("Search")) {
                notifyResponse(callback, exception = "service not support this action.")
                return
            }
            executeAction(object : Search(service, containerId, "", "*", 0, 99) {
                override fun received(actionInvocation: ActionInvocation<out Service<*, *>>?, didl: DIDLContent?) {
                    notifyResponse(callback, result = didl)
                }

                override fun updateStatus(status: Status?) {}

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(callback, exception = defaultMsg ?: "search failed.")
                }
            })
        }
    }
}

class ActionResponse<T>(val data: T?, val exception: String?) {
    val success: Boolean = exception.isNullOrBlank()
}