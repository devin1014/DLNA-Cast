package com.neulion.android.demo.upnpcast;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.neulion.android.upnpcast.device.CastDevice;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-02
 * Time: 16:32
 */
public class DeviceHolder extends ViewHolder
{
    private TextView name;
    private TextView description;
    private CheckBox selector;

    DeviceHolder(View itemView, OnClickListener listener)
    {
        super(itemView);

        itemView.setOnClickListener(listener);
        name = itemView.findViewById(R.id.device_name);
        description = itemView.findViewById(R.id.device_description);
        selector = itemView.findViewById(R.id.device_selector);
    }

    public void setData(CastDevice castDevice, boolean isSelected)
    {
        itemView.setTag(castDevice);
        name.setText(castDevice.getName());
        description.setText(castDevice.getDescription());
        selector.setChecked(isSelected);
    }
}
