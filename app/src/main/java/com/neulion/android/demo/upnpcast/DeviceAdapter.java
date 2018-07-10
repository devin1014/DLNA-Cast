package com.neulion.android.demo.upnpcast;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.neulion.android.upnpcast.NLDeviceRegistryListener.OnRegistryDeviceListener;
import com.neulion.android.upnpcast.device.CastDevice;

import java.util.ArrayList;
import java.util.List;


/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-02
 * Time: 16:32
 */
public class DeviceAdapter extends Adapter<DeviceHolder> implements OnRegistryDeviceListener
{
    private List<CastDevice> mDeviceList = new ArrayList<>();

    private LayoutInflater mLayoutInflater;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private OnClickListener mOnClickListener;

    private CastDevice mSelectedDevice;

    public DeviceAdapter(Activity activity, OnClickListener listener)
    {
        mLayoutInflater = activity.getLayoutInflater();

        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new DeviceHolder(mLayoutInflater.inflate(R.layout.item_device_list, parent, false), mOnClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position)
    {
        holder.setData(getItem(position), isSelected(position));
    }

    @Override
    public int getItemCount()
    {
        return mDeviceList.size();
    }

    private CastDevice getItem(int position)
    {
        if (position < 0 || position >= getItemCount())
        {
            return null;
        }

        return mDeviceList.get(position);
    }

    public void setSelectedDevice(CastDevice device)
    {
        if (mSelectedDevice != null && device != null && mSelectedDevice.getId().equals(device.getId()))
        {
            return;
        }

        mSelectedDevice = device;

        notifyDataSetChanged();
    }

    private boolean isSelected(int position)
    {
        CastDevice device = getItem(position);

        if (device != null && mSelectedDevice != null)
        {
            return device.getId().equals(mSelectedDevice.getId());
        }

        return false;
    }

    @Override
    public void onDeviceAdded(CastDevice device)
    {
        for (CastDevice castDevice : mDeviceList)
        {
            if (castDevice.getId().equals(device.getId()))
            {
                mDeviceList.remove(castDevice);

                break;
            }
        }

        mDeviceList.add(device);

        if (Thread.currentThread() != Looper.getMainLooper().getThread())
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    notifyDataSetChanged();
                }
            });
        }
        else
        {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceRemoved(CastDevice device)
    {
        CastDevice removeDevice = null;

        for (CastDevice castDevice : mDeviceList)
        {
            if (castDevice.getId().equals(device.getId()))
            {
                removeDevice = castDevice;

                break;
            }
        }

        if (removeDevice != null)
        {
            mDeviceList.remove(removeDevice);

            if (Thread.currentThread() != Looper.getMainLooper().getThread())
            {
                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        notifyDataSetChanged();
                    }
                });
            }
            else
            {
                notifyDataSetChanged();
            }
        }
    }
}
