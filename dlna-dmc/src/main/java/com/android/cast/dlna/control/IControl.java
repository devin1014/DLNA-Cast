package com.android.cast.dlna.control;

import com.android.cast.dlna.controller.CastObject;

public interface IControl {
    void cast(CastObject castObject);

    void play();

    void pause();

    void stop();

    void seekTo(long position);

    void setVolume(int percent);

    void setMute(boolean mute);

    void setBrightness(int percent);
}
