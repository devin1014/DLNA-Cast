package com.neulion.android.upnpcast;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.neulion.android.upnpcast.NLDeviceRegistryListener.OnRegistryDeviceListener;
import com.neulion.android.upnpcast.controller.CastControlImp;
import com.neulion.android.upnpcast.controller.CastObject;
import com.neulion.android.upnpcast.controller.ICastControlListener;
import com.neulion.android.upnpcast.device.CastDevice;
import com.neulion.android.upnpcast.service.NLUpnpCastService;
import com.neulion.android.upnpcast.service.UpnpCastServiceConnection;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.RegistryListener;

import java.util.Collection;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-06-29
 * Time: 18:28
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
    //    public static final ServiceType CONTENT_DIRECTORY_SERVICE = new UDAServiceType("ContentDirectory");
    public static final ServiceType SERVICE_AV_TRANSPORT = new UDAServiceType("AVTransport");
    public static final ServiceType SERVICE_RENDERING_CONTROL = new UDAServiceType("RenderingControl");

    private NLUpnpCastService mUpnpCastService;

    private ILogger mLogger = new DefaultLoggerImpl(NLUpnpCastManager.class.getSimpleName(), Constants.DEBUG);

    private NLUpnpCastManager()
    {
    }

    public void bindUpnpCastService(Activity activity)
    {
        if (activity != null)
        {
            activity.bindService(new Intent(activity, NLUpnpCastService.class), mUpnpCastServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    public void unbindUpnpCastService(Activity activity)
    {
        if (activity != null)
        {
            activity.unbindService(mUpnpCastServiceConnection);
        }
    }

    private UpnpCastServiceConnection mUpnpCastServiceConnection = new UpnpCastServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, NLUpnpCastService service)
        {
            mLogger.i("onServiceConnected:" + componentName.getClassName());

            mUpnpCastService = service;

            initUpnpCastService(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mLogger.w("onServiceDisconnected:" + componentName.getClassName());

            resetUpnpCastService(mUpnpCastService);

            mUpnpCastService = null;
        }
    };

    private void initUpnpCastService(@NonNull NLUpnpCastService service)
    {
        Collection<RegistryListener> collection = service.getRegistry().getListeners();

        if (collection == null || !collection.contains(mNLDeviceRegistryListener))
        {
            service.getRegistry().addListener(mNLDeviceRegistryListener);
        }

        if (mCastControlImp != null)
        {
            mCastControlImp.setNLUpnpCastService(service);

            mCastControlImp.syncCasting();
        }
    }

    private void resetUpnpCastService(NLUpnpCastService service)
    {
        if (service != null)
        {
            Collection<RegistryListener> collection = service.getRegistry().getListeners();

            if (collection != null && collection.contains(mNLDeviceRegistryListener))
            {
                service.getRegistry().removeListener(mNLDeviceRegistryListener);
            }
        }
    }

    public void addRegistryDeviceListener(OnRegistryDeviceListener listener)
    {
        mNLDeviceRegistryListener.addRegistryDeviceListener(listener);
    }

    public void removeRegistryListener(OnRegistryDeviceListener listener)
    {
        mNLDeviceRegistryListener.removeRegistryListener(listener);
    }

    private NLDeviceRegistryListener mNLDeviceRegistryListener = new NLDeviceRegistryListener();

    // ------------------------------------------------------------------------------------------
    // control
    // ------------------------------------------------------------------------------------------
    @Override
    public void search(DeviceType type)
    {
        search(type, IUpnpCast.DEFAULT_MAX_SECONDS);
    }

    @Override
    public void search(DeviceType type, int maxSeconds)
    {
        mNLDeviceRegistryListener.setSearchDeviceType(type);

        if (mUpnpCastService != null)
        {
            mUpnpCastService.get().getControlPoint().search(maxSeconds);
        }
    }

    private ICastControlListener mControlListener;

    public void setOnControlListener(ICastControlListener listener)
    {
        mControlListener = listener;
    }

    private CastControlImp mCastControlImp;

    @Override
    public void connect(CastDevice castDevice)
    {
        if (mCastControlImp == null)
        {
            mCastControlImp = new CastControlImp(mUpnpCastService, mControlListener);
        }

        mCastControlImp.connect(castDevice);
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
    public void seekTo(int position)
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
    public int getCastStatus()
    {
        if (mCastControlImp != null)
        {
            return mCastControlImp.getCastStatus();
        }

        return CastControlImp.IDLE;
    }
}
