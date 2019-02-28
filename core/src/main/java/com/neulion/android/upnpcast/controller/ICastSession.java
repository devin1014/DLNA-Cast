package com.neulion.android.upnpcast.controller;

/**
 */
public interface ICastSession
{
    void start();

    void stop();

    boolean isRunning();
}
