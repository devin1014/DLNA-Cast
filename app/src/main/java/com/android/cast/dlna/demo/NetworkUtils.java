package com.android.cast.dlna.demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.core.app.ActivityCompat;

/**
 *
 */
public class NetworkUtils {
    public static String getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = getSystemService(context, Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return getWiFiSSID(context);
            } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return context.getResources().getString(R.string.not_connect_wifi);
            }
        }
        return null;
    }

    public static String getWiFiSSID(Context context) {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSSID().equals(WifiManager.UNKNOWN_SSID)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "No 'ACCESS_FINE_LOCATION' Permission";
            }
            if (wifiManager.getConfiguredNetworks() != null) {
                for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
                    if (config.networkId == wifiInfo.getNetworkId()) {
                        return String.format("WIFI: %s", config.SSID.replaceAll("\"", ""));
                    }
                }
            }
            return String.format("WIFI: %s", WifiManager.UNKNOWN_SSID);
        } else {
            return String.format("WIFI: %s", wifiInfo.getSSID().replaceAll("\"", ""));
        }
    }

    // public static String getWiFiIPAddress(Context context) {
    //     return getWiFiIPAddress(context, "");
    // }
    //
    // public static String getWiFiIPAddress(Context context, String prefixString) {
    //     WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
    //     WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    //     int address = wifiInfo.getIpAddress();
    //     String ip = (address & 0xFF) + "." + ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF) + "." + (address >> 24 & 0xFF);
    //     return prefixString + ip;
    // }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject"})
    private static <T extends Object> T getSystemService(Context context, String name) {
        return (T) context.getApplicationContext().getSystemService(name);
    }
}
