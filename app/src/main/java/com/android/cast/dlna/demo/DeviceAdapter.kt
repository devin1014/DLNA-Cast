package com.android.cast.dlna.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.android.cast.dlna.demo.R.layout
import com.android.cast.dlna.dmc.OnDeviceRegistryListener
import org.fourthline.cling.model.meta.Device

@SuppressLint("NotifyDataSetChanged")
class DeviceAdapter(activity: Activity, listener: OnItemSelectedListener?) : Adapter<DeviceHolder>(), OnDeviceRegistryListener {

    interface OnItemSelectedListener {
        fun onItemSelected(castDevice: Device<*, *, *>?, selected: Boolean)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val layoutInflater: LayoutInflater = activity.layoutInflater
    private val deviceList: MutableList<Device<*, *, *>> = ArrayList()
    private val itemSelectedListener: OnItemSelectedListener? = listener

    var castDevice: Device<*, *, *>? = null
        set(value) {
            field = value
            // app maybe crashed here
            handler.post { notifyDataSetChanged() }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        return DeviceHolder(layoutInflater.inflate(layout.item_device, parent, false), itemSelectedListener)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.setData(getItem(position), isSelected(position))
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    private fun getItem(position: Int): Device<*, *, *>? {
        return if (position < 0 || position >= itemCount) {
            null
        } else deviceList[position]
    }

    private fun isSelected(position: Int): Boolean {
        val device = getItem(position)
        return if (device != null && castDevice != null) {
            device.identity.udn.identifierString == castDevice!!.identity.udn.identifierString
        } else false
    }

    override fun onDeviceAdded(device: Device<*, *, *>) {
        if (!deviceList.contains(device)) {
            deviceList.add(device)
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                handler.post { notifyDataSetChanged() }
            } else {
                notifyDataSetChanged()
            }
        }
    }

    override fun onDeviceUpdated(device: Device<*, *, *>) {}

    override fun onDeviceRemoved(device: Device<*, *, *>) {
        if (deviceList.contains(device)) {
            deviceList.remove(device)
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                handler.post { notifyDataSetChanged() }
            } else {
                notifyDataSetChanged()
            }
        }
    }
}