package com.android.cast.dlna.dmr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.dmr.R.id
import com.android.cast.dlna.dmr.R.layout
import com.android.cast.dlna.dmr.RenderControl.VideoViewRenderControl
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable
import org.fourthline.cling.support.model.Channel.Master
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.TransportState.*
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable.Volume

/**
 *
 */
class DLNARendererActivity : AppCompatActivity() {

    companion object {
        private const val KEY_EXTRA_CURRENT_URI = "Renderer.KeyExtra.CurrentUri"
        fun startActivity(context: Context, currentURI: String?) {
            val intent = Intent(context, DLNARendererActivity::class.java)
            intent.putExtra(KEY_EXTRA_CURRENT_URI, currentURI)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // start from service content,should add 'FLAG_ACTIVITY_NEW_TASK' flag.
            context.startActivity(intent)
        }
    }

    private val instanceId = UnsignedIntegerFourBytes(0)
    private val videoView: VideoView? by lazy { findViewById(id.video_view) }
    private val progressBar: ProgressBar? by lazy { findViewById(id.video_progress) }
    private var rendererService: DLNARendererService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            //TODO: can not access to RendererServiceBinder.
            //rendererService = (service as RendererServiceBinder).rendererService
            rendererService?.setRenderControl(VideoViewRenderControl(videoView!!))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            rendererService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_dlna_renderer)
        bindService(Intent(this, DLNARendererService::class.java), serviceConnection, BIND_AUTO_CREATE)
        openMedia(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openMedia(intent)
    }

    private fun openMedia(intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            progressBar?.visibility = View.VISIBLE
            val currentUri = bundle.getString(KEY_EXTRA_CURRENT_URI)
            videoView?.setVideoURI(Uri.parse(currentUri))
            videoView?.setOnPreparedListener { mp ->
                mp.start()
                progressBar?.visibility = View.INVISIBLE
                notifyTransportStateChanged(PLAYING)
            }
            videoView!!.setOnErrorListener { mp, what, extra ->
                progressBar?.visibility = View.INVISIBLE
                notifyTransportStateChanged(STOPPED)
                finish()
                true
            }
            videoView!!.setOnCompletionListener {
                progressBar?.visibility = View.INVISIBLE
                notifyTransportStateChanged(STOPPED)
                finish()
            }
        } else {
            Toast.makeText(this, "没有找到有效的视频地址，请检查...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        videoView?.stopPlayback()
        notifyTransportStateChanged(STOPPED)
        unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val handled = super.onKeyDown(keyCode, event)
        if (rendererService != null) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                val volume = (application.getSystemService(AUDIO_SERVICE) as AudioManager).getStreamVolume(AudioManager.STREAM_MUSIC)
                notifyRenderVolumeChanged(volume)
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (videoView?.isPlaying == true) {
                    videoView?.pause()
                    notifyTransportStateChanged(PAUSED_PLAYBACK)
                } else if (videoView != null) {
                    videoView?.resume()
                    notifyTransportStateChanged(PLAYING)
                }
            }
        }
        return handled
    }

    private fun notifyTransportStateChanged(transportState: TransportState) {
        rendererService?.avTransportLastChange?.setEventedValue(instanceId, AVTransportVariable.TransportState(transportState))
    }

    private fun notifyRenderVolumeChanged(volume: Int) {
        rendererService?.audioControlLastChange?.setEventedValue(instanceId, Volume(ChannelVolume(Master, volume)))
    }

}