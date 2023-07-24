package com.android.cast.dlna.dmr.service

import android.content.Context
import android.media.AudioManager
import com.android.cast.dlna.core.Logger
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes

/**
 *
 */
class AudioRenderController constructor(context: Context) : AudioControl {

    private val muteVolume = UnsignedIntegerTwoBytes(0)
    private val audioManager: AudioManager
    private var currentVolume: UnsignedIntegerTwoBytes

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        currentVolume = UnsignedIntegerTwoBytes(volume * 100L / maxMusicVolume)
    }

    override val logger: Logger = Logger.create("AudioRenderController")

    override fun setMute(channelName: String, desiredMute: Boolean) {
        super.setMute(channelName, desiredMute)
        setVolume(channelName, if (desiredMute) muteVolume else currentVolume)
    }

    override fun getMute(channelName: String): Boolean = getVolume(channelName).value == 0L

    override fun setVolume(channelName: String, desiredVolume: UnsignedIntegerTwoBytes) {
        super.setVolume(channelName, desiredVolume)
        currentVolume = desiredVolume
        val volume = desiredVolume.value.toInt()
        val adjustVolume = volume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustVolume, AudioManager.FLAG_PLAY_SOUND or AudioManager.FLAG_SHOW_UI)
    }

    override fun getVolume(channelName: String): UnsignedIntegerTwoBytes =
        UnsignedIntegerTwoBytes(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100L / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
}
