package com.android.cast.dlna.demo

import android.app.Application
import com.android.cast.dlna.dmc.DLNACastManager
import java.util.logging.Level

class CastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        java.util.logging.Logger.getLogger("org.fourthline.cling").level = Level.CONFIG
        com.android.cast.dlna.core.Logger.create("CastApplication").i("Application onCreate.")
        DLNACastManager.enableLog(true)
    }
}