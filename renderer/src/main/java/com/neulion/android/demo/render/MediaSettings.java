package com.neulion.android.demo.render;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MediaSettings
{
    public static String getDeviceName(Context context)
    {
        return getSharedPreferences(context).getString("deName", android.os.Build.MODEL + " Renderer");
    }

    private static SharedPreferences getSharedPreferences(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
