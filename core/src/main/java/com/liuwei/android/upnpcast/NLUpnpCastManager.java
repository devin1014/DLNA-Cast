package com.liuwei.android.upnpcast;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.liuwei.android.upnpcast.NLDeviceRegistryListener.OnRegistryDeviceListener;
import com.liuwei.android.upnpcast.controller.CastControlImp;
import com.liuwei.android.upnpcast.controller.CastEventListenerListWrapper;
import com.liuwei.android.upnpcast.controller.CastObject;
import com.liuwei.android.upnpcast.controller.ICastEventListener;
import com.liuwei.android.upnpcast.device.CastDevice;
import com.liuwei.android.upnpcast.service.NLUpnpCastService;
import com.liuwei.android.upnpcast.service.NLUpnpCastService.NLUpnpCastBinder;
import com.liuwei.android.upnpcast.util.ILogger;
import com.liuwei.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class NLUpnpCastManager implements IUpnpCast
{
    private static class Holder
    {
        private static final NLUpnpCastManager INSTANCE = new NLUpnpCastManager();
    }

    public static NLUpnpCastManager getInstance()
    {
        return Holder.INSTANCE;
    }

    public static final DeviceType DEVICE_TYPE_DMR = new UDADeviceType("MediaRenderer");
    public static final ServiceType SERVICE_AV_TRANSPORT = new UDAServiceType("AVTransport");
    public static final ServiceType SERVICE_RENDERING_CONTROL = new UDAServiceType("RenderingControl");

    private NLUpnpCastService mUpnpCastService;

    private ILogger mLogger = new DefaultLoggerImpl(this);

    private NLUpnpCastManager()
    {
    }

    public void bindUpnpCastService(Activity activity)
    {
        if (activity != null)
        {
            mLogger.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            mLogger.i(String.format("bindUpnpCastService[%s]", activity.getComponentName().getShortClassName()));

            activity.getApplication().bindService(new Intent(activity, NLUpnpCastService.class), mUpnpCastServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    public void unbindUpnpCastService(Activity activity)
    {
        if (activity != null)
        {
            activity.getApplication().unbindService(mUpnpCastServiceConnection);

            mLogger.w(String.format("unbindUpnpCastService[%s]", activity.getComponentName().getShortClassName()));

            mLogger.i("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

            mUpnpCastServiceConnection.onServiceDisconnected(null);
        }
    }

    private ServiceConnection mUpnpCastServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mLogger.i(String.format("onServiceConnected [%s][%s]",
                    componentName.getShortClassName(), iBinder.getClass().getSimpleName() + "@" + Integer.toHexString(iBinder.hashCode())));

            NLUpnpCastService service = ((NLUpnpCastBinder) iBinder).getService();

            mUpnpCastService = service;

            // add registry listener
            Collection<RegistryListener> collection = service.getRegistry().getListeners();

            if (collection == null || !collection.contains(mNLDeviceRegistryListener))
            {
                service.getRegistry().addListener(mNLDeviceRegistryListener);
            }

            // Now add all devices to the list we already know about
            for (Device device : service.getRegistry().getDevices())
            {
                mNLDeviceRegistryListener.deviceAdded(service.getRegistry(), device);
            }

            if (mCastControlImp != null)
            {
                mCastControlImp.bindNLUpnpCastService(service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mLogger.w(String.format("onServiceDisconnected [%s]", componentName != null ? componentName.getShortClassName() : "NULL"));

            // clear registry listener
            if (mUpnpCastService != null)
            {
                Collection<RegistryListener> collection = mUpnpCastService.getRegistry().getListeners();

                if (collection != null && collection.contains(mNLDeviceRegistryListener))
                {
                    mUpnpCastService.getRegistry().removeListener(mNLDeviceRegistryListener);
                }
            }

            if (mCastControlImp != null)
            {
                mCastControlImp.unbindNLUpnpCastService();
            }

            mUpnpCastService = null;
        }

        @Override
        public void onBindingDied(ComponentName componentName)
        {
            mLogger.e(String.format("onBindingDied [%s]", componentName.getClassName()));
        }
    };

    public void addRegistryDeviceListener(OnRegistryDeviceListener listener)
    {
        if (mUpnpCastService != null)
        {
            Collection<Device> devices;

            if (mSearchDeviceType == null)
            {
                devices = mUpnpCastService.getRegistry().getDevices();
            }
            else
            {
                devices = mUpnpCastService.getRegistry().getDevices(mSearchDeviceType);
            }

            if (devices != null)
            {
                for (Device device : devices)
                {
                    listener.onDeviceAdded(new CastDevice(device));
                }
            }
        }

        mNLDeviceRegistryListener.addRegistryDeviceListener(listener);
    }

    public void removeRegistryListener(OnRegistryDeviceListener listener)
    {
        mNLDeviceRegistryListener.removeRegistryListener(listener);
    }

    private NLDeviceRegistryListener mNLDeviceRegistryListener = new NLDeviceRegistryListener();

    private List<ICastEventListener> mListeners = new ArrayList<>();

    public void addCastEventListener(ICastEventListener listener)
    {
        if (!mListeners.contains(listener))
        {
            mListeners.add(listener);
        }
    }

    public void removeCastEventListener(ICastEventListener listener)
    {
        mListeners.remove(listener);
    }

    // ------------------------------------------------------------------------------------------
    // control
    // ------------------------------------------------------------------------------------------
    private DeviceType mSearchDeviceType;

    @Override
    public void search()
    {
        search(null, IUpnpCast.DEFAULT_MAX_SECONDS);
    }

    @Override
    public void search(DeviceType type, int maxSeconds)
    {
        mSearchDeviceType = type;

        UpnpHeader header = type == null ? new STAllHeader() : new UDADeviceTypeHeader(type);

        if (mUpnpCastService != null)
        {
            mUpnpCastService.get().getControlPoint().search(header, maxSeconds);
        }
    }

    @Override
    public void clear()
    {
        if (mUpnpCastService != null)
        {
            mUpnpCastService.get().getRegistry().removeAllRemoteDevices();
        }
    }

    private CastControlImp mCastControlImp;

    @Override
    public void connect(CastDevice castDevice)
    {
        if (mCastControlImp == null)
        {
            mCastControlImp = new CastControlImp(mUpnpCastService, mNLDeviceRegistryListener, new CastEventListenerListWrapper(mListeners));
        }

        mCastControlImp.connect(castDevice);
    }

    @Override
    public void disconnect()
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.disconnect();
        }
    }

    @Override
    public boolean isConnected()
    {
        return mCastControlImp != null && mCastControlImp.isConnected();
    }

    @Override
    public void cast(CastObject castObject)
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.cast(castObject);
        }
    }

    @Override
    public void start()
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.start();
        }
    }

    @Override
    public void pause()
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.pause();
        }
    }

    @Override
    public void stop()
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.stop();
        }
    }

    @Override
    public void seekTo(long position)
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.seekTo(position);
        }
    }

    @Override
    public void setVolume(int percent)
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.setVolume(percent);
        }
    }

    @Override
    public void setBrightness(int percent)
    {
        if (mCastControlImp != null)
        {
            mCastControlImp.setBrightness(percent);
        }
    }

    @Override
    public int getCastStatus()
    {
        if (mCastControlImp != null)
        {
            return mCastControlImp.getCastStatus();
        }

        return CastControlImp.IDLE;
    }

    @Override
    public PositionInfo getPosition()
    {
        if (mCastControlImp != null)
        {
            return mCastControlImp.getPosition();
        }

        return null;
    }

    @Override
    public MediaInfo getMedia()
    {
        if (mCastControlImp != null)
        {
            return mCastControlImp.getMedia();
        }

        return null;
    }

}
