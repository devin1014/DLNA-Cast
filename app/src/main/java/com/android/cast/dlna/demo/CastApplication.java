package com.android.cast.dlna.demo;

import android.app.Application;

import com.android.cast.dlna.ILogger;

public class CastApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new ILogger.DefaultLoggerImpl(this).i("Application onCreate!");
    }
}
