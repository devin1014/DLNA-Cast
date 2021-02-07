package com.android.cast.dlna.dmr.service;

import android.content.Context;
import android.media.AudioManager;

import com.android.cast.dlna.dmr.player.ICastMediaControl;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

/**
 *
 */
public class AudioRenderController implements IRendererInterface.IAudioControl {

    private static final UnsignedIntegerTwoBytes VOLUME_MUTE = new UnsignedIntegerTwoBytes(0);
    private final UnsignedIntegerFourBytes mInstanceId;
    private final ICastMediaControl mMediaControl;
    private UnsignedIntegerTwoBytes mLastVolume;
    private UnsignedIntegerTwoBytes mCurrentVolume;

    public AudioRenderController(Context context, ICastMediaControl control) {
        this(context, new UnsignedIntegerFourBytes(0), control);
    }

    public AudioRenderController(Context context, UnsignedIntegerFourBytes instanceId, ICastMediaControl control) {
        mInstanceId = instanceId;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final int MAX_MUSIC = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mCurrentVolume = new UnsignedIntegerTwoBytes(volume * 100 / MAX_MUSIC);
        mLastVolume = new UnsignedIntegerTwoBytes(volume * 100 / MAX_MUSIC);
        mMediaControl = control;
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
        //boolean mute = desiredVolume == VOLUME_MUTE || mLastVolume == VOLUME_MUTE;
        int volume = desiredVolume.getValue().intValue();
        mCurrentVolume = desiredVolume;
        mMediaControl.setVolume(volume);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(String channelName) {
        return mCurrentVolume;
    }
}
