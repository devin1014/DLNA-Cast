package com.android.cast.dlna.demo

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.demo.CastFragment.Callback
import com.android.cast.dlna.demo.R.layout
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.ICastInterface.*
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportState
import java.util.*

class ControlFragment : Fragment(), IDisplayDevice, Callback {

    private val positionInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_position_info) }
    private val positionSeekBar: SeekBar? by lazy { view?.findViewById(R.id.ctrl_seek_position) }
    private val volumeInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_volume_info) }
    private val volumeSeekBar: SeekBar? by lazy { view?.findViewById(R.id.ctrl_seek_volume) }
    private val statusInfo: TextView? by lazy { view?.findViewById(R.id.ctrl_status_info) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponent(view)

        DLNACastManager.getInstance().registerActionCallbacks(
            object : CastEventListener {
                override fun onSuccess(result: String?) {
                    Toast.makeText(activity, "Cast: $result", Toast.LENGTH_LONG).show()
                    positionHandler.start()
                    mVolumeMsgHandler.start()
                }

                override fun onFailed(errMsg: String) {
                    Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show()
                }
            },
            object : PlayEventListener {
                override fun onSuccess(result: Void?) {
                    Toast.makeText(activity, "Play", Toast.LENGTH_LONG).show()
                }

                override fun onFailed(errMsg: String) {
                    Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show()
                }
            },
            object : PauseEventListener {
                override fun onSuccess(result: Void?) {
                    Toast.makeText(activity, "Pause", Toast.LENGTH_LONG).show()
                }

                override fun onFailed(errMsg: String) {
                    Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show()
                }
            },
            object : StopEventListener {
                override fun onSuccess(result: Void?) {
                    Toast.makeText(activity, "Stop", Toast.LENGTH_LONG).show()
                    positionHandler.stop()
                    mVolumeMsgHandler.stop()
                }

                override fun onFailed(errMsg: String) {
                    Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show()
                }
            },
            object : SeekToEventListener {
                override fun onSuccess(result: Long) {
                    Toast.makeText(activity, "SeekTo: " + Utils.getStringTime(result), Toast.LENGTH_LONG).show()
                }

                override fun onFailed(errMsg: String) {
                    Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show()
                }
            }
        )
        DLNACastManager.getInstance().registerSubscriptionListener { event: TransportState -> statusInfo?.text = event.value }
    }

    private fun initComponent(view: View) {
        view.findViewById<View>(R.id.btn_cast).setOnClickListener { CastFragment(this).show(childFragmentManager) }
        view.findViewById<View>(R.id.btn_cast_pause).setOnClickListener { DLNACastManager.getInstance().pause() }
        view.findViewById<View>(R.id.btn_cast_resume).setOnClickListener { DLNACastManager.getInstance().play() }
        view.findViewById<View>(R.id.btn_cast_stop).setOnClickListener { DLNACastManager.getInstance().stop() }
        view.findViewById<View>(R.id.btn_cast_mute).setOnClickListener { DLNACastManager.getInstance().setMute(true) }
        positionSeekBar?.setOnSeekBarChangeListener(seekBarChangeListener)
        volumeSeekBar?.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    override fun onDestroyView() {
        DLNACastManager.getInstance().unregisterActionCallbacks()
        super.onDestroyView()
    }

    private val seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            when (seekBar.id) {
                R.id.ctrl_seek_volume -> {
                    DLNACastManager.getInstance().setVolume((seekBar.progress * 100f / seekBar.max).toInt())
                }
                R.id.ctrl_seek_position -> {
                    if (durationMillSeconds > 0) {
                        val position = (seekBar.progress * 1f / seekBar.max * durationMillSeconds).toInt()
                        DLNACastManager.getInstance().seekTo(position.toLong())
                    }
                }
            }
        }
    }

    private var device: Device<*, *, *>? = null

    override fun setCastDevice(device: Device<*, *, *>?) {
        this.device = device
        if (device == null) {
            positionInfo?.text = ""
            positionSeekBar?.progress = 0
            volumeInfo?.text = ""
            volumeSeekBar?.progress = 0
            positionHandler.stop()
            mVolumeMsgHandler.stop()
        }
        // reconnect device, should recover status?
    }

    override fun onCastUrl(url: String?) {
        if (device != null) {
            DLNACastManager.getInstance().cast(device, CastObject.newInstance(url!!, UUID.randomUUID().toString(), "Test Sample"))
        }
    }

    private var durationMillSeconds: Long = 0

    private val positionHandler = CircleMessageHandler(1000, Runnable {
        if (device == null) return@Runnable
        // update position text and progress
        DLNACastManager.getInstance().getPositionInfo(device) { positionInfo: PositionInfo?, errMsg: String? ->
            if (positionInfo != null) {
                this.positionInfo?.text = String.format("%s/%s", positionInfo.relTime, positionInfo.trackDuration)
                if (positionInfo.trackDurationSeconds != 0L) {
                    durationMillSeconds = positionInfo.trackDurationSeconds * 1000
                    positionSeekBar?.progress = (positionInfo.trackElapsedSeconds * 100 / positionInfo.trackDurationSeconds).toInt()
                } else {
                    positionSeekBar?.progress = 0
                }
            } else {
                this.positionInfo?.text = errMsg
            }
        }
    })

    private val mVolumeMsgHandler = CircleMessageHandler(3000, Runnable {
        if (device == null) return@Runnable
        // update volume
        DLNACastManager.getInstance().getVolumeInfo(device) { integer: Int?, errMsg: String? ->
            if (integer != null && activity != null) {
                val audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                volumeSeekBar?.progress = integer
                volumeInfo?.text = String.format("%s/%s", (integer / 100f * maxVolume).toInt(), maxVolume)
            } else {
                volumeInfo?.text = errMsg
            }
        }
    })

    // ------------------------------------------------------------
    // ---- Handler
    // ------------------------------------------------------------
    private class CircleMessageHandler(
        private val duration: Long,
        private val runnable: Runnable
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
}