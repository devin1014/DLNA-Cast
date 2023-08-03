package com.android.cast.dlna.dms

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.dms.service.ContentControl
import com.android.cast.dlna.dms.service.ContentDirectoryServiceController
import com.android.cast.dlna.dms.service.ContentDirectoryServiceImpl
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import java.util.*

open class DLNAContentService : AndroidUpnpServiceImpl() {
    companion object {
        fun startService(context: Context) = context.applicationContext.startService(Intent(context, DLNAContentService::class.java))
    }

    protected inner class RendererServiceBinderWrapper : AndroidUpnpServiceImpl.Binder(), ContentServiceBinder {
        override val service: DLNAContentService
            get() = this@DLNAContentService
    }

    private val logger = Logger.create("LocalContentService")
    private val serviceBinder = RendererServiceBinderWrapper()
    private var localDevice: LocalDevice? = null
    private lateinit var contentControl: ContentControl

    override fun onCreate() {
        logger.i("DLNAContentService create.")
        super.onCreate()
        contentControl = ContentDirectoryServiceController(this)
        val baseUrl = Utils.getHttpBaseUrl(this)
        try {
            localDevice = createContentServiceDevice(baseUrl = baseUrl)
            upnpService.registry.addDevice(localDevice)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    protected open fun createContentServiceDevice(baseUrl: String): LocalDevice {
        val info = "DLNA_ContentService-$baseUrl-${Build.MODEL}-${Build.MANUFACTURER}"
        val udn = try {
            UDN(UUID.nameUUIDFromBytes(info.toByteArray()))
        } catch (ex: Exception) {
            UDN(UUID.randomUUID())
        }
        logger.i("create local device: [MediaServer][$udn]($baseUrl)")
        return LocalDevice(
            DeviceIdentity(udn),
            UDADeviceType("MediaServer", 1),
            DeviceDetails(
                "DMS (${Build.MODEL})",
                ManufacturerDetails(Build.MANUFACTURER),
                ModelDetails(Build.MODEL, "MSI MediaServer", "v1", baseUrl)
            ),
            emptyArray(),
            generateLocalServices()
        )
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun generateLocalServices(): Array<LocalService<*>> {
        val serviceBinder = AnnotationLocalServiceBinder()
        // content directory service
        val contentDirectoryService = serviceBinder.read(AbstractContentDirectoryService::class.java) as LocalService<AbstractContentDirectoryService>
        contentDirectoryService.manager = object : DefaultServiceManager<AbstractContentDirectoryService>(contentDirectoryService) {
            override fun createServiceInstance(): AbstractContentDirectoryService {
                return ContentDirectoryServiceImpl(contentControl)
            }
        }
        return arrayOf(contentDirectoryService)
    }

    override fun onBind(intent: Intent?): IBinder? = serviceBinder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        logger.w("DLNAContentService destroy.")
        localDevice?.also { device ->
            upnpService.registry.removeDevice(device)
        }
        super.onDestroy()
    }

    override fun createConfiguration(): UpnpServiceConfiguration = object : AndroidUpnpServiceConfiguration() {
        // content service never need find other device
        override fun getExclusiveServiceTypes(): Array<ServiceType>? = null
    }
}

interface ContentServiceBinder {
    val service: DLNAContentService
}