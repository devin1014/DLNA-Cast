package com.android.cast.dlna.demo.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.cast.dlna.core.ContentType
import com.android.cast.dlna.demo.DetailContainer
import com.android.cast.dlna.demo.MainActivity
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.DeviceControl
import com.android.cast.dlna.dmc.control.OnDeviceControlListener
import com.android.cast.dlna.dmc.control.ServiceActionCallback
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.model.DIDLContent

class ContentFragment : Fragment() {
    private val device: Device<*, *, *> by lazy { (requireParentFragment() as DetailContainer).getDevice() }
    private val contentResult: TextView by lazy { requireView().findViewById(R.id.content_result) }
    private lateinit var deviceControl: DeviceControl

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.browse).setOnClickListener {
            deviceControl.browse(ContentType.ALL.id, object : ServiceActionCallback<DIDLContent> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(result: DIDLContent) {
                    contentResult.text = "Browse\nContainers:${result.containers.size}\nItems:${result.items.size}"
                }

                override fun onFailure(msg: String) {
                    contentResult.text = msg
                }
            })
        }
        view.findViewById<View>(R.id.search).setOnClickListener {
            deviceControl.search(ContentType.ALL.id, object : ServiceActionCallback<DIDLContent> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(result: DIDLContent) {
                    contentResult.text = "Search\nContainers:${result.containers.size}\nItems:${result.items.size}"
                }

                override fun onFailure(msg: String) {
                    contentResult.text = msg
                }
            })
        }
        deviceControl = DLNACastManager.connectDevice(device, object : OnDeviceControlListener {
            override fun onConnected(device: Device<*, *, *>) {
                Toast.makeText(requireContext(), "成功连接: ${device.details.friendlyName}", Toast.LENGTH_SHORT).show()
            }

            override fun onDisconnected(device: Device<*, *, *>) {
                (requireActivity() as MainActivity).onBackPressed()
                Toast.makeText(requireContext(), "无法连接: ${device.details.friendlyName}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}