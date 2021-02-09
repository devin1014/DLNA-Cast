package com.android.cast.dlna.dmr;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.android.cast.dlna.core.Utils;
import com.android.cast.dlna.dmr.IDLNARenderControl.DefaultRenderControl;
import com.android.cast.dlna.dmr.service.AVTransportController;
import com.android.cast.dlna.dmr.service.AVTransportServiceImpl;
import com.android.cast.dlna.dmr.service.AudioRenderController;
import com.android.cast.dlna.dmr.service.AudioRenderServiceImpl;
import com.android.cast.dlna.dmr.service.ConnectionManagerServiceImpl;
import com.android.cast.dlna.dmr.service.RenderControlManager;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
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
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

/**
 *
 */
public class DLNARendererService extends AndroidUpnpServiceImpl {

    public static void startService(Context context) {
        context.getApplicationContext().startService(new Intent(context, DLNARendererService.class));
    }

    public static final int NOTIFICATION_ID = 0x11;
    private final RenderControlManager mRenderControlManager = new RenderControlManager();

    private LastChange mAvTransportLastChange;
    private LastChange mAudioControlLastChange;
    private final RendererServiceBinder mBinder = new RendererServiceBinder();
    private LocalDevice mRendererDevice;

    @Override
    protected UpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration() {
            @Override
            public int getAliveIntervalMillis() {
                return 5 * 1000;
            }
        };
    }

    @Override
    public void onCreate() {
        org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());
        super.onCreate();
        String ipAddress = Utils.getWiFiIPAddress(getApplicationContext());
        mRenderControlManager.addControl(new AudioRenderController(getApplicationContext()));
        mRenderControlManager.addControl(new AVTransportController(getApplicationContext(), new DefaultRenderControl()));
        try {
            mRendererDevice = createRendererDevice(getApplicationContext(), ipAddress);
            upnpService.getRegistry().addDevice(mRendererDevice);
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
            return;
        }

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        startForeground(NOTIFICATION_ID, builder.build());
        startService(new Intent(this, KeepLiveInnerService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        if (mRendererDevice != null && upnpService != null && upnpService.getRegistry() != null) {
            upnpService.getRegistry().removeDevice(mRendererDevice);
        }
        super.onDestroy();
    }

    public LastChange getAvTransportLastChange() {
        return mAvTransportLastChange;
    }

    public LastChange getAudioControlLastChange() {
        return mAudioControlLastChange;
    }

    public void setRenderControl(IDLNARenderControl control) {
        mRenderControlManager.addControl(new AVTransportController(getApplicationContext(), control));
    }

    // -------------------------------------------------------------------------------------------
    // - MediaPlayer Device
    // -------------------------------------------------------------------------------------------
    private static final String DMS_DESC = "MPI MediaPlayer";
    private static final String ID_SALT = "MediaPlayer";
    public final static String TYPE_MEDIA_PLAYER = "MediaRenderer";
    private final static int VERSION = 1;

    protected LocalDevice createRendererDevice(Context context, String ipAddress) throws ValidationException, IOException {
        DeviceIdentity deviceIdentity = new DeviceIdentity(createUniqueSystemIdentifier(ID_SALT, ipAddress));
        UDADeviceType deviceType = new UDADeviceType(TYPE_MEDIA_PLAYER, VERSION);
        DeviceDetails details = new DeviceDetails(String.format("DMR  (%s)", android.os.Build.MODEL),
                new ManufacturerDetails(android.os.Build.MANUFACTURER),
                new ModelDetails(android.os.Build.MODEL, DMS_DESC, "v1", String.format("http://%s:%s", ipAddress, "8191")));
        Icon[] icons = null;
        BitmapDrawable drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_launcher));
        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stream.toByteArray());
            icons = new Icon[]{new Icon("image/png", 48, 48, 8, "icon.png", byteArrayInputStream)};
        }
        return new LocalDevice(deviceIdentity, deviceType, details, icons, generateLocalServices());
    }

    @SuppressWarnings("unchecked")
    protected LocalService<?>[] generateLocalServices() {

        // connection
        LocalService<ConnectionManagerServiceImpl> connectionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerServiceImpl.class);
        connectionManagerService.setManager(new DefaultServiceManager<ConnectionManagerServiceImpl>(connectionManagerService, ConnectionManagerServiceImpl.class) {
            @Override
            protected ConnectionManagerServiceImpl createServiceInstance() {
                return new ConnectionManagerServiceImpl();
            }
        });

        // av transport service
        mAvTransportLastChange = new LastChange(new AVTransportLastChangeParser());
        LocalService<AVTransportServiceImpl> avTransportService = new AnnotationLocalServiceBinder().read(AVTransportServiceImpl.class);
        avTransportService.setManager(new LastChangeAwareServiceManager<AVTransportServiceImpl>(avTransportService, new AVTransportLastChangeParser()) {
            @Override
            protected AVTransportServiceImpl createServiceInstance() {
                return new AVTransportServiceImpl(mAvTransportLastChange, mRenderControlManager);
            }
        });

        // render service
        mAudioControlLastChange = new LastChange(new RenderingControlLastChangeParser());
        LocalService<AudioRenderServiceImpl> renderingControlService = new AnnotationLocalServiceBinder().read(AudioRenderServiceImpl.class);
        renderingControlService.setManager(new LastChangeAwareServiceManager<AudioRenderServiceImpl>(renderingControlService, new RenderingControlLastChangeParser()) {
            @Override
            protected AudioRenderServiceImpl createServiceInstance() {
                return new AudioRenderServiceImpl(mAudioControlLastChange, mRenderControlManager);
            }
        });

        return new LocalService[]{connectionManagerService, avTransportService, renderingControlService};
    }

    private static UDN createUniqueSystemIdentifier(@SuppressWarnings("SameParameterValue") String salt, String ipAddress) {
        StringBuilder builder = new StringBuilder();
        builder.append(ipAddress);
        builder.append(android.os.Build.MODEL);
        builder.append(android.os.Build.MANUFACTURER);
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(builder.toString().getBytes());
            return new UDN(new UUID(new BigInteger(-1, hash).longValue(), salt.hashCode()));
        } catch (Exception ex) {
            return new UDN(ex.getMessage() != null ? ex.getMessage() : "UNKNOWN");
        }
    }

    // -------------------------------------------------------------------------------------------
    // - Binder
    // -------------------------------------------------------------------------------------------
    public class RendererServiceBinder extends Binder {
        public DLNARendererService getRendererService() {
            return DLNARendererService.this;
        }
    }
}
