package com.android.cast.dlna.dmc

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.android.cast.dlna.dmc.control.ICastInterface.GetInfoListener
import com.orhanobut.logger.Logger
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.model.BrowseFlag.METADATA
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume

internal abstract class QueryRequest<T>(protected val service: Service<*, *>?) {

    private var listener: GetInfoListener<T>? = null
    private val handler = Handler(Looper.getMainLooper())

    protected abstract val actionName: String?
    protected abstract val action: ActionCallback?

    protected fun setResult(t: T) {
        listener?.let { l ->
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                handler.post { l.onGetInfoResult(t, null) }
            } else {
                l.onGetInfoResult(t, null)
            }
        }
    }

    protected fun setError(errorMsg: String?) {
        Logger.e(errorMsg ?: "error")
        listener?.let { l ->
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                handler.post { l.onGetInfoResult(null, errorMsg ?: "error") }
            } else {
                l.onGetInfoResult(null, errorMsg ?: "error")
            }
        }
    }

    fun execute(point: ControlPoint, listener: GetInfoListener<T>?) {
        this.listener = listener
        if (TextUtils.isEmpty(actionName)) {
            setError("not find action name!")
            return
        } else if (service == null) {
            setError("the service is NULL!")
            return
        } else if (service.getAction(actionName) == null) {
            setError(String.format("this service not support '%s' action.", actionName))
            return
        }
        val actionCallback = action
        if (actionCallback != null) {
            point.execute(action)
        } else {
            setError("this service action is NULL!")
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ---- MediaInfo
    // ---------------------------------------------------------------------------------------------
    internal class MediaInfoRequest(service: Service<*, *>?) : QueryRequest<MediaInfo>(service) {
        override val actionName: String = "GetMediaInfo"
        override val action: ActionCallback?
            get() = if (service != null) object : GetMediaInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, mediaInfo: MediaInfo) {
                    setResult(mediaInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    setError(defaultMsg)
                }
            } else null
    }

    // ---------------------------------------------------------------------------------------------
    // ---- PositionInfo
    // ---------------------------------------------------------------------------------------------
    internal class PositionInfoRequest(service: Service<*, *>) : QueryRequest<PositionInfo>(service) {
        override val actionName: String = "GetPositionInfo"

        override val action: ActionCallback?
            get() = if (service != null) object : GetPositionInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, positionInfo: PositionInfo) {
                    setResult(positionInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    setError(defaultMsg)
                }
            } else null
    }

    // ---------------------------------------------------------------------------------------------
    // ---- TransportInfo
    // ---------------------------------------------------------------------------------------------
    internal class TransportInfoRequest(service: Service<*, *>) : QueryRequest<TransportInfo>(service) {
        override val actionName: String = "GetTransportInfo"
        override val action: ActionCallback?
            get() = if (service != null) object : GetTransportInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, transportInfo: TransportInfo) {
                    setResult(transportInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    setError(defaultMsg)
                }
            } else null
    }

    // ---------------------------------------------------------------------------------------------
    // ---- VolumeInfo
    // ---------------------------------------------------------------------------------------------
    internal class VolumeInfoRequest(service: Service<*, *>) : QueryRequest<Int>(service) {
        override val actionName: String = "GetVolume"
        override val action: ActionCallback?
            get() = if (service != null) object : GetVolume(service) {
                override fun received(actionInvocation: ActionInvocation<*>?, currentVolume: Int) {
                    setResult(currentVolume)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    setError(defaultMsg)
                }
            } else null
    }

    // ---------------------------------------------------------------------------------------------
    // ---- Browse
    // ---------------------------------------------------------------------------------------------
    internal class BrowseContentRequest(service: Service<*, *>, private val containId: String) : QueryRequest<DIDLContent>(service) {
        override val actionName: String = "Browse"
        override val action: ActionCallback?
            get() = if (service != null) object : Browse(service, containId, METADATA) {
                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse, defaultMsg: String) {
                    setError(defaultMsg)
                }

                override fun received(actionInvocation: ActionInvocation<*>?, didl: DIDLContent) {
                    setResult(didl)
                }

                override fun updateStatus(status: Status) {}
            } else null
    }
}