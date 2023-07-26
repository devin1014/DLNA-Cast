package com.android.cast.dlna.demo.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.R.layout
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dms.MediaServer
import org.fourthline.cling.model.meta.Device
import java.util.*

class LocalControlFragment : Fragment() {

    private val mPickupContent: TextView? by lazy { view?.findViewById(R.id.local_ctrl_pick_content_text) }
    private lateinit var mMediaServer: MediaServer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_local_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMediaServer = MediaServer(requireContext()).apply { start() }
        DLNACastManager.addMediaServer(mMediaServer.device)
        initComponent(view)
    }

    private fun initComponent(view: View) {
        val selectPath = PreferenceManager.getDefaultSharedPreferences(activity).getString("selectPath", "")
        mPickupContent?.text = selectPath
        mCastPathUrl = selectPath
        view.findViewById<View>(R.id.local_ctrl_pick_content).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/*;audio/*;image/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT)
        }
        view.findViewById<View>(R.id.local_ctrl_cast).setOnClickListener {
            if (mDevice != null) {
                //DLNACastManager.cast(mDevice!!, newInstance(mCastPathUrl!!, UUID.randomUUID().toString(), "Test Sample"))
            }
        }
    }

    private var mCastPathUrl: String? = ""

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val path = Utils.parseUri2Path(requireContext(), uri)
            mCastPathUrl = mMediaServer.baseUrl + path
            mPickupContent!!.text = mCastPathUrl
            PreferenceManager.getDefaultSharedPreferences(activity)
                .edit().putString("selectPath", mCastPathUrl)
                .apply()
        }
    }

    private var mDevice: Device<*, *, *>? = null
    fun setCastDevice(device: Device<*, *, *>?) {
        mDevice = device
    }

    override fun onDestroyView() {
        DLNACastManager.removeMediaServer(mMediaServer.device)
        mMediaServer.stop()
        super.onDestroyView()
    }

    companion object {
        private const val REQUEST_CODE_SELECT = 222
    }
}