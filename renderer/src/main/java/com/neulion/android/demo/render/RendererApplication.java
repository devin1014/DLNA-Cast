package com.neulion.android.demo.render;

import android.app.Application;

import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

public class RendererApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

        new DefaultLoggerImpl(this).i("RendererApplication onCreate!!!");
    }
}
