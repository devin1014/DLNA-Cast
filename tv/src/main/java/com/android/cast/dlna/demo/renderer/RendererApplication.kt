package com.android.cast.dlna.demo.renderer

import android.app.Application
import com.android.cast.dlna.core.Logger
import java.util.logging.Level

class RendererApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // LoggingUtil.resetRootHandler(FixedAndroidLogHandler())
        java.util.logging.Logger.getLogger("org.fourthline.cling").level = Level.ALL
        Logger.create("RendererApplication").i("Application onCreate.")
    }
}