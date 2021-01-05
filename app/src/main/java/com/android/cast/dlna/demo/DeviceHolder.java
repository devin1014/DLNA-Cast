package com.android.cast.dlna.demo;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.android.cast.dlna.demo.DeviceAdapter.OnItemSelectedListener;

import org.fourthline.cling.model.meta.Device;

/**
 *
 */
public class DeviceHolder extends ViewHolder implements OnClickListener, OnCheckedChangeListener {
    private final TextView name;
    private final TextView description;
    private final TextView id;
    private final CheckBox selector;
    private final OnItemSelectedListener mOnItemSelectedListener;
    private Device<?, ?, ?> mCastDevice;
    private boolean mBinding = false;

    DeviceHolder(final View itemView, OnItemSelectedListener listener) {
        super(itemView);

        mOnItemSelectedListener = listener;

        itemView.setOnClickListener(this);
        name = itemView.findViewById(R.id.device_name);
        description = itemView.findViewById(R.id.device_description);
        id = itemView.findViewById(R.id.device_id);
        selector = itemView.findViewById(R.id.device_selector);
        selector.setOnCheckedChangeListener(this);
    }

    public void setData(@Nullable Device<?, ?, ?> castDevice, boolean isSelected) {
        if (castDevice == null) return;
        mBinding = true;
        mCastDevice = castDevice;
        name.setText(castDevice.getDetails().getFriendlyName());
        description.setText(castDevice.getDetails().getManufacturerDetails().getManufacturer());
        id.setText(castDevice.getIdentity().getUdn().getIdentifierString());
        selector.setChecked(isSelected);
        mBinding = false;
    }

    @Override
    public void onClick(View v) {
        selector.setChecked(!selector.isChecked());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mOnItemSelectedListener != null && !mBinding) {
            mOnItemSelectedListener.onItemSelected(mCastDevice, isChecked);
        }
    }
}
