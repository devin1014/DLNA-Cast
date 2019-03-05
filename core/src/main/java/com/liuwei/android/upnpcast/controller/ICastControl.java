package com.liuwei.android.upnpcast.controller;

import android.support.annotation.IntDef;

import com.liuwei.android.upnpcast.device.CastDevice;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
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

    PositionInfo getPosition();

    MediaInfo getMedia();
}
