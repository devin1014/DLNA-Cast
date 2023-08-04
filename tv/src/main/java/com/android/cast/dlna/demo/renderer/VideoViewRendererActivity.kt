package com.android.cast.dlna.demo.renderer

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import com.android.cast.dlna.dmr.BaseRendererActivity
import com.android.cast.dlna.dmr.RenderControl
import com.android.cast.dlna.dmr.RenderState

class VideoViewRendererActivity : BaseRendererActivity() {

    private val videoView: VideoView by lazy { findViewById(R.id.video_view) }
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
        rendererService?.bindRealPlayer(VideoViewRenderControl(videoView))
        openMedia()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videoview_renderer)
        initComponent()
        rendererService?.run {
            openMedia()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initComponent() {
        // 方便在平板上调试，模拟遥控器
        findViewById<View>(R.id.player_action_bar).visibility = View.VISIBLE
        findViewById<View>(R.id.player_pause).setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                renderState = RenderState.PAUSED
            }
        }
        findViewById<View>(R.id.player_resume).setOnClickListener {
            if (!videoView.isPlaying) {
                videoView.start()
                renderState = RenderState.PLAYING
            }
        }
        videoView.setOnPreparedListener { mp ->
            mp.start()
            renderState = RenderState.PLAYING
            progressBar.visibility = View.INVISIBLE
        }
        videoView.setOnErrorListener { _, what, extra ->
            renderState = RenderState.ERROR
            if (nextURI.isNullOrBlank()) {
                progressBar.visibility = View.INVISIBLE
                errorMsg.visibility = View.VISIBLE
                errorMsg.text = "播放错误: $what, $extra"
            } else {
                videoView.setVideoURI(Uri.parse(nextURI))
                nextURI = null
            }
            true
        }
        videoView.setOnCompletionListener {
            renderState = RenderState.STOPPED
            if (nextURI.isNullOrBlank()) {
                finish()
            } else {
                videoView.setVideoURI(Uri.parse(nextURI))
                nextURI = null
            }
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        openMedia()
    }

    private var nextURI: String? = null

    @SuppressLint("SetTextI18n")
    private fun openMedia() {
        castAction?.currentURI?.run {
            progressBar.visibility = View.VISIBLE
            errorMsg.visibility = View.INVISIBLE
            videoView.setVideoURI(Uri.parse(this))
        }
        castAction?.nextURI?.run {
            nextURI = this
        }
        castAction?.stop?.run {
            finish()
        }
    }

    override fun onDestroy() {
        renderState = RenderState.STOPPED
        videoView.stopPlayback()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val handled = super.onKeyDown(keyCode, event)
        if (rendererService != null) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                val volume = (application.getSystemService(AUDIO_SERVICE) as AudioManager).getStreamVolume(AudioManager.STREAM_MUSIC)
                rendererService?.notifyRenderControlLastChange(volume)
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (videoView.isPlaying) {
                    videoView.pause()
                    renderState = RenderState.PAUSED
                } else {
                    videoView.resume()
                    renderState = RenderState.PLAYING
                }
            }
        }
        return handled
    }

    private inner class VideoViewRenderControl(private val videoView: VideoView) : RenderControl {
        override val currentPosition: Long
            get() = videoView.currentPosition.toLong()
        override val duration: Long
            get() = videoView.duration.toLong()

        override fun play(speed: Double?) { // video view 不支持倍速播放
            videoView.start()
            renderState = RenderState.PLAYING
        }

        override fun pause() {
            videoView.pause()
            renderState = RenderState.PAUSED
        }

        override fun seek(millSeconds: Long) = videoView.seekTo(millSeconds.toInt())
        override fun stop() {
            videoView.stopPlayback()
            renderState = RenderState.STOPPED
            // close player
            finish()
        }

        override fun getState(): RenderState = renderState
    }
}

