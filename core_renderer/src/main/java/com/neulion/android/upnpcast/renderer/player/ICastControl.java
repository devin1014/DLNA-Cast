package com.neulion.android.upnpcast.renderer.player;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-26
 * Time: 18:19
 */
public interface ICastControl
{
    void play();

    void pause();

    void seek(long position);

    void stop();

    void setVolume(int volume);
}
