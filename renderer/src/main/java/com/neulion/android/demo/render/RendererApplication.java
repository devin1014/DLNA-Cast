package com.neulion.android.demo.render;

import android.app.Application;

public class RendererApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }
}
