package com.android.cast.dlna.demo.server

import android.app.Application
import com.android.cast.dlna.dms.DLNAContentService

class ServerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DLNAContentService.startService(this)
    }
}