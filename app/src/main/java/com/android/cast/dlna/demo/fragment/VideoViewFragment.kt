package com.android.cast.dlna.demo.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.demo.DetailContainer
import com.android.cast.dlna.demo.MainActivity
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.VideoUrl
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.DeviceControl
import com.android.cast.dlna.dmc.control.OnDeviceControlListener
import com.android.cast.dlna.dmc.control.ServiceActionCallback
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.TransportState.NO_MEDIA_PRESENT
import java.util.Formatter
import java.util.Locale

class VideoViewFragment : Fragment() {

    private val logger = Logger.create("VideoViewFragment")

    @Suppress("DEPRECATION")
    private val colorAccent: Int by lazy { resources.getColor(R.color.colorAccent) }
    private val device: Device<*, *, *> by lazy { (requireParentFragment() as DetailContainer).getDevice() }
    private val positionInfo: TextView by lazy { requireView().findViewById(R.id.video_cast_position) }
    private val positionSeekBar: SeekBar by lazy { requireView().findViewById(R.id.video_cast_seekbar) }
    private val pauseButton: ImageView by lazy { requireView().findViewById(R.id.video_cast_pause) }
    private val volumeMuteButton: ImageView by lazy { requireView().findViewById(R.id.video_cast_mute) }
    private lateinit var deviceControl: DeviceControl
    private var currentState: TransportState = NO_MEDIA_PRESENT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponent(view)
        deviceControl = DLNACastManager.connectDevice(device, object : OnDeviceControlListener {
            override fun onConnected(device: Device<*, *, *>) {
                Toast.makeText(requireContext(), "成功连接: ${device.details.friendlyName}", Toast.LENGTH_SHORT).show()
            }

            override fun onDisconnected(device: Device<*, *, *>) {
                (requireActivity() as MainActivity).onBackPressed()
                Toast.makeText(requireContext(), "无法连接: ${device.details.friendlyName}", Toast.LENGTH_SHORT).show()
            }

            override fun onAvTransportStateChanged(state: TransportState) {
                currentState = state
                pauseButton.setImageResource(if (state == TransportState.PLAYING) R.drawable.cast_pause else R.drawable.cast_play)
            }
        })
        deviceControl.getMute(object : ServiceActionCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                volumeMuteButton.isSelected = result
                volumeMuteButton.setColorFilter(if (result) colorAccent else 0xFFFFFF)
            }

            override fun onFailure(msg: String) {
            }
        })
    }

    private fun initComponent(view: View) {
        view.findViewById<View>(R.id.video_cast_stop).setOnClickListener {
            deviceControl.stop()
        }
        view.findViewById<View>(R.id.video_cast).setOnClickListener {
            CastUrlDialogFragment.show(childFragmentManager, object : OnUrlSelectListener {
                override fun onUrlSelected(video: VideoUrl) {
                    durationMillSeconds = 0L
                    deviceControl.setAVTransportURI(video.url, video.title, object : ServiceActionCallback<Unit> {
                        override fun onSuccess(result: Unit) {
                            positionHandler.start()
                        }

                        override fun onFailure(msg: String) {
                            activity?.also { context ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            })
        }
        view.findViewById<View>(R.id.local_cast).setOnClickListener {
            startActivityForResult(Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "video/*;audio/*"
            }, 987)
        }
        view.findViewById<View>(R.id.video_cast_add).setOnClickListener {
            CastUrlDialogFragment.show(childFragmentManager, object : OnUrlSelectListener {
                override fun onUrlSelected(video: VideoUrl) {
                    deviceControl.setNextAVTransportURI(video.url, video.title)
                }
            })
        }
        pauseButton.setOnClickListener {
            if (currentState == TransportState.PLAYING) {
                deviceControl.pause()
            } else {
                deviceControl.play()
            }
        }
        volumeMuteButton.setOnClickListener {
            val mute = !it.isSelected
            deviceControl.setMute(mute)
            it.isSelected = mute
            volumeMuteButton.setColorFilter(if (mute) colorAccent else 0xFFFFFF)
        }
        positionSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    private var durationMillSeconds: Long = 0

    private val positionHandler = CircleMessageHandler(1000) {
        deviceControl.getPositionInfo(object : ServiceActionCallback<PositionInfo> {
            override fun onSuccess(result: PositionInfo) {
                if (durationMillSeconds == 0L) {
                    durationMillSeconds = result.trackDurationSeconds * 1000
                }
                positionInfo.text = String.format("%s/%s", getStringTime(result.trackElapsedSeconds * 1000), getStringTime(result.trackDurationSeconds * 1000))
                positionSeekBar.progress = result.elapsedPercent
            }

            override fun onFailure(msg: String) {
                positionInfo.text = "--:--/--:--"
            }
        })
    }

    private fun getStringTime(timeMs: Long): String {
        val formatBuilder = StringBuilder()
        val formatter = Formatter(formatBuilder, Locale.US)
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours == 0L) formatter.format("%02d:%02d", minutes, seconds).toString()
        else formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString()
    }

    private val seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            if (durationMillSeconds > 0) {
                deviceControl.seek(seekBar.progress * durationMillSeconds / seekBar.max)
            }
        }
    }

    // content://com.android.providers.media.documents/document/video:6217
    // content://com.android.providers.media.documents/document/video:6224
    // content://com.android.providers.downloads.documents/document/raw:/storage/emulated/0/Download/big_buck_bunny.mp4
    @Deprecated("Deprecated in Java", ReplaceWith("super.onActivityResult(requestCode, resultCode, data)", "androidx.fragment.app.Fragment"))
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 987 && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val file = Utils.parseUri2File(requireContext(), uri)
                if (file == null) {
                    logger.w("selected: ${Uri.decode(uri.toString())}")
                    Toast.makeText(context, "找不到文件路径...", Toast.LENGTH_SHORT).show()
                } else {
                    logger.i("selected: $file")
                    DLNACastManager.startLocalHttpServer()
                    durationMillSeconds = 0L
                    deviceControl.setAVTransportURI("${DLNACastManager.localServer?.baseUrl}${file.path}", file.nameWithoutExtension, object : ServiceActionCallback<Unit> {
                        override fun onSuccess(result: Unit) {
                            positionHandler.start()
                        }

                        override fun onFailure(msg: String) {
                            activity?.also { context ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            }
        }
    }

    override fun onDestroyView() {
        DLNACastManager.stopLocalHttpServer()
        DLNACastManager.disconnectDevice(device)
        positionHandler.stop()
        super.onDestroyView()
    }
}

// ------------------------------------------------------------
// ---- Handler
// ------------------------------------------------------------
private class CircleMessageHandler(
    private val duration: Long,
    private val runnable: Runnable,
) : Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        runnable.run()
        sendEmptyMessageDelayed(MSG, duration)
    }

    fun start(delay: Long = 0L) {
        stop()
        sendEmptyMessageDelayed(MSG, delay)
    }

    fun stop() {
        removeMessages(MSG)
    }

    companion object {
        private const val MSG = 101
    }
}