package com.neulion.android.upnpcast.controller;

import com.neulion.android.upnpcast.controller.CastControlImp.CastStatus;
import com.neulion.android.upnpcast.device.CastDevice;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 11:27
 */
public interface ICastControl
{
    // device control
    void connect(CastDevice castDevice);

    void disconnect();

    boolean isConnected();

    // media control
    void cast(CastObject castObject);

    void start();

    void pause();

    void stop();

    void seekTo(int position);

    void setVolume(int percent);

    void setBrightness(int percent);

    @CastStatus
    int getCastStatus();
}
