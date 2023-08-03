package com.android.cast.dlna.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.cast.dlna.demo.DetailContainer
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.R.layout
import org.fourthline.cling.model.meta.Action
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service

class DeviceServiceActionFragment : Fragment() {
    private val device: Device<*, *, *> by lazy { (requireParentFragment() as DetailContainer).getDevice() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layout.fragment_device_service_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.info_device_name)?.text = device.details?.friendlyName
        view.findViewById<TextView>(R.id.info_device_status)?.text = buildDeviceInfo(device)
    }

    private fun buildDeviceInfo(device: Device<*, *, *>): String {
        val builder = StringBuilder()
        device.details.baseURL?.let { url -> builder.append("URL: $url\n") }
        builder.append("DeviceType: ${device.type.type}\n")
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