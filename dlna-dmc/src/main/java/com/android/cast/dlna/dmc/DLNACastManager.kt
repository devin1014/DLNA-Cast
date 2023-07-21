package com.android.cast.dlna.dmc

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.android.cast.dlna.core.ContentType
import com.android.cast.dlna.core.ICast
import com.android.cast.dlna.core.Level
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.dmc.QueryRequest.BrowseContentRequest
import com.android.cast.dlna.dmc.QueryRequest.MediaInfoRequest
import com.android.cast.dlna.dmc.QueryRequest.PositionInfoRequest
import com.android.cast.dlna.dmc.QueryRequest.TransportInfoRequest
import com.android.cast.dlna.dmc.QueryRequest.VolumeInfoRequest
import com.android.cast.dlna.dmc.control.CastEventListener
import com.android.cast.dlna.dmc.control.Control
import com.android.cast.dlna.dmc.control.ControlImpl
import com.android.cast.dlna.dmc.control.GetInfo
import com.android.cast.dlna.dmc.control.GetInfoListener
import com.android.cast.dlna.dmc.control.PauseEventListener
import com.android.cast.dlna.dmc.control.PlayEventListener
import com.android.cast.dlna.dmc.control.SeekToEventListener
import com.android.cast.dlna.dmc.control.ServiceAction.CAST
import com.android.cast.dlna.dmc.control.ServiceAction.PAUSE
import com.android.cast.dlna.dmc.control.ServiceAction.PLAY
import com.android.cast.dlna.dmc.control.ServiceAction.SEEK_TO
import com.android.cast.dlna.dmc.control.ServiceAction.STOP
import com.android.cast.dlna.dmc.control.ServiceActionCallback
import com.android.cast.dlna.dmc.control.StopEventListener
import com.android.cast.dlna.dmc.control.SubscriptionListener
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.message.header.STAllHeader
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.types.DeviceType
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo

/**
 *
 */
object DLNACastManager : Control, GetInfo, OnDeviceRegistryListener {

    //public static final DeviceType DEVICE_TYPE_DMR = new UDADeviceType("MediaRenderer");
    val SERVICE_AV_TRANSPORT: ServiceType = UDAServiceType("AVTransport")
    val SERVICE_RENDERING_CONTROL: ServiceType = UDAServiceType("RenderingControl")
    val SERVICE_CONNECTION_MANAGER: ServiceType = UDAServiceType("ConnectionManager")
    val SERVICE_CONTENT_DIRECTORY: ServiceType = UDAServiceType("ContentDirectory")

    private val logger = Logger.create("CastManager")
    private val deviceRegistryImpl = DeviceRegistryImpl(this)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val actionEventCallbackMap: MutableMap<String, ServiceActionCallback<*>> = LinkedHashMap()
    private var searchDeviceType: DeviceType? = null
    private var controlImpl: ControlImpl? = null
    private var upnpService: AndroidUpnpService? = null

    fun enableLog(logging: Boolean = true, level: Int = Level.V) {
        Logger.enabled = logging
        Logger.level = level
    }

    fun bindCastService(context: Context) {
        if (context is Application || context is Activity) {
            context.bindService(Intent(context, DLNACastService::class.java), serviceConnection, Service.BIND_AUTO_CREATE)
        } else {
            logger.e("bindCastService only support Application or Activity implementation.")
        }
    }

    fun unbindCastService(context: Context) {
        if (context is Application || context is Activity) {
            context.unbindService(serviceConnection)
        } else {
            logger.e("bindCastService only support Application or Activity implementation.")
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val upnpServiceBinder = iBinder as AndroidUpnpService
            if (upnpService !== upnpServiceBinder) {
                upnpService = upnpServiceBinder
                logger.i(String.format("onServiceConnected: [%s]", componentName.shortClassName))
                //logger.i(String.format("[Registry]: listener= %s, devices= %s", upnpServiceBinder.registry.listeners.size, upnpServiceBinder.registry.devices.size))
                val registry = upnpServiceBinder.registry
                // add registry listener
                val collection = registry.listeners
                if (collection == null || !collection.contains(deviceRegistryImpl)) {
                    registry.addListener(deviceRegistryImpl)
                }
                // Now add all devices to the list we already know about
                deviceRegistryImpl.setDevices(upnpServiceBinder.registry.devices)
            }
            if (mediaServer != null) {
                upnpService?.registry?.addDevice(mediaServer)
            }
            mediaServer = null
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            logger.w(String.format("[%s] onServiceDisconnected", componentName.shortClassName))
            removeRegistryListener()
        }

        override fun onBindingDied(componentName: ComponentName) {
            logger.w(String.format("[%s] onBindingDied", componentName.shortClassName))
            removeRegistryListener()
        }

        private fun removeRegistryListener() {
            upnpService?.registry?.removeListener(deviceRegistryImpl)
            upnpService = null
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- register or unregister device listener
    // -----------------------------------------------------------------------------------------
    private val lock = ByteArray(0)
    private val registerDeviceListeners: MutableList<OnDeviceRegistryListener> = ArrayList()

    fun registerDeviceListener(listener: OnDeviceRegistryListener?) {
        if (listener == null) return
        upnpService?.also { service ->
            service.registry.devices
                .filter { searchDeviceType == null || searchDeviceType == it.type }
                .forEach { device ->
                    // if some devices has been found, notify first.
                    exeActionInUIThread { listener.onDeviceAdded(device) }
                }

        }
        synchronized(lock) {
            if (!registerDeviceListeners.contains(listener)) registerDeviceListeners.add(listener)
        }
    }

    private fun exeActionInUIThread(action: Runnable) {
        if (Thread.currentThread() !== Looper.getMainLooper().thread) mainHandler.post(action)
        else action.run()
    }

    fun unregisterListener(listener: OnDeviceRegistryListener) {
        synchronized(lock) { registerDeviceListeners.remove(listener) }
    }

    override fun onDeviceAdded(device: Device<*, *, *>) {
        if (checkDeviceType(device)) {
            synchronized(lock) {
                registerDeviceListeners.forEach { listener -> listener.onDeviceAdded(device) }
            }
        }
    }

    override fun onDeviceUpdated(device: Device<*, *, *>) {
        if (checkDeviceType(device)) {
            synchronized(lock) {
                registerDeviceListeners.forEach { listener -> listener.onDeviceUpdated(device) }
            }
        }
    }

    override fun onDeviceRemoved(device: Device<*, *, *>) {
        if (checkDeviceType(device)) {
            // if this device is casting, disconnect first!
            if (controlImpl?.isCasting(device) == true) controlImpl?.stop()
            controlImpl = null
            synchronized(lock) {
                registerDeviceListeners.forEach { listener -> listener.onDeviceRemoved(device) }
            }
        }
    }

    private fun checkDeviceType(device: Device<*, *, *>): Boolean = searchDeviceType == null || searchDeviceType == device.type

    // -----------------------------------------------------------------------------------------
    // ---- MediaServer
    // -----------------------------------------------------------------------------------------
    private var mediaServer: LocalDevice? = null
    fun addMediaServer(mediaServer: LocalDevice?) {
        if (upnpService != null && mediaServer != null) {
            if (upnpService?.registry?.getDevice(mediaServer.identity.udn, true) == null) {
                upnpService?.registry?.addDevice(mediaServer)
            }
        } else {
            this.mediaServer = mediaServer
        }
    }

    fun removeMediaServer(mediaServer: LocalDevice?) {
        if (upnpService != null && mediaServer != null) {
            upnpService?.registry?.removeDevice(mediaServer)
        } else {
            this.mediaServer = null
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- search
    // -----------------------------------------------------------------------------------------
    fun search(type: DeviceType? = null, maxSeconds: Int = 60) {
        searchDeviceType = type
        upnpService?.get()?.also { service ->
            //when search device, clear all founded first.
            service.registry.removeAllRemoteDevices()
            service.controlPoint.search(type?.let { UDADeviceTypeHeader(it) } ?: STAllHeader(), maxSeconds)
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- action
    // -----------------------------------------------------------------------------------------
    override fun cast(device: Device<*, *, *>, cast: ICast) {
        // check device has been connected.
//        if (controlImpl != null) {
//            // the device is casting! should not recast and syc control status.
//            // the device is casting! should not recast and syc control status.
//            // the device is casting! should not recast and syc control status.
//            // if (mControlImpl.isCasting(device, object.getUri())) {
//            //     syc status
//            //     return;
//            // }
//            controlImpl!!.stop()
//        }
        controlImpl?.stop()
        upnpService?.let { upnpService ->
            controlImpl = ControlImpl(upnpService.controlPoint, device, actionEventCallbackMap, subscriptionListener).also {
                it.cast(device, cast)
            }
        }
    }

    override fun play() {
        controlImpl?.play()
    }

    override fun pause() {
        controlImpl?.pause()
    }

    override fun isCasting(device: Device<*, *, *>?): Boolean {
        return controlImpl?.isCasting(device) == true
    }

    override fun isCasting(device: Device<*, *, *>?, uri: String?): Boolean {
        return controlImpl?.isCasting(device, uri) == true
    }

    override fun stop() {
        controlImpl?.stop()
    }

    override fun seekTo(millSeconds: Long) {
        controlImpl?.seekTo(millSeconds)
    }

    override fun setVolume(percent: Int) {
        controlImpl?.setVolume(percent)
    }

    override fun setMute(mute: Boolean) {
        controlImpl?.setMute(mute)
    }

    override fun setBrightness(percent: Int) {
        controlImpl?.setBrightness(percent)
    }

    // -----------------------------------------------------------------------------------------
    // ---- Callback
    // -----------------------------------------------------------------------------------------
    fun registerActionCallbacks(vararg callbacks: ServiceActionCallback<*>) {
        callbacks.forEach { callback ->
            when (callback) {
                is CastEventListener -> actionEventCallbackMap[CAST.name] = callback
                is PlayEventListener -> actionEventCallbackMap[PLAY.name] = callback
                is PauseEventListener -> actionEventCallbackMap[PAUSE.name] = callback
                is StopEventListener -> actionEventCallbackMap[STOP.name] = callback
                is SeekToEventListener -> actionEventCallbackMap[SEEK_TO.name] = callback
            }
        }
    }

    fun unregisterActionCallbacks() = actionEventCallbackMap.clear()

    var subscriptionListener: SubscriptionListener? = null

    // -----------------------------------------------------------------------------------------
    // ---- query
    // -----------------------------------------------------------------------------------------
    override fun getMediaInfo(device: Device<*, *, *>, listener: GetInfoListener<MediaInfo>?) {
        upnpService?.also { service ->
            MediaInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(service.controlPoint, listener)
        }
    }

    override fun getPositionInfo(device: Device<*, *, *>, listener: GetInfoListener<PositionInfo>?) {
        upnpService?.also { service ->
            PositionInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(service.controlPoint, listener)
        }
    }

    override fun getTransportInfo(device: Device<*, *, *>, listener: GetInfoListener<TransportInfo>?) {
        upnpService?.also { service ->
            TransportInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(service.controlPoint, listener)
        }
    }

    override fun getVolumeInfo(device: Device<*, *, *>, listener: GetInfoListener<Int>?) {
        upnpService?.also { service ->
            VolumeInfoRequest(device.findService(SERVICE_RENDERING_CONTROL)).execute(service.controlPoint, listener)
        }
    }

    override fun getContent(device: Device<*, *, *>, contentType: ContentType, listener: GetInfoListener<DIDLContent>?) {
        upnpService?.also { service ->
            BrowseContentRequest(device.findService(SERVICE_CONTENT_DIRECTORY), contentType.id).execute(service.controlPoint, listener)
        }
    }
}