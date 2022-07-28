package com.android.cast.dlna.demo

import android.view.View
import android.view.View.OnClickListener
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.android.cast.dlna.demo.DeviceAdapter.OnItemSelectedListener
import org.fourthline.cling.model.meta.Device

class DeviceHolder internal constructor(
    itemView: View,
    private val itemSelectedListener: OnItemSelectedListener?
) : ViewHolder(itemView), OnClickListener,
    OnCheckedChangeListener {

    private val name: TextView = itemView.findViewById(R.id.device_name)
    private val description: TextView = itemView.findViewById(R.id.device_description)
    private val id: TextView = itemView.findViewById(R.id.device_id)
    private val selector: CheckBox = itemView.findViewById(R.id.device_selector)
    private var mCastDevice: Device<*, *, *>? = null

    init {
        itemView.setOnClickListener(this)
        selector.setOnCheckedChangeListener(this)
    }

    fun setData(castDevice: Device<*, *, *>?, isSelected: Boolean) {
        if (castDevice == null) return
        mCastDevice = castDevice
        name.text = castDevice.details.friendlyName
        description.text = castDevice.details.manufacturerDetails.manufacturer
        id.text = castDevice.identity.udn.identifierString
        selector.isChecked = isSelected
    }

    override fun onClick(v: View) {
        selector.isChecked = !selector.isChecked
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        itemSelectedListener?.onItemSelected(mCastDevice, isChecked)
    }
}