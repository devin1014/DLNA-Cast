package com.android.cast.dlna.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.cast.dlna.core.ContentType.VIDEO
import com.android.cast.dlna.dmc.DLNACastManager
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
        return inflater.inflate(R.layout.fragment_query, container, false)
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
        if (device != null) {
            DLNACastManager.getInstance().getMediaInfo(device) { mediaInfo: MediaInfo?, errMsg: String? ->
                this.mediaInfo?.text = String.format("MediaInfo:\n%s", if (mediaInfo != null) mediaInfo.currentURI else errMsg)
            }
            DLNACastManager.getInstance().getPositionInfo(device) { positionInfo: PositionInfo?, errMsg: String? ->
                try {
                    this.positionInfo?.text = String.format("PositionInfo:\n%s", positionInfo ?: errMsg)
                } catch (e: Exception) {
                    e.printStackTrace()
                    this.positionInfo?.text = e.toString()
                }
            }
            DLNACastManager.getInstance().getTransportInfo(device) { transportInfo: TransportInfo?, errMsg: String? ->
                this.transportInfo?.text = String.format("TransportInfo:\n%s", if (transportInfo != null) transportInfo.currentTransportState else errMsg)
            }
            DLNACastManager.getInstance().getVolumeInfo(device) { volume: Int?, errMsg: String? ->
                volumeInfo?.text = String.format("Volume: %s", volume ?: errMsg)
            }
            DLNACastManager.getInstance().getContent(device, VIDEO) { s: DIDLContent?, errMsg: String? ->
                browseInfo?.text = if (s != null) parseContentString(s) else errMsg
            }
        } else {
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
        if (list == null || list.isEmpty()) return "[]"
        val builder = StringBuilder()
        for (item in list) {
            builder.append(item.firstResource.value).append("\n")
        }
        return builder.toString()
    }
}