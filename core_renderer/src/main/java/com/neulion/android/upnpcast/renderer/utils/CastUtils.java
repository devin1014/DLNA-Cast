package com.neulion.android.upnpcast.renderer.utils;

import android.net.Uri;
import android.text.TextUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.Locale;

public class CastUtils
{
    public static URI parseURI(String url)
    {
        try
        {
            return new URI(url);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static URI parseURI(Uri uri)
    {
        try
        {
            return new URI(uri.toString());
        }
        catch (URISyntaxException e)
        {
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
    public static String getStringTime(long timeMs)
    {
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
    public static long getIntTime(String formatTime)
    {
        if (!TextUtils.isEmpty(formatTime))
        {
            String[] tmp = formatTime.split(":");

            if (tmp.length < 3)
            {
                return 0;
            }

            int second = Integer.valueOf(tmp[0]) * 3600 + Integer.valueOf(tmp[1]) * 60 + Integer.valueOf(tmp[2]);

            return second * 1000L;
        }

        return 0;
    }

    public static long parseTime(String s)
    {
        try
        {
            return Long.parseLong(s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 0L;
    }
}
