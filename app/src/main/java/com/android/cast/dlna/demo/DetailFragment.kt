package com.android.cast.dlna.demo

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.cast.dlna.demo.fragment.ContentFragment
import com.android.cast.dlna.demo.fragment.DeviceInfoFragment
import com.android.cast.dlna.demo.fragment.DeviceServiceActionFragment
import com.android.cast.dlna.demo.fragment.VideoViewFragment
import com.android.cast.dlna.dmc.DLNACastManager
import org.fourthline.cling.model.meta.Device

interface DetailContainer {
    fun getDevice(): Device<*, *, *>
}

interface OnKeyEventHandler {
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = false
}

class DetailFragment : Fragment(), DetailContainer, OnKeyEventHandler {
    companion object {
        fun create(device: Device<*, *, *>): Fragment = DetailFragment().apply {
            this.device = device
        }
    }

    private lateinit var device: Device<*, *, *>
    override fun getDevice(): Device<*, *, *> = device

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (this::device.isInitialized) {
            if (device.type == DLNACastManager.DEVICE_TYPE_MEDIA_RENDERER) {
                replace(R.id.detail_video_container, VideoViewFragment())
                replace(R.id.detail_service_container, DeviceServiceActionFragment())
                replace(R.id.detail_info_container, DeviceInfoFragment())
            } else if (device.type == DLNACastManager.DEVICE_TYPE_MEDIA_SERVER) {
                replace(R.id.detail_content_container, ContentFragment())
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        childFragmentManager.fragments.forEach {
            (it as? OnKeyEventHandler)?.onKeyDown(keyCode, event)
        }
        return false
    }
}
