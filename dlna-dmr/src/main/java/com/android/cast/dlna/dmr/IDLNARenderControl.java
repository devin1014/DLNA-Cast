package com.android.cast.dlna.dmr;

import android.content.Context;
import android.media.AudioManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public interface IDLNARenderControl {
    void play();

    void pause();

    void seek(long position);

    void stop();

    void setVolume(int volume);

    // -------------------------------------------------------------------------------------------
    // -
    // -------------------------------------------------------------------------------------------
    class CastMediaControlListener implements IDLNARenderControl {
        private final List<IDLNARenderControl> mListeners = new ArrayList<>();

        private final IDLNARenderControl mDefaultListener;

        public CastMediaControlListener(Context context) {
            mDefaultListener = new DefaultCastMediaControlImp(context);

            register(mDefaultListener);
        }

        public void register(IDLNARenderControl listener) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }

            if (mListeners.size() > 1) {
                mListeners.remove(mDefaultListener);
            }
        }

        public void unregister(IDLNARenderControl listener) {
            if (mListeners.contains(listener)) {
                mListeners.remove(listener);
            }

            if (mListeners.size() == 0) {
                mListeners.add(mDefaultListener);
            }
        }

        @Override
        public void play() {
            for (IDLNARenderControl bridge : mListeners) {
                bridge.play();
            }
        }

        @Override
        public void pause() {
            for (IDLNARenderControl bridge : mListeners) {
                bridge.pause();
            }
        }

        @Override
        public void seek(long position) {
            for (IDLNARenderControl bridge : mListeners) {
                bridge.seek(position);
            }
        }

        @Override
        public void stop() {
            for (IDLNARenderControl bridge : mListeners) {
                bridge.stop();
            }
        }

        @Override
        public void setVolume(int volume) {
            for (IDLNARenderControl bridge : mListeners) {
                bridge.setVolume(volume);
            }
        }

        private static class DefaultCastMediaControlImp implements IDLNARenderControl {
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
