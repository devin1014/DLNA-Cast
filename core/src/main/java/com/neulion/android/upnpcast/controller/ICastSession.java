package com.neulion.android.upnpcast.controller;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-18
 * Time: 14:33
 */
public interface ICastSession
{
    void start();

    void stop();

    boolean isRunning();
}
