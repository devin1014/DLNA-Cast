package com.neulion.android.upnpcast.renderer;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import com.neulion.android.upnpcast.renderer.Constants.Key;
import com.neulion.android.upnpcast.renderer.localservice.AVTransportControlImp;
import com.neulion.android.upnpcast.renderer.localservice.AudioControlImp;
import com.neulion.android.upnpcast.renderer.localservice.IRendererInterface.IAVTransport;
import com.neulion.android.upnpcast.renderer.localservice.IRendererInterface.IAudioControl;
import com.neulion.android.upnpcast.renderer.localservice.RendererAVTransportService;
import com.neulion.android.upnpcast.renderer.localservice.RendererAudioControlService;
import com.neulion.android.upnpcast.renderer.localservice.RendererConnectionService;
import com.neulion.android.upnpcast.renderer.player.ICastMediaControl;
import com.neulion.android.upnpcast.renderer.player.ICastMediaControl.CastMediaControlListener;
import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-25
 * Time: 14:02
 */
public class NLUpnpRendererService extends AndroidUpnpServiceImpl
{
    public static final int NOTIFICATION_ID = 0x11;

    public static void startService(Context context)
    {
        context.getApplicationContext().startService(new Intent(context.getApplicationContext(), NLUpnpRendererService.class));
    }

    private ILogger mLogger = new DefaultLoggerImpl(this);

    private Map<UnsignedIntegerFourBytes, IAVTransport> mAVTransportControls;

    private Map<UnsignedIntegerFourBytes, IAudioControl> mAudioControls;

    private LastChange mAvTransportLastChange = new LastChange(new AVTransportLastChangeParser());

    private LastChange mAudioControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    private RendererServiceBinder mBinder = new RendererServiceBinder();

    private CastMediaControlListener mCastControlListener;

    private LocalDevice mLocalDevice;

    @Override
    protected UpnpServiceConfiguration createConfiguration()
    {
        return new AndroidUpnpServiceConfiguration()
        {
            @Override
            public int getAliveIntervalMillis()
            {
                return 60 * 1000;
            }
        };
    }

    @Override
    public void onCreate()
    {
        mLogger.w(getClass().getSimpleName() + " onCreate!!!");

        org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());

        super.onCreate();

        mCastControlListener = new CastMediaControlListener(getApplication());

        mAVTransportControls = new ConcurrentHashMap<>(1);

        for (int i = 0; i < 1; i++)
        {
            AVTransportControlImp controlImp = new AVTransportControlImp(getApplicationContext(), new UnsignedIntegerFourBytes(i), mCastControlListener);

            mAVTransportControls.put(controlImp.getInstanceId(), controlImp);
        }

        mAudioControls = new ConcurrentHashMap<>(1);

        for (int i = 0; i < 1; i++)
        {
            AudioControlImp controlImp = new AudioControlImp(getApplicationContext(), new UnsignedIntegerFourBytes(i), mCastControlListener);

            mAudioControls.put(controlImp.getInstanceId(), controlImp);
        }

        upnpService.getRegistry().addListener(mDefaultRegistryListener);

        // Now add all devices to the list we already know about
        for (Device device : upnpService.getRegistry().getDevices())
        {
            mDefaultRegistryListener.deviceAdded(upnpService.getRegistry(), device);
        }

        mLocalDevice = upnpService.getRegistry().getLocalDevice(getLocalDeviceUDN(), true);

        if (mLocalDevice == null)
        {
            try
            {
                mLocalDevice = createLocalDevice();

                mLogger.i(String.format("[create local device]: %s", mLocalDevice));

                upnpService.getRegistry().addDevice(mLocalDevice);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            startForeground(NOTIFICATION_ID, new Notification());
        }
        else
        {
            //API 18以上，发送Notification并将其置为前台后，启动InnerService
            Notification.Builder builder = new Notification.Builder(this);
            //builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(NOTIFICATION_ID, builder.build());

            startService(new Intent(this, KeepLiveInnerService.class));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mLogger.i(String.format("onStartCommand[%s]", intent));

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        mLogger.i(String.format("onBind[%s]", intent));

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        mLogger.w(String.format("onUnbind[%s]", intent));

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent)
    {
        mLogger.i(String.format("onRebind[%s]", intent));

        super.onRebind(intent);
    }

    @Override
    public void onDestroy()
    {
        mLogger.w(getClass().getSimpleName() + " onDestroy!!!");

        if (mLocalDevice != null)
        {
            upnpService.getRegistry().removeDevice(mLocalDevice);
        }

        super.onDestroy();
    }

    protected LocalDevice createLocalDevice() throws ValidationException, IOException
    {
        DeviceIdentity deviceIdentity = new DeviceIdentity(getLocalDeviceUDN());

        UDADeviceType deviceType = new UDADeviceType("MediaRenderer", 1);

        DeviceDetails deviceDetails = new DeviceDetails("NLCastRendererDemo", new ManufacturerDetails(android.os.Build.MANUFACTURER),
                new ModelDetails("NLMediaRenderer", "NLCast Renderer Demo", "1"));

        return new LocalDevice(deviceIdentity, deviceType, deviceDetails, generateDeviceIcon(), generateLocalServices());
    }

    private UDN getLocalDeviceUDN()
    {
        SharedPreferences sp = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String uuid = sp.getString(Key.DEVICE_ID, UUID.randomUUID().toString());

        if (!sp.contains(Key.DEVICE_ID))
        {
            sp.edit().putString(Key.DEVICE_ID, uuid).apply();
        }

        return new UDN(UUID.fromString(uuid));
    }

    protected Icon[] generateDeviceIcon() throws IOException
    {
        BitmapDrawable drawable = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher));

        Bitmap bitmap = drawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stream.toByteArray());

        return new Icon[]{new Icon("image/png", 48, 48, 8, "icon.png", byteArrayInputStream)};
    }

    @SuppressWarnings("unchecked")
    protected LocalService[] generateLocalServices()
    {
        final LocalServiceBinder localServiceBinder = new AnnotationLocalServiceBinder();

        // connect service
        LocalService<RendererConnectionService> connectionManagerService = localServiceBinder.read(RendererConnectionService.class);

        DefaultServiceManager<RendererConnectionService> connectManager = new DefaultServiceManager<RendererConnectionService>(connectionManagerService, RendererConnectionService.class)
        {
            @Override
            protected RendererConnectionService createServiceInstance()
            {
                return new RendererConnectionService();
            }
        };

        connectionManagerService.setManager(connectManager);

        // av transport service
        LocalService<RendererAVTransportService> avTransportService = localServiceBinder.read(RendererAVTransportService.class);

        LastChangeAwareServiceManager avLastChangeManager = new LastChangeAwareServiceManager<RendererAVTransportService>(avTransportService, new AVTransportLastChangeParser())
        {
            @Override
            protected RendererAVTransportService createServiceInstance()
            {
                return new RendererAVTransportService(mAvTransportLastChange, mAVTransportControls);
            }
        };

        avTransportService.setManager(avLastChangeManager);

        // render service
        LocalService<RendererAudioControlService> renderingControlService = localServiceBinder.read(RendererAudioControlService.class);

        LastChangeAwareServiceManager renderLastChangeManager = new LastChangeAwareServiceManager<RendererAudioControlService>(renderingControlService, new RenderingControlLastChangeParser())
        {
            @Override
            protected RendererAudioControlService createServiceInstance()
            {
                return new RendererAudioControlService(mAudioControlLastChange, mAudioControls);
            }
        };

        renderingControlService.setManager(renderLastChangeManager);

        return new LocalService[]{connectionManagerService, avTransportService, renderingControlService};
    }

    // -------------------------------------------------------------------------------------------
    // - Binder
    // -------------------------------------------------------------------------------------------
    public class RendererServiceBinder extends Binder
    {
        public NLUpnpRendererService getRendererService()
        {
            return NLUpnpRendererService.this;
        }
    }

    public LastChange getAvTransportLastChange()
    {
        return mAvTransportLastChange;
    }

    public LastChange getAudioControlLastChange()
    {
        return mAudioControlLastChange;
    }

    public LocalDevice getLocalDevice()
    {
        return mLocalDevice;
    }

    public Map<UnsignedIntegerFourBytes, IAVTransport> getAVTransportControls()
    {
        return mAVTransportControls;
    }

    public void registerControlBridge(ICastMediaControl bridge)
    {
        mCastControlListener.register(bridge);
    }

    public void unregisterControlBridge(ICastMediaControl bridge)
    {
        mCastControlListener.unregister(bridge);
    }

    // -------------------------------------------------------------------------------------------
    // - Registry listener
    // -------------------------------------------------------------------------------------------
    private DefaultRegistryListener mDefaultRegistryListener = new DefaultRegistryListener()
    {
        @Override
        public void deviceAdded(Registry registry, Device device)
        {
            mLogger.d(String.format("deviceAdded:[%s][%s]", device.getDetails().getFriendlyName(), device.getType().getType()));
        }

        @Override
        public void deviceRemoved(Registry registry, Device device)
        {
            mLogger.w(String.format("deviceRemoved:[%s][%s]", device.getDetails().getFriendlyName(), device.getType().getType()));
        }

        private Map<URL, Long> mRemoteDevice = new HashMap<>();

        @Override
        public void remoteDeviceUpdated(Registry registry, RemoteDevice device)
        {
            long now = SystemClock.currentThreadTimeMillis();

            Long value = mRemoteDevice.get(device.getIdentity().getDescriptorURL());

            if (value == null || (now - value > 30 * 1000))
            {
                mLogger.d(String.format("    remoteDeviceUpdated:[%s][%s]", device.getDetails().getFriendlyName(), device.getType().getType()));

                mRemoteDevice.put(device.getIdentity().getDescriptorURL(), now);
            }
        }
    };
}
