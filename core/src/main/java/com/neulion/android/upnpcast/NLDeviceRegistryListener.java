package com.neulion.android.upnpcast;

import android.support.annotation.NonNull;

import com.neulion.android.upnpcast.device.CastDevice;
import com.neulion.android.upnpcast.util.DeviceUtil;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-02
 * Time: 14:12
 */
public class NLDeviceRegistryListener extends DefaultRegistryListener
{
    private ILogger mLog = new DefaultLoggerImpl(NLDeviceRegistryListener.class.getSimpleName());

    /* Discovery performance optimization for very slow Android devices! */
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device)
    {
        mLog.d("remoteDeviceDiscoveryStarted:" + DeviceUtil.parseDevice(device));
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex)
    {
        mLog.w("remoteDeviceDiscoveryFailed:" + DeviceUtil.parseDevice(device));

        deviceRemoved(device);
    }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device)
    {
        mLog.d("remoteDeviceAdded:" + DeviceUtil.parseDevice(device));

        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
    {
        mLog.w("remoteDeviceRemoved:" + DeviceUtil.parseDevice(device));

        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device)
    {
        mLog.d("localDeviceAdded:" + DeviceUtil.parseDevice(device));
        //        deviceAdded(device); // 本地设备 已加入
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device)
    {
        mLog.w("localDeviceRemoved:" + DeviceUtil.parseDevice(device));
        //        deviceRemoved(device); // 本地设备 已移除
    }

    private void deviceAdded(Device device)
    {
        if (device.getType().equals(mSearchDeviceType))
        {
            //mLog.w(String.format("[%s][%s] device type not matched.", device.getType(), device.getDisplayString()));

            CastDevice castDevice = new CastDevice(device);

            for (OnRegistryDeviceListener listener : mOnRegistryDeviceListener)
            {
                if (listener != null) // remove listener empty list now!
                {
                    listener.onDeviceAdded(castDevice);
                }
            }
        }
    }

    private void deviceRemoved(Device device)
    {
        CastDevice castDevice = new CastDevice(device);

        for (OnRegistryDeviceListener listener : mOnRegistryDeviceListener)
        {
            if (listener != null) // remove listener empty list now!
            {
                listener.onDeviceRemoved(castDevice);
            }
        }
    }

    private DeviceType mSearchDeviceType = NLUpnpCastManager.DEVICE_TYPE_DMR;

    public void setSearchDeviceType(@NonNull DeviceType type)
    {
        if (!type.equals(mSearchDeviceType))
        {
            mSearchDeviceType = type;
        }
    }

    private List<OnRegistryDeviceListener> mOnRegistryDeviceListener = new ArrayList<>();

    public void addRegistryDeviceListener(OnRegistryDeviceListener listener)
    {
        if (mOnRegistryDeviceListener != null && listener != null && !mOnRegistryDeviceListener.contains(listener))
        {
            mOnRegistryDeviceListener.add(listener);
        }
    }

    public void removeRegistryListener(OnRegistryDeviceListener listener)
    {
        if (mOnRegistryDeviceListener != null && listener != null && mOnRegistryDeviceListener.contains(listener))
        {
            mOnRegistryDeviceListener.remove(listener);
        }
    }

    public interface OnRegistryDeviceListener
    {
        void onDeviceAdded(CastDevice device);

        void onDeviceRemoved(CastDevice device);
    }
}
