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
import org.fourthline.cling.support.model.TransportState.PAUSED_PLAYBACK
import org.fourthline.cling.support.model.TransportState.PLAYING
import org.fourthline.cling.support.model.TransportState.STOPPED

class VideoViewRendererActivity : BaseRendererActivity() {

    private val videoView: VideoView by lazy { findViewById(R.id.video_view) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.video_progress) }
    private val errorMsg: TextView by lazy { findViewById(R.id.video_error) }
    private var renderState: RenderState = RenderState.IDLE

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

    private fun initComponent() {
        // 方便在平板上调试，模拟遥控器
        findViewById<View>(R.id.player_action_bar).visibility = View.VISIBLE
        findViewById<View>(R.id.player_pause).setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                renderState = RenderState.PAUSED
                rendererService?.changeTransportState(PAUSED_PLAYBACK)
            }
        }
        findViewById<View>(R.id.player_resume).setOnClickListener {
            if (!videoView.isPlaying) {
                videoView.resume()
                renderState = RenderState.PLAYING
                rendererService?.changeTransportState(PLAYING)
            }
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(intent)
        openMedia()
    }

    @SuppressLint("SetTextI18n")
    private fun openMedia() {
        // demo only handle currentURI...
        val url = castAction?.currentURI
        if (url != null) {
            progressBar.visibility = View.VISIBLE
            errorMsg.visibility = View.INVISIBLE
            videoView.setVideoURI(Uri.parse(url))
            videoView.setOnPreparedListener { mp ->
                mp.start()
                renderState = RenderState.PLAYING
                progressBar.visibility = View.INVISIBLE
                rendererService?.changeTransportState(PLAYING)
            }
            videoView.setOnErrorListener { _, what, extra ->
                renderState = RenderState.ERROR
                progressBar.visibility = View.INVISIBLE
                errorMsg.visibility = View.VISIBLE
                errorMsg.text = "播放错误: $what, $extra"
                rendererService?.changeTransportState(STOPPED)
                true
            }
            videoView.setOnCompletionListener {
                renderState = RenderState.STOPPED
                progressBar.visibility = View.INVISIBLE
                rendererService?.changeTransportState(STOPPED)
                finish()
            }
        } else {
            errorMsg.visibility = View.VISIBLE
            errorMsg.text = "没有找到有效的视频地址，请检查..."
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
                rendererService?.changeVolume(volume)
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (videoView.isPlaying) {
                    videoView.pause()
                    renderState = RenderState.PAUSED
                    rendererService?.changeTransportState(PAUSED_PLAYBACK)
                } else {
                    videoView.resume()
                    renderState = RenderState.PLAYING
                    rendererService?.changeTransportState(PLAYING)
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

        override fun play() = videoView.start()
        override fun pause() = videoView.pause()
        override fun seek(millSeconds: Long) = videoView.seekTo(millSeconds.toInt())
        override fun stop() = videoView.stopPlayback()
        override fun getState(): RenderState = renderState
    }
}

