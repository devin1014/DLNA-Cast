package com.android.cast.dlna.demo;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.android.cast.dlna.OnDeviceRegistryListener;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class DeviceAdapter extends Adapter<DeviceHolder> implements OnDeviceRegistryListener {
    private final LayoutInflater mLayoutInflater;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<Device<?, ?, ?>> mDeviceList = new ArrayList<>();
    private final OnItemSelectedListener mOnItemSelectedListener;

    private Device<?, ?, ?> mSelectedDevice;

    public DeviceAdapter(Activity activity, OnItemSelectedListener listener) {
        mLayoutInflater = activity.getLayoutInflater();
        mOnItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceHolder(mLayoutInflater.inflate(R.layout.item_device, parent, false), mOnItemSelectedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        holder.setData(getItem(position), isSelected(position));
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    private Device<?, ?, ?> getItem(int position) {
        if (position < 0 || position >= getItemCount()) {
            return null;
        }

        return mDeviceList.get(position);
    }

    public void setSelectedDevice(Device<?, ?, ?> device) {
        mSelectedDevice = device;
        notifyDataSetChanged();
    }

    public Device<?, ?, ?> getCastDevice() {
        return mSelectedDevice;
    }

    private boolean isSelected(int position) {
        Device<?, ?, ?> device = getItem(position);
        if (device != null && mSelectedDevice != null) {
            return device.getIdentity().getUdn().getIdentifierString().equals(mSelectedDevice.getIdentity().getUdn().getIdentifierString());
        }
        return false;
    }

    @Override
    public void onDeviceAdded(Device<?, ?, ?> device) {
        if (!mDeviceList.contains(device)) {
            mDeviceList.add(device);
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                mHandler.post(this::notifyDataSetChanged);
            } else {
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDeviceUpdated(Device<?, ?, ?> device) {
    }

    @Override
    public void onDeviceRemoved(Device<?, ?, ?> device) {
        if (mDeviceList.contains(device)) {
            mDeviceList.remove(device);
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                mHandler.post(this::notifyDataSetChanged);
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Device<?, ?, ?> castDevice, boolean selected);
    }
}
