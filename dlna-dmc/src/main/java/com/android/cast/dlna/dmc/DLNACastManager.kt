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
import com.android.cast.dlna.core.Level
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.dmc.control.CastControlImpl
import com.android.cast.dlna.dmc.control.DeviceControl
import com.android.cast.dlna.dmc.control.EmptyDeviceControl
import com.android.cast.dlna.dmc.control.OnDeviceControlListener
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.message.header.STAllHeader
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.types.DeviceType
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDAServiceType

/**
 *
 */
object DLNACastManager : OnDeviceRegistryListener {

    val DEVICE_TYPE_MEDIA_RENDERER = UDADeviceType("MediaRenderer")

    val SERVICE_AV_TRANSPORT: ServiceType = UDAServiceType("AVTransport")
    val SERVICE_RENDERING_CONTROL: ServiceType = UDAServiceType("RenderingControl")
    val SERVICE_CONTENT_DIRECTORY: ServiceType = UDAServiceType("ContentDirectory")

    private val logger = Logger.create("CastManager")
    private val deviceRegistryImpl = DeviceRegistryImpl(this)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var searchDeviceType: DeviceType? = null
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
            //TODO:check
//            if (controlImpl?.isCasting(device) == true) controlImpl?.stop()
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

    fun search(type: DeviceType? = null, maxSeconds: Int = 60) {
        searchDeviceType = type
        upnpService?.get()?.also { service ->
            //when search device, clear all founded first.
            service.registry.removeAllRemoteDevices()
            service.controlPoint.search(type?.let { UDADeviceTypeHeader(it) } ?: STAllHeader(), maxSeconds)
        }
    }

    private val deviceControlMap = mutableMapOf<Device<*, *, *>, DeviceControl?>()
    fun connectDevice(device: Device<*, *, *>, listener: OnDeviceControlListener): DeviceControl {
        val service = upnpService?.get() ?: return EmptyDeviceControl
        var control = deviceControlMap[device]
        if (control == null) {
            val newController = CastControlImpl(service.controlPoint, device, listener)
            deviceControlMap[device] = newController
            control = newController
        }
        return control
    }

    fun disconnectDevice(device: Device<*, *, *>) {
        (deviceControlMap[device] as? CastControlImpl)?.released = true
        deviceControlMap[device] = null
    }
}