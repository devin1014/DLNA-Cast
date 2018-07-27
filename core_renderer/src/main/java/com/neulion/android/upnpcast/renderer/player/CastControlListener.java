package com.neulion.android.upnpcast.renderer.player;

import java.util.ArrayList;
import java.util.List;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-26
 * Time: 18:20
 */
public class CastControlListener implements ICastControl
{
    private List<ICastControl> mListeners = new ArrayList<>();

    public void register(ICastControl bridge)
    {
        if (!mListeners.contains(bridge))
        {
            mListeners.add(bridge);
        }
    }

    public void unregister(ICastControl bridge)
    {
        if (mListeners.contains(bridge))
        {
            mListeners.remove(bridge);
        }
    }

    @Override
    public void play()
    {
        for (ICastControl bridge : mListeners)
        {
            bridge.play();
        }
    }

    @Override
    public void pause()
    {
        for (ICastControl bridge : mListeners)
        {
            bridge.pause();
        }
    }

    @Override
    public void seek(long position)
    {
        for (ICastControl bridge : mListeners)
        {
            bridge.seek(position);
        }
    }

    @Override
    public void stop()
    {
        for (ICastControl bridge : mListeners)
        {
            bridge.stop();
        }
    }

    @Override
    public void setVolume(int volume)
    {
        for (ICastControl bridge : mListeners)
        {
            bridge.setVolume(volume);
        }
    }
}
