package com.neulion.android.demo.render.upnp;

import java.util.ArrayList;
import java.util.List;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-26
 * Time: 18:20
 */
public class ControlBridgeListener implements IControlBridge
{
    private List<IControlBridge> mListeners = new ArrayList<>();

    public void register(IControlBridge bridge)
    {
        if (!mListeners.contains(bridge))
        {
            mListeners.add(bridge);
        }
    }

    public void unregister(IControlBridge bridge)
    {
        if (mListeners.contains(bridge))
        {
            mListeners.remove(bridge);
        }
    }

    @Override
    public void play()
    {
        for (IControlBridge bridge : mListeners)
        {
            bridge.play();
        }
    }

    @Override
    public void pause()
    {
        for (IControlBridge bridge : mListeners)
        {
            bridge.pause();
        }
    }

    @Override
    public void seek(long position)
    {
        for (IControlBridge bridge : mListeners)
        {
            bridge.seek(position);
        }
    }

    @Override
    public void stop()
    {
        for (IControlBridge bridge : mListeners)
        {
            bridge.stop();
        }
    }

    @Override
    public void setVolume(int volume)
    {
        for (IControlBridge bridge : mListeners)
        {
            bridge.setVolume(volume);
        }
    }
}
