package com.neulion.android.demo.render;

import android.app.Application;

public class RendererApplication extends Application
{
    private static String url = "";
    private static Boolean playMode = false;

    @Override
    public void onCreate()
    {
        super.onCreate();

        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }

    public static String getUrl()
    {
        return url;
    }

    public static void setUrl(String uri)
    {
        RendererApplication.url = uri;
    }

    public static Boolean getPlayMode()
    {
        return playMode;
    }

    public static void setPlayMode(Boolean playMode)
    {
        RendererApplication.playMode = playMode;
    }

}
