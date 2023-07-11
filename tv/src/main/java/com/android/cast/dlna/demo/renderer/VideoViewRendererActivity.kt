package com.android.cast.dlna.demo.renderer

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import com.android.cast.dlna.dmr.DLNARendererService
import com.android.cast.dlna.dmr.RenderControl.VideoViewRenderControl
import com.android.cast.dlna.dmr.service.keyCurrentURI
import org.fourthline.cling.support.model.TransportState.PAUSED_PLAYBACK
import org.fourthline.cling.support.model.TransportState.PLAYING
import org.fourthline.cling.support.model.TransportState.STOPPED

class VideoViewRendererActivity : BaseRendererActivity() {

    private val videoView: VideoView by lazy {
        findViewById<VideoView?>(R.id.video_view).apply {
            setMediaController(MediaController(this@VideoViewRendererActivity))
        }
    }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.video_progress) }
    private val errorMsg: TextView by lazy { findViewById(R.id.video_error) }


    override fun onServiceConnected(service: DLNARendererService) {
        super.onServiceConnected(service)
        rendererService?.setRenderControl(VideoViewRenderControl(videoView))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videoview_renderer)
        openMedia(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openMedia(intent)
    }

    private fun openMedia(intent: Intent) {
        val url = intent.extras?.getString(keyCurrentURI)
        if (url != null) {
            progressBar.visibility = View.VISIBLE
            errorMsg.visibility = View.INVISIBLE
            videoView.setVideoURI(Uri.parse(url))
            videoView.setOnPreparedListener { mp ->
                mp.start()
                progressBar.visibility = View.INVISIBLE
                notifyTransportStateChanged(PLAYING)
            }
            videoView.setOnErrorListener { _, what, extra ->
                progressBar.visibility = View.INVISIBLE
                errorMsg.visibility = View.VISIBLE
                errorMsg.text = "播放错误: $what, $extra"
                notifyTransportStateChanged(STOPPED)
                true
            }
            videoView.setOnCompletionListener {
                progressBar.visibility = View.INVISIBLE
                notifyTransportStateChanged(STOPPED)
                finish()
            }
        } else {
            errorMsg.visibility = View.VISIBLE
            errorMsg.text = "没有找到有效的视频地址，请检查..."
        }
    }

    override fun onDestroy() {
        videoView.stopPlayback()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val handled = super.onKeyDown(keyCode, event)
        if (rendererService != null) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                val volume = (application.getSystemService(AUDIO_SERVICE) as AudioManager).getStreamVolume(AudioManager.STREAM_MUSIC)
                notifyRenderVolumeChanged(volume)
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (videoView.isPlaying) {
                    videoView.pause()
                    notifyTransportStateChanged(PAUSED_PLAYBACK)
                } else {
                    videoView.resume()
                    notifyTransportStateChanged(PLAYING)
                }
            }
        }
        return handled
    }
}