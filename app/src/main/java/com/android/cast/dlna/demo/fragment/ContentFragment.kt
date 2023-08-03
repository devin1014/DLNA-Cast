package com.android.cast.dlna.demo.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.android.cast.dlna.demo.DetailContainer
import com.android.cast.dlna.demo.MainActivity
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.DeviceControl
import com.android.cast.dlna.dmc.control.OnDeviceControlListener
import com.android.cast.dlna.dmc.control.ServiceActionCallback
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.Item
import kotlin.math.roundToInt

class ContentFragment : Fragment() {
    private val device: Device<*, *, *> by lazy { (requireParentFragment() as DetailContainer).getDevice() }
    private val browseContainer: LinearLayout by lazy { requireView().findViewById(R.id.content_browse_container) }
    private val resultInfo: TextView by lazy { requireView().findViewById(R.id.content_result_info) }
    private val adapter: ContentAdapter = ContentAdapter()
    private lateinit var deviceControl: DeviceControl

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ContentFragment.adapter
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
        deviceControl.search(callback = object : ServiceActionCallback<DIDLContent> {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(result: DIDLContent) {
                result.containers.forEach { item ->
                    browseContainer.addView(
                        layoutInflater.inflate(R.layout.item_browse_button, browseContainer, false).apply {
                            (this as Button).text = item.title
                            setOnClickListener {
                                // '0' 应该是代表根目录，具体要看server的实现
                                deviceControl.browse(objectId = item.id, callback = object : ServiceActionCallback<DIDLContent> {
                                    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
                                    override fun onSuccess(result: DIDLContent) {
                                        resultInfo.text = "Containers: [${result.containers.size}]\n" +
                                                "Items: [${result.items.size}]\n" +
                                                "descMetadata: [${result.descMetadata.size}]"
                                        adapter.items = result.items
                                        adapter.containers = result.containers
                                        adapter.notifyDataSetChanged()
                                    }

                                    override fun onFailure(msg: String) {
                                        activity?.also { context ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })
                            }
                        })
                }
            }

            override fun onFailure(msg: String) {
                activity?.also { context ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private inner class ContentAdapter : Adapter<ContentHolderView>() {
        var items: List<Item>? = null
        val itemSize: Int
            get() = items?.size ?: 0
        var containers: List<Container>? = null
        val containerSize: Int
            get() = containers?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentHolderView {
            return ContentHolderView(layoutInflater.inflate(R.layout.item_content, parent, false))
        }

        override fun onBindViewHolder(holder: ContentHolderView, position: Int) {
            val item: Any? = if (position < itemSize) {
                items?.get(position)
            } else if (position - itemSize < containerSize) {
                containers?.get(position - itemSize)
            } else {
                null
            }
            holder.setData(position, item)
        }

        override fun getItemCount(): Int = (items?.size ?: 0) + (containers?.size ?: 0)
    }

    private inner class ContentHolderView(itemView: View) : ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.content_name)
        private val info: TextView = itemView.findViewById(R.id.content_info)
        private val path: TextView = itemView.findViewById(R.id.content_path)

        @SuppressLint("SetTextI18n")
        fun setData(position: Int, item: Any?) {
            (item as? Item)?.let { data ->
                name.text = "[${position}] ${data.title}"
                info.text = data.firstResource?.size?.let { "size: ${parseSize(it)}" } ?: "unknown"
                path.text = data.firstResource?.value ?: data.clazz.friendlyName
            }
            (item as? Container)?.let { data ->
                name.text = "[${position}] ${data.title}"
                info.text = "[${data.parentID}][${data.id}]"
                path.text = "containers: ${data.containers.size}, items: ${data.items.size}"
            }
        }

        private fun parseSize(fileSize: Long): String {
            val gb = (fileSize / 1024 / 1024 / 1024 * 100f).roundToInt() / 100f
            if (gb > 1) return "$gb Gb"
            val mb = (fileSize / 1024 / 1024 * 100f).roundToInt() / 100f
            if (mb > 1) return "$mb Mb"
            val kb = (fileSize / 1024 * 100f).roundToInt() / 100f
            return "$kb kb"
        }
    }
}

