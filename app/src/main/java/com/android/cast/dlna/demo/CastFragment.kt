package com.android.cast.dlna.demo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class CastFragment(private val callback: Callback?) : DialogFragment() {

    interface Callback {
        fun onCastUrl(url: String?)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, theme)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.cast_url_ok).setOnClickListener {
            val group = view.findViewById<RadioGroup>(R.id.cast_url_group)
            when (group.checkedRadioButtonId) {
                R.id.cast_video_m3u8 -> callback?.onCastUrl(CAST_VIDEO_M3U8)
                R.id.cast_video_mp4 -> callback?.onCastUrl(CAST_VIDEO_MP4)
                R.id.cast_image_jpg -> callback?.onCastUrl(CAST_IMAGE_JPG)
                else -> {}
            }
            dismiss()
        }
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "ControlFragment")
    }
}