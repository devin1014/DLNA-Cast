package com.android.cast.dlna.demo.server

import android.Manifest.permission
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.core.Utils
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionX.init(this)
            .permissions(permission.READ_EXTERNAL_STORAGE, permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION)
            .request { _: Boolean, _: List<String?>?, _: List<String?>? ->
                resetWifiInfo()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun resetWifiInfo() {
        (findViewById<View>(R.id.network_info) as TextView).text = "${Utils.getWiFiName(this)} - ${Utils.getWiFiIpAddress(this)}"
    }
}