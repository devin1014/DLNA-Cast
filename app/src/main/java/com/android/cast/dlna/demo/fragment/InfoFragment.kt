package com.android.cast.dlna.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.R.layout
import org.fourthline.cling.model.meta.Action
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service

class InfoFragment : Fragment() {

    var device: Device<*, *, *>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layout.fragment_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.info_device_name)?.text = device?.details?.friendlyName ?: device?.identity?.udn?.identifierString
        view.findViewById<TextView>(R.id.info_device_status)?.text = device?.let { buildDeviceInfo(it) } ?: ""
    }

    private fun buildDeviceInfo(device: Device<*, *, *>): String {
        val builder = StringBuilder()
        builder.append("URL: ").append(device.details.baseURL?.toString() ?: "null").append("\n")
        builder.append("DeviceType: ").append(device.type.type).append("\n")
        builder.append("ModelName: ").append(device.details.modelDetails.modelName).append("\n")
        builder.append("ModelDescription: ").append(device.details.modelDetails.modelDescription).append("\n")
        try {
            builder.append("ModelURL: ").append(device.details.modelDetails.modelURI.toString()).append("\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        (device.services as? Array<out Service<*, *>>)?.forEach { service ->
            builder.append("\n")
            builder.append("ServiceType: ").append(service.serviceType.type).append("\n")
            val list = mutableListOf(*service.actions)
            list.sortWith { o1: Action<*>, o2: Action<*> -> o1.name.compareTo(o2.name) }
            builder.append("Action: ")
            for (action in list) {
                builder.append(action.name).append(", ")
            }
            builder.append("\n")
        }
        return builder.toString()
    }
}