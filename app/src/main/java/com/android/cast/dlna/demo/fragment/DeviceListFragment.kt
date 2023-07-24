package com.android.cast.dlna.demo.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.OnDeviceRegistryListener
import org.fourthline.cling.model.meta.Device

class DeviceListFragment : Fragment() {
    private lateinit var adapter: DeviceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_device_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = DeviceAdapter(requireContext(), requireActivity() as OnItemClickListener).also { adapter = it }

        DLNACastManager.registerDeviceListener(adapter)
    }

    override fun onDestroyView() {
        DLNACastManager.unregisterListener(adapter)
        super.onDestroyView()
    }
}

interface OnItemClickListener {
    fun onItemClick(device: Device<*, *, *>)
}

@SuppressLint("NotifyDataSetChanged")
private class DeviceAdapter(
    context: Context,
    private val listener: OnItemClickListener,
) : Adapter<DeviceHolder>(), OnDeviceRegistryListener {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val deviceList: MutableList<Device<*, *, *>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        return DeviceHolder(layoutInflater.inflate(R.layout.item_device, parent, false), listener)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.setData(getItem(position))
    }

    override fun getItemCount(): Int = deviceList.size

    private fun getItem(position: Int): Device<*, *, *>? {
        return if (position < 0 || position >= itemCount) {
            null
        } else deviceList[position]
    }

    override fun onDeviceAdded(device: Device<*, *, *>) {
        if (!deviceList.contains(device)) {
            deviceList.add(device)
            notifyDataSetChanged()
        }
    }

    override fun onDeviceUpdated(device: Device<*, *, *>) {}

    override fun onDeviceRemoved(device: Device<*, *, *>) {
        if (deviceList.contains(device)) {
            deviceList.remove(device)
            notifyDataSetChanged()
        }
    }
}

private class DeviceHolder(
    itemView: View,
    private val itemSelectedListener: OnItemClickListener,
) : ViewHolder(itemView), OnClickListener {

    private val deviceName: TextView = itemView.findViewById(R.id.device_name)
    private val deviceDescription: TextView = itemView.findViewById(R.id.device_description)
    private val deviceType: TextView = itemView.findViewById(R.id.device_type)
    private val deviceId: TextView = itemView.findViewById(R.id.device_id)

    init {
        itemView.setOnClickListener(this)
    }

    fun setData(device: Device<*, *, *>?) {
        itemView.tag = device
        device?.apply {
            deviceName.text = details.friendlyName
            deviceDescription.text = details.manufacturerDetails.manufacturer
            deviceType.text = type.type
            deviceId.text = identity.udn.identifierString
        }
    }

    override fun onClick(v: View) {
        (v.tag as? Device<*, *, *>)?.also { device ->
            itemSelectedListener.onItemClick(device)
        }
    }

}