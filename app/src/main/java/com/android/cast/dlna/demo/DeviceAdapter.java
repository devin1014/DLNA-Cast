package com.android.cast.dlna.demo;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.android.cast.dlna.NLDeviceRegistryListener.OnRegistryDeviceListener;
import com.android.cast.dlna.device.CastDevice;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class DeviceAdapter extends Adapter<DeviceHolder> implements OnRegistryDeviceListener {
    private List<CastDevice> mDeviceList = new ArrayList<>();

    private LayoutInflater mLayoutInflater;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private OnItemSelectedListener mOnItemSelectedListener;

    private CastDevice mSelectedDevice;

    public DeviceAdapter(Activity activity, OnItemSelectedListener listener) {
        mLayoutInflater = activity.getLayoutInflater();

        mOnItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceHolder(mLayoutInflater.inflate(R.layout.item_device_list, parent, false), mOnItemSelectedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        holder.setData(getItem(position), isSelected(position));
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    private CastDevice getItem(int position) {
        if (position < 0 || position >= getItemCount()) {
            return null;
        }

        return mDeviceList.get(position);
    }

    public void setSelectedDevice(CastDevice device) {
        // Notify: remove code here!
        //        if (mSelectedDevice != null && device != null && mSelectedDevice.getId().equals(device.getId()))
        //        {
        //            return;
        //        }

        mSelectedDevice = device;

        notifyDataSetChanged();
    }

    private boolean isSelected(int position) {
        CastDevice device = getItem(position);

        if (device != null && mSelectedDevice != null) {
            return device == mSelectedDevice || device.getId().equals(mSelectedDevice.getId());
        }

        return false;
    }

    @Override
    public void onDeviceAdded(CastDevice device) {
        boolean added = false;

        for (CastDevice castDevice : mDeviceList) {
            if (castDevice.getId().equals(device.getId())) {
                castDevice = new CastDevice(device.getDevice());

                added = true;

                break;
            }
        }

        if (!added) {
            mDeviceList.add(device);
        }

        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceRemoved(CastDevice device) {
        CastDevice removeDevice = null;

        for (CastDevice castDevice : mDeviceList) {
            if (castDevice.getId().equals(device.getId())) {
                removeDevice = castDevice;

                break;
            }
        }

        if (removeDevice != null) {
            mDeviceList.remove(removeDevice);

            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(CastDevice castDevice, boolean selected);
    }
}
