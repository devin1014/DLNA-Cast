package com.android.cast.dlna.demo.renderer

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.PlayerView
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.demo.renderer.util.Utils
import com.android.cast.dlna.dmr.BaseRendererActivity
import com.android.cast.dlna.dmr.RenderControl
import com.android.cast.dlna.dmr.RenderState

class ExoPlayerRendererActivity : BaseRendererActivity() {

    private val log = Logger.create("ExoPlayerRendererActivity")
    private val exoPlayer: Player by lazy { initExoPlayer() }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.video_progress) }
    private val errorMsg: TextView by lazy { findViewById(R.id.video_error) }
    private var renderState: RenderState = RenderState.IDLE
        set(value) {
            if (field != value) {
                field = value
                rendererService?.notifyAvTransportLastChange(field)
            }
        }

    override fun onServiceConnected() {
        rendererService?.bindRealPlayer(ExoPlayerRenderControl(exoPlayer))
        openMedia()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exoplayer_renderer)
        initComponent()
        rendererService?.run {
            openMedia()
        }
    }

    private fun initExoPlayer(): Player {
        return ExoPlayer.Builder(this).build().apply {
            addAnalyticsListener(EventLogger())
        }
    }


    @SuppressLint("SetTextI18n")
    private fun initComponent() {
        // 方便在平板上调试，模拟遥控器
        findViewById<View>(R.id.player_action_bar).visibility = if (Utils.isTelevision(this)) View.VISIBLE else View.GONE
        findViewById<View>(R.id.player_pause).setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                renderState = RenderState.PAUSED
            }
        }
        findViewById<View>(R.id.player_resume).setOnClickListener {
            if (!exoPlayer.isPlaying) {
                exoPlayer.play()
                renderState = RenderState.PLAYING
            }
        }

        findViewById<PlayerView>(R.id.player_view).player = exoPlayer

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        renderState = RenderState.STOPPED
                    }

                    Player.STATE_READY -> {
                        progressBar.visibility = View.GONE
                    }

                    Player.STATE_BUFFERING -> {
                        progressBar.visibility = View.VISIBLE
                    }

                    else -> {
                        // Ignore
                    }
                }
            }

            override fun onRenderedFirstFrame() {
                renderState = RenderState.PLAYING
                progressBar.visibility = View.INVISIBLE
            }

            override fun onPlayerError(error: PlaybackException) {
                renderState = RenderState.ERROR
                progressBar.visibility = View.INVISIBLE
                errorMsg.visibility = View.VISIBLE
                errorMsg.text = "播放错误: $error"
            }
        })
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        openMedia()
    }

    private fun openMedia() {
        castAction?.currentURI?.run {
            progressBar.visibility = View.VISIBLE
            errorMsg.visibility = View.INVISIBLE
            exoPlayer.addMediaItem(MediaItem.fromUri(this))
        }
        castAction?.nextURI?.run {
            exoPlayer.addMediaItem(MediaItem.fromUri(this))
        }
        castAction?.stop?.run {
            finish()
        }

        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun onDestroy() {
        renderState = RenderState.STOPPED
        exoPlayer.release()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val handled = super.onKeyDown(keyCode, event)
        if (rendererService != null) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                val volume = (application.getSystemService(AUDIO_SERVICE) as AudioManager).getStreamVolume(AudioManager.STREAM_MUSIC)
                rendererService?.notifyRenderControlLastChange(volume)
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    renderState = RenderState.PAUSED
                } else {
                    exoPlayer.play()
                    renderState = RenderState.PLAYING
                }
            }
        }
        return handled
    }

    private inner class ExoPlayerRenderControl(private val player: Player) : RenderControl {
        override val currentPosition: Long
            get() = player.currentPosition
        override val duration: Long
            get() = player.duration

        override fun play(speed: Double?) {
            if (player.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH)) {
                speed?.also {
                    log.i("change player speed to ${it.toFloat()}")
                    player.setPlaybackSpeed(it.toFloat())
                }
            } else {
                log.w("player not support play speed...")
            }
            player.play()
            renderState = RenderState.PLAYING
        }

        override fun pause() {
            player.pause()
            renderState = RenderState.PAUSED
        }

        override fun seek(millSeconds: Long) = player.seekTo(millSeconds)
        override fun stop() {
            player.stop()
            renderState = RenderState.STOPPED
            // close player
            finish()
        }

        override fun getState(): RenderState = renderState
    }
}

