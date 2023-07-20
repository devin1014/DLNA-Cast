package com.android.cast.dlna.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.cast.dlna.core.ContentType.VIDEO
import com.android.cast.dlna.demo.IDisplayDevice
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.R.layout
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.GetInfoListener
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.item.Item

class QueryFragment : Fragment(), IDisplayDevice {

    private val mediaInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_device_query_media_info) }
    private val positionInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_device_query_position_info) }
    private val transportInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_device_query_transport_info) }
    private val volumeInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_device_query_volume_info) }
    private val browseInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_device_browse_info) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_query, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponent(view)
    }

    private fun initComponent(view: View) {
        view.findViewById<View>(R.id.ctrl_device_query_refresh).setOnClickListener { setInfo() }
    }

    private var device: Device<*, *, *>? = null
    override fun setCastDevice(device: Device<*, *, *>?) {
        this.device = device
        if (!isHidden) setInfo()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) setInfo()
    }

    private fun setInfo() {
        device?.let { device ->
            DLNACastManager.getMediaInfo(device, object : GetInfoListener<MediaInfo> {
                override fun onGetInfoResult(t: MediaInfo?, errMsg: String?) {
                    mediaInfo?.text = t?.currentURI ?: errMsg
                }
            })
            DLNACastManager.getPositionInfo(device, object : GetInfoListener<PositionInfo> {
                override fun onGetInfoResult(t: PositionInfo?, errMsg: String?) {
                    positionInfo?.text = t?.let { "${it.relTime}/${it.trackDuration}" } ?: errMsg
                }
            })
            DLNACastManager.getTransportInfo(device, object : GetInfoListener<TransportInfo> {
                override fun onGetInfoResult(t: TransportInfo?, errMsg: String?) {
                    transportInfo?.text = t?.currentTransportState?.value ?: errMsg
                }
            })
            DLNACastManager.getVolumeInfo(device, object : GetInfoListener<Int> {
                override fun onGetInfoResult(t: Int?, errMsg: String?) {
                    volumeInfo?.text = t?.toString() ?: errMsg
                }
            })
            DLNACastManager.getContent(device, VIDEO, object : GetInfoListener<DIDLContent> {
                override fun onGetInfoResult(t: DIDLContent?, errMsg: String?) {
                    browseInfo?.text = t?.let { parseContentString(it) } ?: errMsg
                }
            })
        }

        if (device == null) {
            mediaInfo?.text = ""
            positionInfo?.text = ""
            transportInfo?.text = ""
            volumeInfo?.text = ""
            browseInfo?.text = ""
        }
    }

    private fun parseContentString(content: DIDLContent?): String {
        if (content == null || content.items.isEmpty() && content.containers.isEmpty()) return ""
        val builder = StringBuilder()
        if (content.containers.isNotEmpty()) {
            for (container in content.containers) {
                builder
                    .append("\nContainer: ")
                    .append(container.title)
                    .append("\nItems:\n")
                    .append(parseItems(container.items))
            }
        } else if (content.items.isNotEmpty()) {
            builder
                .append("\nItems:\n")
                .append(parseItems(content.items))
        }
        return builder.toString()
    }

    private fun parseItems(list: List<Item>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val builder = StringBuilder()
        for (item in list) {
            builder.append(item.firstResource.value).append("\n")
        }
        return builder.toString()
    }
}