package com.android.cast.dlna.dmc

import android.os.Handler
import android.os.Looper
import com.android.cast.dlna.core.Logger
import org.fourthline.cling.model.meta.Action
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry

/**
 *
 */
internal class DeviceRegistryImpl(private val deviceRegistryListener: OnDeviceRegistryListener) : DefaultRegistryListener() {

    private val logger = Logger.create("DeviceRegistry")
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    var ignoreUpdate = true

    fun setDevices(collection: Collection<Device<*, *, *>>?) {
        if (!collection.isNullOrEmpty()) {
            for (device in collection) {
                notifyDeviceAdd(device)
            }
        }
    }

    // Discovery performance optimization for very slow Android devices!
    // This function will called early than 'remoteDeviceAdded',but the device services maybe not entirely.
    override fun remoteDeviceDiscoveryStarted(registry: Registry, device: RemoteDevice) {
        logger.i(String.format("[%s] discovery started...", device.details.friendlyName))
    }

    //End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz)
    override fun remoteDeviceDiscoveryFailed(registry: Registry, device: RemoteDevice, ex: Exception) {
        logger.e(String.format("[%s] discovery failed...", device.details.friendlyName), ex)
    }

    // remote device
    override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
        logger.i("remoteDeviceAdded: " + parseDeviceInfo(device))
        logger.i(parseDeviceService(device))
        notifyDeviceAdd(device)
    }

    override fun remoteDeviceUpdated(registry: Registry, device: RemoteDevice) {
        if (!ignoreUpdate) {
            logger.d("remoteDeviceUpdated: " + parseDeviceInfo(device))
            notifyDeviceUpdate(device)
        }
    }

    override fun remoteDeviceRemoved(registry: Registry, device: RemoteDevice) {
        logger.w("remoteDeviceRemoved: " + parseDeviceInfo(device))
        notifyDeviceRemove(device)
    }

    // local device
    override fun localDeviceAdded(registry: Registry, device: LocalDevice) {
        super.localDeviceAdded(registry, device)
    }

    override fun localDeviceRemoved(registry: Registry, device: LocalDevice) {
        super.localDeviceRemoved(registry, device)
    }

    private fun notifyDeviceAdd(device: Device<*, *, *>) {
        handler.post { deviceRegistryListener.onDeviceAdded(device) }
    }

    private fun notifyDeviceUpdate(device: Device<*, *, *>) {
        handler.post { deviceRegistryListener.onDeviceUpdated(device) }
    }

    private fun notifyDeviceRemove(device: Device<*, *, *>) {
        handler.post { deviceRegistryListener.onDeviceRemoved(device) }
    }

    private fun parseDeviceInfo(device: RemoteDevice): String =
        "[${device.type.type}][${device.details.friendlyName}][${device.details.manufacturerDetails.manufacturer}][${device.identity.udn}]"

    private fun parseDeviceService(device: RemoteDevice): String {
        val builder = StringBuilder(device.details.friendlyName)
        builder.append(":")
        for (service in device.services) {
            builder.append("\nservice:").append(service.serviceType.type)
            if (service.hasActions()) {
                builder.append("\nactions: ")
                val list = mutableListOf<Action<*>>(*service.actions)
                list.sortWith { o1: Action<*>, o2: Action<*> -> o1.name.compareTo(o2.name) }
                for (action in list) {
                    builder.append(action.name).append(", ")
                }
            }
        }
        return builder.toString()
    }
}