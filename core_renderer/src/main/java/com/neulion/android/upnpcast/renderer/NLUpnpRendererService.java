package com.neulion.android.upnpcast.renderer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;

import com.neulion.android.upnpcast.renderer.localservice.AVTransportControlImp;
import com.neulion.android.upnpcast.renderer.localservice.AudioControlImp;
import com.neulion.android.upnpcast.renderer.localservice.IAudioControl;
import com.neulion.android.upnpcast.renderer.localservice.IRendererInterface.IAVTransport;
import com.neulion.android.upnpcast.renderer.localservice.RendererAVTransportService;
import com.neulion.android.upnpcast.renderer.localservice.RendererAudioControlService;
import com.neulion.android.upnpcast.renderer.localservice.RendererConnectionManagerService;
import com.neulion.android.upnpcast.renderer.player.CastControlListener;
import com.neulion.android.upnpcast.renderer.player.ICastControl;
import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private ILogger mLogger = new DefaultLoggerImpl(this);

    private Map<UnsignedIntegerFourBytes, IAVTransport> mAVTransportControls;

    private Map<UnsignedIntegerFourBytes, IAudioControl> mAudioControls;

    private LastChange mAvTransportLastChange = new LastChange(new AVTransportLastChangeParser());

    private LastChange mAudioControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    private RendererServiceBinder mBinder = new RendererServiceBinder();

    private CastControlListener mCastControlListener = new CastControlListener();

    private LocalDevice mLocalDevice;

    @Override
    public void onCreate()
    {
        mLogger.i("onCreate");

        super.onCreate();

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

        try
        {
            mLocalDevice = createLocalDevice();

            mLogger.i("create Device: " + mLocalDevice);

            upnpService.getRegistry().addDevice(mLocalDevice);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mLogger.i(String.format("onStartCommand[%s,%s,%s]", intent, flags, startId));

        return super.onStartCommand(intent, flags, startId);
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
        mLogger.w("onDestroy");

        if (mLocalDevice != null)
        {
            upnpService.getRegistry().removeDevice(mLocalDevice);
        }

        super.onDestroy();
    }

    protected LocalDevice createLocalDevice() throws ValidationException, IOException
    {
        String key = getPackageName() + ".key.deviceUUID";

        SharedPreferences sp = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String uuid = sp.getString(key, UUID.randomUUID().toString());

        if (!sp.contains(key))
        {
            sp.edit().putString(key, uuid).apply();
        }

        DeviceIdentity deviceIdentity = new DeviceIdentity(new UDN(UUID.fromString(uuid)));

        UDADeviceType deviceType = new UDADeviceType("MediaRenderer", 1);

        DeviceDetails deviceDetails = new DeviceDetails("NLMediaRenderer Demo", new ManufacturerDetails(android.os.Build.MANUFACTURER),
                new ModelDetails("MediaRenderer", "MediaRenderer", "1"));

        return new LocalDevice(deviceIdentity, deviceType, deviceDetails, generateDeviceIcon(), generateLocalServices());
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
        LocalService<RendererConnectionManagerService> connectionManagerService = localServiceBinder.read(RendererConnectionManagerService.class);

        DefaultServiceManager<RendererConnectionManagerService> connectManager = new DefaultServiceManager<RendererConnectionManagerService>(connectionManagerService, RendererConnectionManagerService.class)
        {
            @Override
            protected RendererConnectionManagerService createServiceInstance()
            {
                return new RendererConnectionManagerService();
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

    public Map<UnsignedIntegerFourBytes, IAudioControl> getAudioControls()
    {
        return mAudioControls;
    }

    public void registerControlBridge(ICastControl bridge)
    {
        mCastControlListener.register(bridge);
    }

    public void unregisterControlBridge(ICastControl bridge)
    {
        mCastControlListener.unregister(bridge);
    }
}
