package com.android.cast.dlna.dmr

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.core.toIcon
import com.android.cast.dlna.dmr.RenderControl.DefaultRenderControl
import com.android.cast.dlna.dmr.service.AVTransportController
import com.android.cast.dlna.dmr.service.AVTransportServiceImpl
import com.android.cast.dlna.dmr.service.AudioRenderController
import com.android.cast.dlna.dmr.service.AudioRenderServiceImpl
import com.android.cast.dlna.dmr.service.ConnectionManagerServiceImpl
import com.android.cast.dlna.dmr.service.RenderControlManager
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.android.FixedAndroidLogHandler
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.ValidationException
import org.fourthline.cling.model.meta.DeviceDetails
import org.fourthline.cling.model.meta.DeviceIdentity
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.LocalService
import org.fourthline.cling.model.meta.ManufacturerDetails
import org.fourthline.cling.model.meta.ModelDetails
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser
import org.seamless.util.logging.LoggingUtil
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

/**
 *
 */
open class DLNARendererService : AndroidUpnpServiceImpl() {

    private val renderControlManager = RenderControlManager()
    var avTransportLastChange: LastChange? = null
        private set
    var audioControlLastChange: LastChange? = null
        private set
    private var localDevice: LocalDevice? = null
    private val serviceBinder = RendererServiceBinder()

    override fun createConfiguration(): UpnpServiceConfiguration {
        return object : AndroidUpnpServiceConfiguration() {
            override fun getAliveIntervalMillis(): Int = 5 * 1000
        }
    }

    override fun onCreate() {
        LoggingUtil.resetRootHandler(FixedAndroidLogHandler())
        super.onCreate()
        renderControlManager.addControl(AudioRenderController(applicationContext))
        renderControlManager.addControl(AVTransportController(applicationContext, DefaultRenderControl()))
        try {
            localDevice = createRendererDevice(applicationContext, Utils.getWiFiInfoIPAddress(applicationContext))
            upnpService.registry.addDevice(localDevice)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent): IBinder? = serviceBinder

    override fun onDestroy() {
        localDevice?.also { device ->
            upnpService.registry.removeDevice(device)
        }
        super.onDestroy()
    }

    fun setRenderControl(control: RenderControl) {
        renderControlManager.addControl(AVTransportController(applicationContext, control))
    }

    @Throws(ValidationException::class, IOException::class)
    protected fun createRendererDevice(context: Context, ipAddress: String): LocalDevice {
        return LocalDevice(
            DeviceIdentity(createUniqueSystemIdentifier(ID_SALT, ipAddress)),
            UDADeviceType(TYPE_MEDIA_PLAYER, VERSION),
            DeviceDetails(
                String.format("DMR  (%s)", Build.MODEL),
                ManufacturerDetails(Build.MANUFACTURER),
                ModelDetails(Build.MODEL, DMS_DESC, "v1", String.format("http://%s:%s", ipAddress, "8191"))
            ),
            (ContextCompat.getDrawable(context, R.drawable.ic_launcher) as? BitmapDrawable)?.bitmap?.toIcon(),
            generateLocalServices()
        )
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun generateLocalServices(): Array<LocalService<*>> {
        val serviceBinder = AnnotationLocalServiceBinder()
        // connection
        val connectionManagerService = serviceBinder.read(ConnectionManagerServiceImpl::class.java) as LocalService<ConnectionManagerServiceImpl>
        connectionManagerService.manager = object : DefaultServiceManager<ConnectionManagerServiceImpl>(connectionManagerService, ConnectionManagerServiceImpl::class.java) {
            override fun createServiceInstance(): ConnectionManagerServiceImpl {
                return ConnectionManagerServiceImpl()
            }
        }

        // av transport service
        avTransportLastChange = LastChange(AVTransportLastChangeParser())
        val avTransportService = serviceBinder.read(AVTransportServiceImpl::class.java) as LocalService<AVTransportServiceImpl>
        avTransportService.manager = object : LastChangeAwareServiceManager<AVTransportServiceImpl>(avTransportService, AVTransportLastChangeParser()) {
            override fun createServiceInstance(): AVTransportServiceImpl {
                return AVTransportServiceImpl(avTransportLastChange, renderControlManager)
            }
        }

        // render service
        audioControlLastChange = LastChange(RenderingControlLastChangeParser())
        val renderingControlService = serviceBinder.read(AudioRenderServiceImpl::class.java) as LocalService<AudioRenderServiceImpl>
        renderingControlService.manager = object : LastChangeAwareServiceManager<AudioRenderServiceImpl>(renderingControlService, RenderingControlLastChangeParser()) {
            override fun createServiceInstance(): AudioRenderServiceImpl {
                return AudioRenderServiceImpl(audioControlLastChange, renderControlManager)
            }
        }
        return arrayOf(connectionManagerService, avTransportService, renderingControlService)
    }

    // -------------------------------------------------------------------------------------------
    // - Binder
    // -------------------------------------------------------------------------------------------
    protected inner class RendererServiceBinder : AndroidUpnpServiceImpl.Binder(), RendererService {
        override val service: DLNARendererService
            get() = this@DLNARendererService
    }

    companion object {
        fun startService(context: Context) {
            context.applicationContext.startService(Intent(context, DLNARendererService::class.java))
        }

        // -------------------------------------------------------------------------------------------
        // - MediaPlayer Device
        // -------------------------------------------------------------------------------------------
        private const val DMS_DESC = "MPI MediaPlayer"
        private const val ID_SALT = "MediaPlayer"
        const val TYPE_MEDIA_PLAYER = "MediaRenderer"
        private const val VERSION = 1

        @Suppress("SameParameterValue")
        private fun createUniqueSystemIdentifier(salt: String, ipAddress: String): UDN {
            val builder = StringBuilder()
            builder.append(ipAddress)
            builder.append(Build.MODEL)
            builder.append(Build.MANUFACTURER)
            return try {
                val hash = MessageDigest.getInstance("MD5").digest(builder.toString().toByteArray())
                UDN(UUID(BigInteger(-1, hash).toLong(), salt.hashCode().toLong()))
            } catch (ex: Exception) {
                UDN(if (ex.message != null) ex.message else "UNKNOWN")
            }
        }
    }
}

interface RendererService {
    val service: DLNARendererService
}