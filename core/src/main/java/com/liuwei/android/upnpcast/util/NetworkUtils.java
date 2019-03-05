package com.liuwei.android.upnpcast.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 */
public class NetworkUtils
{
    public static String getActiveNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = getSystemService(context, Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                return getWifiInfo(context);
            }
        }

        return null;
    }

    private static String getWifiInfo(Context context)
    {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String result = "WIFI:" + wifiInfo.getSSID();

        int address = wifiInfo.getIpAddress();

        String ip = (address & 0xFF) + "." + ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF) + "." + (address >> 24 & 0xFF);

        return result + ", IP:" + ip;
    }

    //    public static String getMobileIP()
    //    {
    //        try
    //        {
    //            for (Enumeration<NetworkInterface> en = NetworkInterface
    //                    .getNetworkInterfaces(); en.hasMoreElements(); )
    //            {
    //                NetworkInterface intf = en.nextElement();
    //                for (Enumeration<InetAddress> enumIpAddr = intf
    //                        .getInetAddresses(); enumIpAddr.hasMoreElements(); )
    //                {
    //                    InetAddress inetAddress = enumIpAddr.nextElement();
    //                    if (!inetAddress.isLoopbackAddress())
    //                    {
    //                        return inetAddress.getHostAddress().toString();
    //                    }
    //                }
    //            }
    //        }
    //        catch (SocketException ex)
    //        {
    //            Log.e("哎呀，出错了...", ex.toString());
    //        }
    //        return null;
    //    }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject"})
    private static <T extends Object> T getSystemService(Context context, String name)
    {
        return (T) context.getApplicationContext().getSystemService(name);
    }
}
