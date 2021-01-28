
package com.android.cast.dlna.dms;

import android.content.Context;

import androidx.annotation.Nullable;

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
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;

import java.io.IOException;

public final class MediaServer {

    //TODO:remove local device field?
    private LocalDevice mDevice;
    private HttpServer mHttpServer;
    private final String mInetAddress;
    private final String mBaseUrl;

    public MediaServer(Context context) {
        String address = Utils.getWiFiIPAddress(context);
        mInetAddress = String.format("%s:%s", address, PORT);
        mBaseUrl = String.format("http://%s:%s", address, PORT);
        try {
            mDevice = createLocalDevice(context, address);
            mHttpServer = new HttpServer(PORT);
        } catch (ValidationException | IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (mHttpServer != null) {
            mHttpServer.start();
        }
    }

    public void stop() {
        if (mHttpServer != null) {
            mHttpServer.stop();
        }
    }

    public String getInetAddress() {
        return mInetAddress;
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    @Nullable
    public LocalDevice getDevice() {
        return mDevice;
    }

    private static final String DMS_DESC = "MSI MediaServer";
    private static final String ID_SALT = "GNaP-MediaServer";
    public final static String TYPE_MEDIA_SERVER = "MediaServer";
    private final static int VERSION = 1;
    private final static int PORT = 8192;

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected LocalDevice createLocalDevice(Context context, String ipAddress) throws ValidationException {
        DeviceIdentity identity = new DeviceIdentity(UpnpUtil.uniqueSystemIdentifier(ID_SALT, ipAddress));
        DeviceType type = new UDADeviceType(TYPE_MEDIA_SERVER, VERSION);
        DeviceDetails details = new DeviceDetails(String.format("DMS  (%s)", android.os.Build.MODEL),
                new ManufacturerDetails(android.os.Build.MANUFACTURER),
                new ModelDetails(android.os.Build.MODEL, DMS_DESC, "v1", mBaseUrl));
        final LocalService<?> service = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
        service.setManager(new DefaultServiceManager(service, ContentDirectoryService.class));
        Icon icon = null;
        try {
            icon = new Icon("image/png", 48, 48, 32, "msi.png",
                    context.getResources().getAssets().open("ic_launcher.png"));
        } catch (IOException ignored) {
        }
        return new LocalDevice(identity, type, details, icon, service);
    }
}
