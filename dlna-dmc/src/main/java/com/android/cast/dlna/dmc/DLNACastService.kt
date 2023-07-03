package com.android.cast.dlna.dmc

import android.content.Intent
import com.orhanobut.logger.Logger
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.android.FixedAndroidLogHandler
import org.fourthline.cling.model.types.ServiceType
import org.seamless.util.logging.LoggingUtil

/**
 *
 */
class DLNACastService : AndroidUpnpServiceImpl() {
    override fun onCreate() {
        Logger.i(String.format("[%s] onCreate", javaClass.name))
        LoggingUtil.resetRootHandler(FixedAndroidLogHandler())
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Logger.i(String.format("[%s] onStartCommand: %s, %s, %s", javaClass.name, intent, flags, startId))
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Logger.w(String.format("[%s] onDestroy", javaClass.name))
        super.onDestroy()
    }

    override fun createConfiguration(): UpnpServiceConfiguration = DLNACastServiceConfiguration()

    // ----------------------------------------------------------------
    // ---- configuration
    // ----------------------------------------------------------------
    private class DLNACastServiceConfiguration : AndroidUpnpServiceConfiguration() {
        override fun getRegistryMaintenanceIntervalMillis(): Int {
            return 5000 //default is 3000!
        }

        override fun getExclusiveServiceTypes(): Array<ServiceType> {
            return arrayOf(
                DLNACastManager.SERVICE_RENDERING_CONTROL,
                DLNACastManager.SERVICE_AV_TRANSPORT,
                DLNACastManager.SERVICE_CONNECTION_MANAGER,
                DLNACastManager.SERVICE_CONTENT_DIRECTORY
            )
        }
    }
}