
package com.android.cast.dlna.dms;

import android.content.Context;
import android.util.Log;

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
import org.fourthline.cling.model.types.UDN;

import java.io.IOException;

public class MediaServer {

    private UDN udn = UpnpUtil.uniqueSystemIdentifier("GNaP-MediaServer");
    private LocalDevice localDevice;
    private final static String deviceType = "MediaServer";
    private final static int version = 1;
    private final static String TAG = "MediaServer";
    public final static int PORT = 8192;
    private Context mContext;
    public static final String DMS_DESC = "MSI MediaServer";
    public static final String DMR_DESC = "MSI MediaRenderer";

    public MediaServer(Context context) throws ValidationException {
        mContext = context;
        DeviceType type = new UDADeviceType(deviceType, version);
        DeviceDetails details = new DeviceDetails("DMS  (" + android.os.Build.MODEL + ")", new ManufacturerDetails(
                android.os.Build.MANUFACTURER), new ModelDetails(android.os.Build.MODEL, DMS_DESC, "v1"));

        LocalService service = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);

        service.setManager(new DefaultServiceManager(service, ContentDirectoryService.class));

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, createDefaultDeviceIcon(), service);

        Log.v(TAG, "MediaServer device created: ");
        Log.v(TAG, "friendly name: " + details.getFriendlyName());
        Log.v(TAG, "manufacturer: " + details.getManufacturerDetails().getManufacturer());
        Log.v(TAG, "model: " + details.getModelDetails().getModelName());

        // start http server
        try {
            new HttpServer(PORT);
        } catch (IOException ioe) {
            Log.e(TAG, "Couldn't start server:\n" + ioe);
            System.exit(-1);
        }
        Log.e(TAG, "Started Http Server on port " + PORT);
    }

    public LocalDevice getDevice() {
        return localDevice;
    }

    public static String getAddress() {
        return "Application.getHostAddress():TODO" + ":" + PORT;
    }

    protected Icon createDefaultDeviceIcon() {
        try {
            return new Icon("image/png", 48, 48, 32, "msi.png", mContext.getResources().getAssets().open(FileUtil.LOGO));
        } catch (IOException e) {
            Log.w(TAG, "createDefaultDeviceIcon IOException");
            return null;
        }
    }

}
