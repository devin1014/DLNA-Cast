package com.android.cast.dlna.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.cast.dlna.demo.fragment.InfoFragment
import com.android.cast.dlna.dmc.DLNACastManager
import org.fourthline.cling.model.meta.Device

abstract class DetailFragment : Fragment() {
    companion object {
        fun create(device: Device<*, *, *>): Fragment {
            val fragment: DetailFragment = if (device.type == DLNACastManager.DEVICE_TYPE_MEDIA_RENDERER) {
                VideoViewDetailFragment()
            } else {
                EmptyDetailFragment()
            }
            fragment.device = device
            return fragment
        }
    }

    protected var device: Device<*, *, *>? = null
}

class EmptyDetailFragment : DetailFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.info_fragment) as? InfoFragment)?.device = device
    }
}

class VideoViewDetailFragment : DetailFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.info_fragment) as? InfoFragment)?.device = device
    }
}
