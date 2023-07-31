package com.android.cast.dlna.dmc

import android.content.Intent
import com.android.cast.dlna.core.Logger
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.android.FixedAndroidLogHandler
import org.fourthline.cling.model.types.ServiceType
import org.seamless.util.logging.LoggingUtil

class DLNACastService : AndroidUpnpServiceImpl() {
    private val logger = Logger.create("CastService")
    override fun onCreate() {
        logger.i(String.format("[%s] onCreate", javaClass.simpleName))
        LoggingUtil.resetRootHandler(FixedAndroidLogHandler())
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        logger.i(String.format("[%s] onStartCommand: %s, %s, %s", javaClass.simpleName, intent, flags, startId))
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        logger.w(String.format("[%s] onDestroy", javaClass.simpleName))
        super.onDestroy()
    }

    override fun createConfiguration(): UpnpServiceConfiguration = object : AndroidUpnpServiceConfiguration() {
//        arrayOf(
//        DLNACastManager.SERVICE_RENDERING_CONTROL,
//        DLNACastManager.SERVICE_AV_TRANSPORT,
//        DLNACastManager.SERVICE_CONNECTION_MANAGER,
//        DLNACastManager.SERVICE_CONTENT_DIRECTORY
//        )
        override fun getExclusiveServiceTypes(): Array<ServiceType> = emptyArray()
    }
}