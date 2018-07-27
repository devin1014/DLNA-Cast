package com.neulion.android.demo.render.upnp;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-26
 * Time: 18:19
 */
public interface IControlBridge
{
    void play();

    void pause();

    void seek(long position);

    void stop();

    void setVolume(int volume);
}
