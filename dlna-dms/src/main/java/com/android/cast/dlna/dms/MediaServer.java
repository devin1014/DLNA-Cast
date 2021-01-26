
package com.android.cast.dlna.dms;

import android.content.Context;
import android.util.Log;

import com.orhanobut.logger.Logger;

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

public class MediaServer {

    public static final String DMS_DESC = "MSI MediaServer";
    private final static String MEDIA_SERVER = "MediaServer";
    private final static String TAG = "MediaServer";
    private final static int VERSION = 1;
    public final static int PORT = 8192;

    private final LocalDevice mLocalDevice;

    public MediaServer(Context context, String ipAddress) throws ValidationException {
        Logger.i("MediaServer start");
        DeviceType type = new UDADeviceType(MEDIA_SERVER, VERSION);
        DeviceDetails details = new DeviceDetails("DMS  (" + android.os.Build.MODEL + ")",
                new ManufacturerDetails(android.os.Build.MANUFACTURER),
                new ModelDetails(android.os.Build.MODEL, DMS_DESC, "v1", String.format("http://%s:%s", ipAddress, PORT)));
        final LocalService<?> service = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
        //noinspection unchecked,rawtypes
        service.setManager(new DefaultServiceManager(service, ContentDirectoryService.class));
        mLocalDevice = new LocalDevice(new DeviceIdentity(UpnpUtil.uniqueSystemIdentifier("GNaP-MediaServer")), type, details, createDefaultDeviceIcon(context), service);
        Logger.i("LocalDevice: " + mLocalDevice.toString());

        // start http server
        try {
            new HttpServer(PORT).start();
            Logger.e("Http Server started on port %s", PORT);
        } catch (IOException ioe) {
            Logger.e(ioe, "Couldn't start server.");
            System.exit(-1);
        }
    }

    public final LocalDevice getDevice() {
        return mLocalDevice;
    }

    protected Icon createDefaultDeviceIcon(Context context) {
        try {
            return new Icon("image/png", 48, 48, 32, "msi.png", context.getResources().getAssets().open("ic_launcher.png"));
        } catch (IOException e) {
            Log.w(TAG, "createDefaultDeviceIcon IOException");
            return null;
        }
    }

}
