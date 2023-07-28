package com.android.cast.dlna.demo.server

import android.Manifest.permission
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.core.http.LocalServer
import com.android.cast.dlna.dms.DLNAContentService
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {
    private val localServer: LocalServer by lazy { LocalServer(this, 8193) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionX.init(this)
            .permissions(permission.READ_EXTERNAL_STORAGE, permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION)
            .request { _: Boolean, _: List<String?>?, _: List<String?>? ->
            }
        localServer.startServer()
        DLNAContentService.startService(this)
    }

    override fun onDestroy() {
        localServer.stopServer()
        super.onDestroy()
    }
}