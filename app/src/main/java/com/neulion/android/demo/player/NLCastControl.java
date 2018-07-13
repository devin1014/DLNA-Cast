package com.neulion.android.demo.player;

import android.content.Context;

import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.media.control.MediaConnection.AbstractRemoteControl;


public class NLCastControl extends AbstractRemoteControl
{
    private NLUpnpCastManager mCastManager;

    public NLCastControl(Context context, NLCastMediaConnection connection)
    {
        mCastManager = NLUpnpCastManager.getInstance();
    }

    @Override
    public void prepareAsync()
    {

    }

    @Override
    public void start()
    {

    }

    @Override
    public void pause()
    {

    }

    @Override
    public void stop()
    {

    }

    @Override
    public void release()
    {

    }

    @Override
    public void seekTo(long position)
    {

    }

    @Override
    public void setVolume(float volumeLeft, float volumeRight)
    {

    }

    @Override
    public boolean isLive()
    {
        return false;
    }

    @Override
    public boolean isPlaying()
    {
        return false;
    }

    @Override
    public boolean isPaused()
    {
        return false;
    }

    @Override
    public boolean isStopped()
    {
        return false;
    }

    @Override
    public int getVideoWidth()
    {
        return 0;
    }

    @Override
    public int getVideoHeight()
    {
        return 0;
    }

    @Override
    public long getDuration()
    {
        return 0;
    }

    @Override
    public long getCurrentPosition()
    {
        return 0;
    }
}