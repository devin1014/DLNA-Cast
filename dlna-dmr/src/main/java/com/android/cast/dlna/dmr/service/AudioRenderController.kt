package com.android.cast.dlna.dmr.service

import android.content.Context
import android.media.AudioManager
import com.android.cast.dlna.dmr.service.IRendererInterface.IAudioControl
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes

/**
 *
 */
class AudioRenderController @JvmOverloads constructor(
    context: Context,
    override val instanceId: UnsignedIntegerFourBytes = UnsignedIntegerFourBytes(0)
) : IAudioControl {

    private val muteVolume = UnsignedIntegerTwoBytes(0)
    private val audioManager: AudioManager
    private var lastVolume: UnsignedIntegerTwoBytes
    private var currentVolume: UnsignedIntegerTwoBytes

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        currentVolume = UnsignedIntegerTwoBytes(volume * 100L / maxMusicVolume)
        lastVolume = UnsignedIntegerTwoBytes(volume * 100L / maxMusicVolume)
    }

    override fun setMute(channelName: String, desiredMute: Boolean) {
        if (desiredMute) {
            lastVolume = currentVolume
        }
        setVolume(channelName, if (desiredMute) muteVolume else lastVolume)
    }

    override fun getMute(channelName: String): Boolean = getVolume(channelName).value == 0L

    override fun setVolume(channelName: String, desiredVolume: UnsignedIntegerTwoBytes) {
        currentVolume = desiredVolume
        val volume = desiredVolume.value.toInt()
        val adjustVolume = volume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustVolume, AudioManager.FLAG_PLAY_SOUND or AudioManager.FLAG_SHOW_UI)
    }

    override fun getVolume(channelName: String): UnsignedIntegerTwoBytes = currentVolume

}