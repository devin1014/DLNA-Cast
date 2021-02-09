package com.android.cast.dlna.dmr.service;

import android.content.Context;
import android.media.AudioManager;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

/**
 *
 */
public final class AudioRenderController implements IRendererInterface.IAudioControl {

    private static final UnsignedIntegerTwoBytes VOLUME_MUTE = new UnsignedIntegerTwoBytes(0);
    private final UnsignedIntegerFourBytes mInstanceId;
    private final AudioManager mAudioManager;
    private UnsignedIntegerTwoBytes mLastVolume;
    private UnsignedIntegerTwoBytes mCurrentVolume;

    public AudioRenderController(Context context) {
        this(context, new UnsignedIntegerFourBytes(0));
    }

    public AudioRenderController(Context context, UnsignedIntegerFourBytes instanceId) {
        mInstanceId = instanceId;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final int MAX_MUSIC = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mCurrentVolume = new UnsignedIntegerTwoBytes(volume * 100 / MAX_MUSIC);
        mLastVolume = new UnsignedIntegerTwoBytes(volume * 100 / MAX_MUSIC);
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return mInstanceId;
    }

    @Override
    public void setMute(String channelName, boolean desiredMute) {
        if (desiredMute) {
            mLastVolume = mCurrentVolume;
        }
        setVolume(channelName, desiredMute ? VOLUME_MUTE : mLastVolume);
    }

    @Override
    public boolean getMute(String channelName) {
        return getVolume(channelName).getValue() == 0L;
    }

    @Override
    public void setVolume(String channelName, UnsignedIntegerTwoBytes desiredVolume) {
        mCurrentVolume = desiredVolume;
        int volume = desiredVolume.getValue().intValue();
        int adjustVolume = volume * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustVolume, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(String channelName) {
        return mCurrentVolume;
    }
}
