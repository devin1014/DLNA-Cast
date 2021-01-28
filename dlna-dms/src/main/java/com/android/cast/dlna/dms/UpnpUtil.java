package com.android.cast.dlna.dms;

import android.util.Log;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.model.item.Item;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.UUID;

public class UpnpUtil {

    public static boolean isValidDevice(Device device) {
        // if (UpnpUtil.isMediaRenderDevice(device)){
        // return true;
        // }

        if (UpnpUtil.isMediaServerDevice(device) && !UpnpUtil.isLocalIpAddress(device)) {
            return true;
        }

        return false;

    }

    public static boolean isMediaServerDevice(Device device) {
        if ("urn:schemas-upnp-org:device:MediaServer:1"
                .equalsIgnoreCase(device.getType().getType())) {
            return true;
        }
        return false;
    }

    public static boolean isMediaRenderDevice(Device device) {
        if ("urn:schemas-upnp-org:device:MediaRenderer:1".equalsIgnoreCase(device.getType()
                .getType())) {
            return true;
        }
        return false;
    }

    public final static String DLNA_OBJECTCLASS_MUSICID = "object.item.audioItem";

    public final static String DLNA_OBJECTCLASS_VIDEOID = "object.item.videoItem";

    public final static String DLNA_OBJECTCLASS_PHOTOID = "object.item.imageItem";

    private static final String TAG = "UpnpUtil";

    public static boolean isAudioItem(Item item) {
        // TODO zxt need check?
        String objectClass = item.getId();
        if (objectClass != null && objectClass.contains(DLNA_OBJECTCLASS_MUSICID)) {
            return true;
        }
        return false;
    }

    public static boolean isVideoItem(Item item) {
        // TODO zxt need check?
        String objectClass = item.getId();
        if (objectClass != null && objectClass.contains(DLNA_OBJECTCLASS_VIDEOID)) {
            return true;
        }
        return false;
    }

    public static boolean isPictureItem(Item item) {
        // TODO zxt need check?
        String objectClass = item.getId();
        if (objectClass != null && objectClass.contains(DLNA_OBJECTCLASS_PHOTOID)) {
            return true;
        }
        return false;
    }

    public static boolean isLocalIpAddress(Device device) {
        try {
            String addrip = device.getDetails().getBaseURL().toString();
            addrip = addrip.substring("http://".length(), addrip.length());
            addrip = addrip.substring(0, addrip.indexOf(":"));
            boolean ret = isLocalIpAddress(addrip);
            ret = false;
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isLocalIpAddress(String checkip) {

        boolean ret = false;
        if (checkip != null) {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                        .hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ip = inetAddress.getHostAddress().toString();

                            if (ip == null) {
                                continue;
                            }
                            if (checkip.equals(ip)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    public static UDN uniqueSystemIdentifier(String salt, String ipAddress) {
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

    public static String getIP() throws SocketException {
        String ipaddress = "";
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                .hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            if (intf.getName().toLowerCase().equals("eth0")
                    || intf.getName().toLowerCase().equals("wlan0")) {
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipaddress = inetAddress.getHostAddress().toString();
                        if (!ipaddress.contains("::")) {// ipV6的地址
                            Log.e(TAG, ipaddress);
                            return ipaddress;
                        }
                    }
                }
            } else {
                continue;
            }
        }
        return ipaddress;
    }
}
