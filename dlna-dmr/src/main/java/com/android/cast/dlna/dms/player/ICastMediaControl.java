package com.android.cast.dlna.dms.player;

import android.content.Context;
import android.media.AudioManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public interface ICastMediaControl {
    void play();

    void pause();

    void seek(long position);

    void stop();

    void setVolume(int volume);

    // -------------------------------------------------------------------------------------------
    // -
    // -------------------------------------------------------------------------------------------
    class CastMediaControlListener implements ICastMediaControl {
        private final List<ICastMediaControl> mListeners = new ArrayList<>();

        private final ICastMediaControl mDefaultListener;

        public CastMediaControlListener(Context context) {
            mDefaultListener = new DefaultCastMediaControlImp(context);

            register(mDefaultListener);
        }

        public void register(ICastMediaControl listener) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }

            if (mListeners.size() > 1) {
                mListeners.remove(mDefaultListener);
            }
        }

        public void unregister(ICastMediaControl listener) {
            if (mListeners.contains(listener)) {
                mListeners.remove(listener);
            }

            if (mListeners.size() == 0) {
                mListeners.add(mDefaultListener);
            }
        }

        @Override
        public void play() {
            for (ICastMediaControl bridge : mListeners) {
                bridge.play();
            }
        }

        @Override
        public void pause() {
            for (ICastMediaControl bridge : mListeners) {
                bridge.pause();
            }
        }

        @Override
        public void seek(long position) {
            for (ICastMediaControl bridge : mListeners) {
                bridge.seek(position);
            }
        }

        @Override
        public void stop() {
            for (ICastMediaControl bridge : mListeners) {
                bridge.stop();
            }
        }

        @Override
        public void setVolume(int volume) {
            for (ICastMediaControl bridge : mListeners) {
                bridge.setVolume(volume);
            }
        }

        private static class DefaultCastMediaControlImp implements ICastMediaControl {
            private final AudioManager mAudioManager;

            DefaultCastMediaControlImp(Context context) {
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }

            @Override
            public void play() {
            }

            @Override
            public void pause() {
            }

            @Override
            public void seek(long position) {
            }

            @Override
            public void stop() {
            }

            @Override
            public void setVolume(int volume) {
                int adjustVolume = volume * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100;

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustVolume, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
            }
        }
    }
}
