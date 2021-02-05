package com.android.cast.dlna.dms;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.Locale;

public class CastUtils {
    private CastUtils() {
    }

    //TODO:check auth or multiple ip
    public static String getWiFiIPAddress(Context context) {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int address = wifiInfo.getIpAddress();
            return (address & 0xFF) + "." + ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF) + "." + (address >> 24 & 0xFF);
        } else {
            return "unknown";
        }
    }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject", "SameParameterValue"})
    private static <T extends Object> T getSystemService(Context context, String name) {
        return (T) context.getApplicationContext().getSystemService(name);
    }

    public static URI parseURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static URI parseURI(Uri uri) {
        try {
            return new URI(uri.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 把时间戳转换成 00:00:00 格式
     *
     * @param timeMs 时间戳
     * @return 00:00:00 时间格式
     */
    public static String getStringTime(long timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.US);

        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        return formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
    }

    /**
     * 把 00:00:00 格式转成时间戳
     *
     * @param formatTime 00:00:00 时间格式
     * @return 时间戳(毫秒)
     */
    public static long getIntTime(String formatTime) {
        if (!TextUtils.isEmpty(formatTime)) {
            String[] tmp = formatTime.split(":");

            if (tmp.length < 3) {
                return 0;
            }

            int second = Integer.valueOf(tmp[0]) * 3600 + Integer.valueOf(tmp[1]) * 60 + Integer.valueOf(tmp[2]);

            return second * 1000L;
        }

        return 0;
    }

    public static long parseTime(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;
    }

    public static TransportInfo getTransportInfo(int state) {
        //TODO
        // switch (state)
        // {
        //     case MediaControl.STATE_PREPARING:
        //
        //         return new TransportInfo(TransportState.TRANSITIONING);
        //
        //     case MediaControl.STATE_PREPARED:
        //     case MediaControl.STATE_PLAYING:
        //
        //         return new TransportInfo(TransportState.PLAYING);
        //
        //     case MediaControl.STATE_PAUSED:
        //
        //         return new TransportInfo(TransportState.PAUSED_PLAYBACK);
        //
        //     case MediaControl.STATE_COMPLETED:
        //     case MediaControl.STATE_ERROR:
        //
        //         return new TransportInfo(TransportState.STOPPED);
        // }

        return new TransportInfo(TransportState.STOPPED);
    }
}
