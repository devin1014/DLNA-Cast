package com.android.cast.dlna.demo.fragment

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
import com.android.cast.dlna.demo.DetailContainer
import com.android.cast.dlna.demo.R
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
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.model.PositionInfo
import java.util.Formatter
import java.util.Locale
import java.util.UUID

class VideoViewFragment : Fragment(), CastCallback {

    private val device: Device<*, *, *> by lazy { (requireParentFragment() as DetailContainer).getDevice() }
    private val castControlContainer: View by lazy { requireView().findViewById(R.id.video_cast_control) }
    private val positionInfo: TextView? by lazy { view?.findViewById(R.id.video_cast_position) }
    private val positionSeekBar: SeekBar? by lazy { view?.findViewById(R.id.video_cast_seekbar) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_view, container, false)
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
            }
        }
    }

    private fun initComponent(view: View) {
        view.findViewById<View>(R.id.video_cast).setOnClickListener { CastFragment.show(childFragmentManager) }
        view.findViewById<View>(R.id.video_cast_pause).setOnClickListener { DLNACastManager.pause() }
        view.findViewById<View>(R.id.video_cast_stop).setOnClickListener { DLNACastManager.stop() }
        view.findViewById<View>(R.id.video_cast_mute).setOnClickListener { DLNACastManager.setMute(true) }
        positionSeekBar?.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    override fun onCastUrl(url: String) {
        DLNACastManager.cast(device, CastObject.newInstance(url, UUID.randomUUID().toString(), "Test Sample"))
    }

    private var durationMillSeconds: Long = 0

    private val positionHandler = CircleMessageHandler(1000) {
        DLNACastManager.getPositionInfo(device, object : GetInfoListener<PositionInfo> {
            override fun onGetInfoResult(t: PositionInfo?, errMsg: String?) {
                if (t != null) {
                    positionInfo?.text = String.format("%s:%s", getStringTime(t.trackElapsedSeconds), getStringTime(t.trackDurationSeconds))
                    positionSeekBar?.progress = t.elapsedPercent
                } else {
                    positionInfo?.text = "--:--"
                }
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
                val position = (seekBar.progress * durationMillSeconds.toFloat() / seekBar.max).toLong()
                DLNACastManager.seekTo(position)
            }
        }
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