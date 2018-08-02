package com.neulion.android.demo.render;

import android.app.Application;

import com.neulion.android.upnpcast.renderer.Constants;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

public class RendererApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        new DefaultLoggerImpl(this).i(getClass().getSimpleName() + " onCreate!!!");

        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

        if (Constants.DEBUG)
        {
            //Logger.getLogger("org.fourthline.cling").setLevel(Level.FINEST);
        }
    }
}
