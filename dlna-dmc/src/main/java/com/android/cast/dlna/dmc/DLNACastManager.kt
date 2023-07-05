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
import com.android.cast.dlna.core.Utils.toHexString
import com.android.cast.dlna.dmc.QueryRequest.BrowseContentRequest
import com.android.cast.dlna.dmc.QueryRequest.MediaInfoRequest
import com.android.cast.dlna.dmc.QueryRequest.PositionInfoRequest
import com.android.cast.dlna.dmc.QueryRequest.TransportInfoRequest
import com.android.cast.dlna.dmc.QueryRequest.VolumeInfoRequest
import com.android.cast.dlna.dmc.control.ControlImpl
import com.android.cast.dlna.dmc.control.ICastInterface.CastEventListener
import com.android.cast.dlna.dmc.control.ICastInterface.GetInfoListener
import com.android.cast.dlna.dmc.control.ICastInterface.IControl
import com.android.cast.dlna.dmc.control.ICastInterface.IGetInfo
import com.android.cast.dlna.dmc.control.ICastInterface.ISubscriptionListener
import com.android.cast.dlna.dmc.control.ICastInterface.PauseEventListener
import com.android.cast.dlna.dmc.control.ICastInterface.PlayEventListener
import com.android.cast.dlna.dmc.control.ICastInterface.SeekToEventListener
import com.android.cast.dlna.dmc.control.ICastInterface.StopEventListener
import com.android.cast.dlna.dmc.control.IServiceAction.IServiceActionCallback
import com.android.cast.dlna.dmc.control.IServiceAction.ServiceAction.CAST
import com.android.cast.dlna.dmc.control.IServiceAction.ServiceAction.PAUSE
import com.android.cast.dlna.dmc.control.IServiceAction.ServiceAction.PLAY
import com.android.cast.dlna.dmc.control.IServiceAction.ServiceAction.SEEK_TO
import com.android.cast.dlna.dmc.control.IServiceAction.ServiceAction.STOP
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
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
import java.util.logging.Level

/**
 *
 */
object DLNACastManager : IControl, IGetInfo, OnDeviceRegistryListener {

    //public static final DeviceType DEVICE_TYPE_DMR = new UDADeviceType("MediaRenderer");
    val SERVICE_AV_TRANSPORT: ServiceType = UDAServiceType("AVTransport")
    val SERVICE_RENDERING_CONTROL: ServiceType = UDAServiceType("RenderingControl")
    val SERVICE_CONNECTION_MANAGER: ServiceType = UDAServiceType("ConnectionManager")
    val SERVICE_CONTENT_DIRECTORY: ServiceType = UDAServiceType("ContentDirectory")

    var service: AndroidUpnpService? = null
        private set

    private val deviceRegistryImpl = DeviceRegistryImpl(this)
    private val handler = Handler(Looper.getMainLooper())
    private val actionEventCallbackMap: MutableMap<String, IServiceActionCallback<*>> = LinkedHashMap()
    private var searchDeviceType: DeviceType? = null
    private var controlImpl: ControlImpl? = null

    @JvmOverloads
    fun enableLog(
        formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder().build(),
        level: Level = Level.FINEST
    ) {
        java.util.logging.Logger.getLogger("org.fourthline.cling").level = level
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
    }

    fun bindCastService(context: Context) {
        if (context is Application || context is Activity) {
            context.bindService(Intent(context, DLNACastService::class.java), serviceConnection, Service.BIND_AUTO_CREATE)
        } else {
            Logger.e("bindCastService only support Application or Activity implementation.")
        }
    }

    fun unbindCastService(context: Context) {
        if (context is Application || context is Activity) {
            context.unbindService(serviceConnection)
        } else {
            Logger.e("bindCastService only support Application or Activity implementation.")
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val upnpService = iBinder as AndroidUpnpService
            if (service !== upnpService) {
                service = upnpService
                Logger.i(String.format("[%s] connected %s", componentName.shortClassName, iBinder.javaClass.name))
                Logger.i(String.format("[UpnpService]: %s@0x%s", upnpService.get().javaClass.name, toHexString(upnpService.get().hashCode())))
                Logger.i(String.format("[Registry]: listener=%s, devices=%s", upnpService.registry.listeners.size, upnpService.registry.devices.size))
                val registry = upnpService.registry
                // add registry listener
                val collection = registry.listeners
                if (collection == null || !collection.contains(deviceRegistryImpl)) {
                    registry.addListener(deviceRegistryImpl)
                }
                // Now add all devices to the list we already know about
                deviceRegistryImpl.setDevices(upnpService.registry.devices)
            }
            if (mediaServer != null) {
                service!!.registry.addDevice(mediaServer)
            }
            mediaServer = null
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Logger.w(String.format("[%s] onServiceDisconnected", componentName.shortClassName))
            removeRegistryListener()
        }

        override fun onBindingDied(componentName: ComponentName) {
            Logger.e(String.format("[%s] onBindingDied", componentName.className))
            removeRegistryListener()
        }

        private fun removeRegistryListener() {
            if (service != null) {
                service!!.registry.removeListener(deviceRegistryImpl)
            }
            service = null
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- register or unregister device listener
    // -----------------------------------------------------------------------------------------
    private val lock = ByteArray(0)
    private val registerDeviceListeners: MutableList<OnDeviceRegistryListener> = ArrayList()

    fun registerDeviceListener(listener: OnDeviceRegistryListener?) {
        if (listener == null) return
        if (service != null) {
            val devices: Collection<Device<*, *, *>>? = if (searchDeviceType == null) {
                service!!.registry.devices
            } else {
                service!!.registry.getDevices(searchDeviceType)
            }
            // if some devices has been found, notify first.
            if (!devices.isNullOrEmpty()) {
                exeActionInUIThread { for (device in devices) listener.onDeviceAdded(device) }
            }
        }
        synchronized(lock) {
            if (!registerDeviceListeners.contains(listener)) {
                registerDeviceListeners.add(listener)
            }
        }
    }

    private fun exeActionInUIThread(action: Runnable?) {
        if (action != null) {
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                handler.post(action)
            } else {
                action.run()
            }
        }
    }

    fun unregisterListener(listener: OnDeviceRegistryListener) {
        synchronized(lock) { registerDeviceListeners.remove(listener) }
    }

    override fun onDeviceAdded(device: Device<*, *, *>) {
        if (checkDeviceType(device)) {
            synchronized(lock) { for (listener in registerDeviceListeners) listener.onDeviceAdded(device) }
        }
    }

    override fun onDeviceUpdated(device: Device<*, *, *>) {
        if (checkDeviceType(device)) {
            synchronized(lock) { for (listener in registerDeviceListeners) listener.onDeviceUpdated(device) }
        }
    }

    override fun onDeviceRemoved(device: Device<*, *, *>) {
        if (checkDeviceType(device)) {
            // if this device is casting, disconnect first!
            if (controlImpl?.isCasting(device) == true) {
                controlImpl?.stop()
            }
            controlImpl = null
            synchronized(lock) { for (listener in registerDeviceListeners) listener.onDeviceRemoved(device) }
        }
    }

    private fun checkDeviceType(device: Device<*, *, *>): Boolean {
        return searchDeviceType == null || searchDeviceType == device.type
    }

    // -----------------------------------------------------------------------------------------
    // ---- MediaServer
    // -----------------------------------------------------------------------------------------
    private var mediaServer: LocalDevice? = null
    fun addMediaServer(mediaServer: LocalDevice?) {
        if (service != null && mediaServer != null) {
            if (service!!.registry.getDevice(mediaServer.identity.udn, true) == null) {
                service!!.registry.addDevice(mediaServer)
            }
        } else {
            this.mediaServer = mediaServer
        }
    }

    fun removeMediaServer(mediaServer: LocalDevice?) {
        if (service != null && mediaServer != null) {
            service!!.registry.removeDevice(mediaServer)
        } else {
            this.mediaServer = null
        }
    }

    // -----------------------------------------------------------------------------------------
    // ---- search
    // -----------------------------------------------------------------------------------------
    fun search(type: DeviceType?, maxSeconds: Int) {
        searchDeviceType = type
        if (service != null) {
            val upnpService = service!!.get()
            //when search device, clear all founded first.
            upnpService.registry.removeAllRemoteDevices()
            upnpService.controlPoint.search(type?.let { UDADeviceTypeHeader(it) } ?: STAllHeader(), maxSeconds)
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
        service?.let { upnpService ->
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

    override fun seekTo(position: Long) {
        controlImpl?.seekTo(position)
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
    fun registerActionCallbacks(vararg callbacks: IServiceActionCallback<*>) {
        _innerRegisterActionCallback(*callbacks)
    }

    fun unregisterActionCallbacks() {
        if (actionEventCallbackMap.size > 0) {
            actionEventCallbackMap.clear()
        }
    }

    private fun _innerRegisterActionCallback(vararg callbacks: IServiceActionCallback<*>) {
        if (callbacks.isNotEmpty()) {
            for (callback in callbacks) {
                when (callback) {
                    is CastEventListener -> actionEventCallbackMap[CAST.name] = callback
                    is PlayEventListener -> actionEventCallbackMap[PLAY.name] = callback
                    is PauseEventListener -> actionEventCallbackMap[PAUSE.name] = callback
                    is StopEventListener -> actionEventCallbackMap[STOP.name] = callback
                    is SeekToEventListener -> actionEventCallbackMap[SEEK_TO.name] = callback
                }
            }
        }
    }

    private var subscriptionListener: ISubscriptionListener? = null
    fun registerSubscriptionListener(listener: ISubscriptionListener?) {
        subscriptionListener = listener
    }

    // -----------------------------------------------------------------------------------------
    // ---- query
    // -----------------------------------------------------------------------------------------
    override fun getMediaInfo(device: Device<*, *, *>, listener: GetInfoListener<MediaInfo>?) {
        MediaInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(service!!.controlPoint, listener)
    }

    override fun getPositionInfo(device: Device<*, *, *>, listener: GetInfoListener<PositionInfo>?) {
        PositionInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(service!!.controlPoint, listener)
    }

    override fun getTransportInfo(device: Device<*, *, *>, listener: GetInfoListener<TransportInfo>?) {
        TransportInfoRequest(device.findService(SERVICE_AV_TRANSPORT)).execute(service!!.controlPoint, listener)
    }

    override fun getVolumeInfo(device: Device<*, *, *>, listener: GetInfoListener<Int>?) {
        VolumeInfoRequest(device.findService(SERVICE_RENDERING_CONTROL)).execute(service!!.controlPoint, listener)
    }

    override fun getContent(device: Device<*, *, *>, contentType: ContentType, listener: GetInfoListener<DIDLContent>?) {
        BrowseContentRequest(device.findService(SERVICE_CONTENT_DIRECTORY), contentType.id).execute(service!!.controlPoint, listener)
    }
}