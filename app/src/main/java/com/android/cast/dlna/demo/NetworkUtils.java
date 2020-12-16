package com.android.cast.dlna.demo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 *
 */
public class NetworkUtils {
    public static String getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = getSystemService(context, Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return getWifiInfo(context);
            } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return context.getResources().getString(R.string.not_connect_wifi);
            }
        }
        return null;
    }

    private static String getWifiInfo(Context context) {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String result = "WIFI:\r" + wifiInfo.getSSID() + "\n";
        int address = wifiInfo.getIpAddress();
        String ip = (address & 0xFF) + "." + ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF) + "." + (address >> 24 & 0xFF);
        return result + "IP:\r" + ip;
    }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject"})
    private static <T extends Object> T getSystemService(Context context, String name) {
        return (T) context.getApplicationContext().getSystemService(name);
    }
}
