package com.neulion.android.upnpcast.controller;

import android.support.annotation.IntDef;

import com.neulion.android.upnpcast.device.CastDevice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 11:27
 */
public interface ICastControl
{
    int IDLE = 0;
    int CASTING = 1;
    int PLAY = 2;
    int PAUSE = 3;
    int STOP = 4;
    int BUFFER = 5;
    int ERROR = 6;

    @IntDef({IDLE, CASTING, PLAY, PAUSE, STOP, BUFFER, ERROR})
    @Retention(RetentionPolicy.SOURCE)
    @interface CastStatus
    {
    }

    // device control
    void connect(CastDevice castDevice);

    void disconnect();

    boolean isConnected();

    // media control
    void cast(CastObject castObject);

    void start();

    void pause();

    void stop();

    void seekTo(long position);

    void setVolume(int percent);

    void setBrightness(int percent);

    @CastStatus
    int getCastStatus();
}
