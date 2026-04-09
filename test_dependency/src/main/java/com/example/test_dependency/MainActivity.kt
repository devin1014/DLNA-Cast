package com.example.test_dependency

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.OnDeviceRegistryListener
import org.fourthline.cling.model.meta.Device

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DLNACastManager.registerDeviceListener(object : OnDeviceRegistryListener {
            override fun onDeviceAdded(device: Device<*, *, *>) {
                Log.i("testLog", "onDeviceAdded: $device")
            }

            override fun onDeviceRemoved(device: Device<*, *, *>) {
                Log.w("testLog", "onDeviceRemoved: $device")
            }
        })
    }

    override fun onStart() {
        super.onStart()
        DLNACastManager.bindCastService(this)
    }

    override fun onStop() {
        DLNACastManager.unbindCastService(this)
        super.onStop()
    }
}
