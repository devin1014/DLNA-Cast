package com.android.cast.dlna.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.cast.dlna.demo.CAST_VIDEO_M3U8
import com.android.cast.dlna.demo.CAST_VIDEO_MP4
import com.android.cast.dlna.demo.CAST_VIDEO_MP4_2
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.R.layout

interface CastCallback {
    fun onCastUrl(url: String)
}

class CastFragment : DialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager) {
            CastFragment().show(fragmentManager, "CastFragment")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, theme)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_cast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = parentFragment as? CastCallback ?: activity as? CastCallback
        view.findViewById<View>(R.id.cast_url_ok).setOnClickListener {
            val group = view.findViewById<RadioGroup>(R.id.cast_url_group)
            when (group.checkedRadioButtonId) {
                R.id.cast_video_m3u8 -> callback?.onCastUrl(CAST_VIDEO_M3U8)
                R.id.cast_video_mp4 -> callback?.onCastUrl(CAST_VIDEO_MP4)
                R.id.cast_image_jpg -> callback?.onCastUrl(CAST_VIDEO_MP4_2)
                else -> {}
            }
            dismiss()
        }
    }
}