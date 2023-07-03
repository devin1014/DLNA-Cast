package com.android.cast.dlna.dmr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.dmr.R.drawable
import com.android.cast.dlna.dmr.RenderControl.DefaultRenderControl
import com.android.cast.dlna.dmr.service.*
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.android.FixedAndroidLogHandler
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.ValidationException
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser
import org.seamless.util.logging.LoggingUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

/**
 *
 */
open class DLNARendererService : AndroidUpnpServiceImpl() {

    private val renderControlManager = RenderControlManager()
    var avTransportLastChange: LastChange? = null
        private set
    var audioControlLastChange: LastChange? = null
        private set
    private var rendererDevice: LocalDevice? = null
    private val serviceBinder = RendererServiceBinder()

    override fun createConfiguration(): UpnpServiceConfiguration {
        return object : AndroidUpnpServiceConfiguration() {
            override fun getAliveIntervalMillis(): Int {
                return 5 * 1000
            }
        }
    }

    override fun onCreate() {
        LoggingUtil.resetRootHandler(FixedAndroidLogHandler())
        super.onCreate()
        val ipAddress = Utils.getWiFiInfoIPAddress(applicationContext)
        renderControlManager.addControl(AudioRenderController(applicationContext))
        renderControlManager.addControl(AVTransportController(applicationContext, DefaultRenderControl()))
        try {
            rendererDevice = createRendererDevice(applicationContext, ipAddress)
            upnpService.registry.addDevice(rendererDevice)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = serviceBinder

    override fun onDestroy() {
        if (rendererDevice != null && upnpService != null && upnpService.registry != null) {
            upnpService.registry.removeDevice(rendererDevice)
        }
        super.onDestroy()
    }

    fun setRenderControl(control: RenderControl?) {
        renderControlManager.addControl(AVTransportController(applicationContext, control!!))
    }

    @Throws(ValidationException::class, IOException::class)
    protected fun createRendererDevice(context: Context?, ipAddress: String): LocalDevice {
        val deviceIdentity = DeviceIdentity(createUniqueSystemIdentifier(ID_SALT, ipAddress))
        val deviceType = UDADeviceType(TYPE_MEDIA_PLAYER, VERSION)
        val details = DeviceDetails(
            String.format("DMR  (%s)", Build.MODEL),
            ManufacturerDetails(Build.MANUFACTURER),
            ModelDetails(Build.MODEL, DMS_DESC, "v1", String.format("http://%s:%s", ipAddress, "8191"))
        )
        var icons: Array<Icon>? = null
        val drawable = ContextCompat.getDrawable(context!!, drawable.ic_launcher) as BitmapDrawable?
        if (drawable != null) {
            val bitmap = drawable.bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(PNG, 100, stream)
            val byteArrayInputStream = ByteArrayInputStream(stream.toByteArray())
            icons = arrayOf(Icon("image/png", 48, 48, 8, "icon.png", byteArrayInputStream))
        }
        return LocalDevice(deviceIdentity, deviceType, details, icons, generateLocalServices())
    }

    protected fun generateLocalServices(): Array<LocalService<*>> {

        // connection
        val connectionManagerService = AnnotationLocalServiceBinder().read(ConnectionManagerServiceImpl::class.java) as LocalService<ConnectionManagerServiceImpl>
        connectionManagerService.manager = object : DefaultServiceManager<ConnectionManagerServiceImpl>(connectionManagerService, ConnectionManagerServiceImpl::class.java) {
            override fun createServiceInstance(): ConnectionManagerServiceImpl {
                return ConnectionManagerServiceImpl()
            }
        }

        // av transport service
        avTransportLastChange = LastChange(AVTransportLastChangeParser())
        val avTransportService = AnnotationLocalServiceBinder().read(AVTransportServiceImpl::class.java) as LocalService<AVTransportServiceImpl>
        avTransportService.manager = object : LastChangeAwareServiceManager<AVTransportServiceImpl>(avTransportService, AVTransportLastChangeParser()) {
            override fun createServiceInstance(): AVTransportServiceImpl {
                return AVTransportServiceImpl(avTransportLastChange, renderControlManager)
            }
        }

        // render service
        audioControlLastChange = LastChange(RenderingControlLastChangeParser())
        val renderingControlService = AnnotationLocalServiceBinder().read(AudioRenderServiceImpl::class.java) as LocalService<AudioRenderServiceImpl>
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
    protected inner class RendererServiceBinder : Binder() {
        val rendererService: DLNARendererService
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