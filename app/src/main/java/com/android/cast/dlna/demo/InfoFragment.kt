package com.android.cast.dlna.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.fourthline.cling.model.meta.Action
import org.fourthline.cling.model.meta.Device

class InfoFragment : Fragment(), IDisplayDevice {

    private val castDeviceInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_device_status) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_information, container, false)
    }

    override fun setCastDevice(device: Device<*, *, *>?) {
        if (device == null) {
            castDeviceInfo?.text = ""
            return
        }
        val builder = StringBuilder()
        val url = device.details.baseURL
        builder.append("URL: ").append(url?.toString() ?: "null").append("\n")
        builder.append("DeviceType: ").append(device.type.type).append("\n")
        builder.append("ModelName: ").append(device.details.modelDetails.modelName).append("\n")
        builder.append("ModelDescription: ").append(device.details.modelDetails.modelDescription).append("\n")
        try {
            builder.append("ModelURL: ").append(device.details.modelDetails.modelURI.toString()).append("\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        device.services?.forEach { service ->
            builder.append("\n")
            builder.append("ServiceId: ").append(service.serviceId.id).append("\n")
            builder.append("ServiceType: ").append(service.serviceType.type).append("\n")
            val list = mutableListOf(*service.actions)
            list.sortWith { o1: Action<*>, o2: Action<*> -> o1.name.compareTo(o2.name) }
            builder.append("Action: ")
            for (action in list) {
                builder.append(action.name).append(", ")
            }
            builder.append("\n")
        }
        castDeviceInfo?.text = builder.toString()
    }
}