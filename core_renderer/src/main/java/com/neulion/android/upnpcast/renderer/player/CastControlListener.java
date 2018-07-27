package com.neulion.android.upnpcast.renderer.player;

import android.content.Context;
import android.media.AudioManager;

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

    private ICastControl mDefaultListener;

    public CastControlListener(Context context)
    {
        mDefaultListener = new DefaultCastControlImp(context);

        register(mDefaultListener);
    }

    public void register(ICastControl listener)
    {
        if (!mListeners.contains(listener))
        {
            mListeners.add(listener);
        }

        if (mListeners.size() > 1)
        {
            mListeners.remove(mDefaultListener);
        }
    }

    public void unregister(ICastControl listener)
    {
        if (mListeners.contains(listener))
        {
            mListeners.remove(listener);
        }

        if (mListeners.size() == 0)
        {
            mListeners.add(mDefaultListener);
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

    private class DefaultCastControlImp implements ICastControl
    {
        private AudioManager mAudioManager;

        DefaultCastControlImp(Context context)
        {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        @Override
        public void play()
        {
        }

        @Override
        public void pause()
        {
        }

        @Override
        public void seek(long position)
        {
        }

        @Override
        public void stop()
        {
        }

        @Override
        public void setVolume(int volume)
        {
            int adjustVolume = volume * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100;

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustVolume, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
        }
    }
}
