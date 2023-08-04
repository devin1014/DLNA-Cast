package com.android.cast.dlna.demo.server

import android.app.Application
import java.util.logging.Level

class ServerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // LoggingUtil.resetRootHandler(FixedAndroidLogHandler())
        java.util.logging.Logger.getLogger("org.fourthline.cling").level = Level.CONFIG
        com.android.cast.dlna.core.Logger.printThread = true
        com.android.cast.dlna.core.Logger.enabled = true
        com.android.cast.dlna.core.Logger.level = com.android.cast.dlna.core.Level.D
        com.android.cast.dlna.core.Logger.create("ServerApplication").i("Application onCreate.")
    }
}