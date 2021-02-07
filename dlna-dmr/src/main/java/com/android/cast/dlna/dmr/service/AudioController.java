package com.android.cast.dlna.dmr.service;

import android.content.Context;
import android.media.AudioManager;

import com.android.cast.dlna.dmr.player.ICastMediaControl;
import com.android.cast.dlna.dmr.ILogger;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

/**
 *
 */
public class AudioController implements IRendererInterface.IAudioControl {
    private final UnsignedIntegerFourBytes mInstanceId;
    private final UnsignedIntegerTwoBytes VOLUME_MUTE = new UnsignedIntegerTwoBytes(0);
    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private final ICastMediaControl mControlListener;
    private UnsignedIntegerTwoBytes mLastVolume;
    private UnsignedIntegerTwoBytes mCurrentVolume;

    @SuppressWarnings("ConstantConditions")
    public AudioController(Context context, UnsignedIntegerFourBytes instanceId, ICastMediaControl listener) {
        mInstanceId = instanceId;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int MAX_MUSIC = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mCurrentVolume = new UnsignedIntegerTwoBytes(volume * 100 / MAX_MUSIC);
        mLastVolume = new UnsignedIntegerTwoBytes(volume * 100 / MAX_MUSIC);
        mControlListener = listener;
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return mInstanceId;
    }

    @Override
    public void setMute(String channelName, boolean desiredMute) {
        mLogger.d(String.format("setMute [%s][%s]", channelName, desiredMute));
        if (desiredMute) {
            mLastVolume = mCurrentVolume;
        }
        setVolume(channelName, desiredMute ? VOLUME_MUTE : mLastVolume);
    }

    @Override
    public boolean getMute(String channelName) {
        mLogger.d(String.format("getMute [%s]", channelName));
        return getVolume(channelName).getValue() == 0L;
    }

    @Override
    public void setVolume(String channelName, UnsignedIntegerTwoBytes desiredVolume) {
        mLogger.d(String.format("setVolume [%s][%s]", channelName, desiredVolume));
        //boolean mute = desiredVolume == VOLUME_MUTE || mLastVolume == VOLUME_MUTE;
        int volume = desiredVolume.getValue().intValue();
        mCurrentVolume = desiredVolume;
        mControlListener.setVolume(volume);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(String channelName) {
        mLogger.d(String.format("getVolume [%s][%s]", channelName, mCurrentVolume));
        return mCurrentVolume;
    }
}
