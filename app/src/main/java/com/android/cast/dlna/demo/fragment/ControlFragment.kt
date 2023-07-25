package com.android.cast.dlna.demo.fragment

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
import com.android.cast.dlna.demo.CastObject
import com.android.cast.dlna.demo.IDisplayDevice
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.R.layout
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.control.ActionResponse
import com.android.cast.dlna.dmc.control.CastEventListener
import com.android.cast.dlna.dmc.control.GetInfoListener
import com.android.cast.dlna.dmc.control.PauseEventListener
import com.android.cast.dlna.dmc.control.PlayEventListener
import com.android.cast.dlna.dmc.control.SeekToEventListener
import com.android.cast.dlna.dmc.control.StopEventListener
import com.android.cast.dlna.dmc.control.SubscriptionListener
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.model.PositionInfo
import java.util.UUID

class ControlFragment : Fragment(), IDisplayDevice, CastCallback {

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

        DLNACastManager.registerActionCallbacks(
            object : CastEventListener {
                override fun onResponse(response: ActionResponse<String>) {
                    if (response.exception != null) {
                        Toast.makeText(activity, response.exception, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "Cast: ${response.data}", Toast.LENGTH_LONG).show()
                        positionHandler.start()
                        mVolumeMsgHandler.start()
                    }
                }
            },
            object : PlayEventListener {
                override fun onResponse(response: ActionResponse<String>) {
                    if (response.exception != null) {
                        Toast.makeText(activity, response.exception, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "${response.data}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            object : PauseEventListener {
                override fun onResponse(response: ActionResponse<String>) {
                    if (response.exception != null) {
                        Toast.makeText(activity, response.exception, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "${response.data}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            object : StopEventListener {
                override fun onResponse(response: ActionResponse<String>) {
                    if (response.exception != null) {
                        Toast.makeText(activity, response.exception, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "${response.data}", Toast.LENGTH_LONG).show()
                    }
                    positionHandler.stop()
                    mVolumeMsgHandler.stop()
                }
            },
            object : SeekToEventListener {
                override fun onResponse(response: ActionResponse<Long>) {
                    if (response.exception != null) {
                        Toast.makeText(activity, response.exception, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "SeekTo: ${response.data}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
        DLNACastManager.subscriptionListener = object : SubscriptionListener {
            override fun onSubscriptionTransportStateChanged(event: EventedValue<*>) {
                //TODO:check
                if (event is TransportState) {
                    statusInfo?.text = event.value.value
                }
            }
        }
    }

    private fun initComponent(view: View) {
        view.findViewById<View>(R.id.btn_cast).setOnClickListener { CastFragment.show(childFragmentManager) }
        view.findViewById<View>(R.id.btn_cast_pause).setOnClickListener { DLNACastManager.pause() }
        view.findViewById<View>(R.id.btn_cast_resume).setOnClickListener { DLNACastManager.play() }
        view.findViewById<View>(R.id.btn_cast_stop).setOnClickListener { DLNACastManager.stop() }
        view.findViewById<View>(R.id.btn_cast_mute).setOnClickListener { DLNACastManager.setMute(true) }
        positionSeekBar?.setOnSeekBarChangeListener(seekBarChangeListener)
        volumeSeekBar?.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    override fun onDestroyView() {
        DLNACastManager.unregisterActionCallbacks()
        super.onDestroyView()
    }

    private val seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            when (seekBar.id) {
                R.id.ctrl_seek_volume -> {
                    DLNACastManager.setVolume((seekBar.progress * 100f / seekBar.max).toInt())
                }

                R.id.ctrl_seek_position -> {
                    if (durationMillSeconds > 0) {
                        val position = (seekBar.progress * 1f / seekBar.max * durationMillSeconds).toInt()
                        DLNACastManager.seekTo(position.toLong())
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

    override fun onCastUrl(url: String) {
        if (device != null) {
            DLNACastManager.cast(device!!, CastObject.newInstance(url, UUID.randomUUID().toString(), "Test Sample"))
        }
    }

    private var durationMillSeconds: Long = 0

    private val positionHandler = CircleMessageHandler(1000, Runnable {
        if (device == null) return@Runnable
        // update position text and progress
        DLNACastManager.getPositionInfo(device!!, object : GetInfoListener<PositionInfo> {
            override fun onGetInfoResult(t: PositionInfo?, errMsg: String?) {
                if (t != null) {
                    this@ControlFragment.positionInfo?.text = String.format("%s/%s", t.relTime, t.trackDuration)
                    if (t.trackDurationSeconds != 0L) {
                        durationMillSeconds = t.trackDurationSeconds * 1000
                        positionSeekBar?.progress = (t.trackElapsedSeconds * 100 / t.trackDurationSeconds).toInt()
                    } else {
                        positionSeekBar?.progress = 0
                    }
                } else {
                    this@ControlFragment.positionInfo?.text = errMsg
                }
            }
        })
    })

    private val mVolumeMsgHandler = CircleMessageHandler(3000, Runnable {
        if (device == null) return@Runnable
        // update volume
        DLNACastManager.getVolumeInfo(device!!, object : GetInfoListener<Int> {
            override fun onGetInfoResult(t: Int?, errMsg: String?) {
                if (t != null && activity != null) {
                    val audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    volumeSeekBar?.progress = t
                    volumeInfo?.text = String.format("%s/%s", (t / 100f * maxVolume).toInt(), maxVolume)
                } else {
                    volumeInfo?.text = errMsg
                }
            }

        })
    })

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
}