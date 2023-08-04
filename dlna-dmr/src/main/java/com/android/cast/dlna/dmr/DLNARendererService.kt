package com.android.cast.dlna.dmr

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.core.getLogger
import com.android.cast.dlna.dmr.service.AVTransportController
import com.android.cast.dlna.dmr.service.AVTransportServiceImpl
import com.android.cast.dlna.dmr.service.AudioControl
import com.android.cast.dlna.dmr.service.AudioRenderController
import com.android.cast.dlna.dmr.service.AudioRenderServiceImpl
import com.android.cast.dlna.dmr.service.AvTransportControl
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.ValidationException
import org.fourthline.cling.model.meta.DeviceDetails
import org.fourthline.cling.model.meta.DeviceIdentity
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.LocalService
import org.fourthline.cling.model.meta.ManufacturerDetails
import org.fourthline.cling.model.meta.ModelDetails
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDAServiceId
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager
import org.fourthline.cling.support.model.Channel
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable.Volume
import java.io.IOException
import java.util.UUID

open class DLNARendererService : AndroidUpnpServiceImpl() {
    companion object {
        fun startService(context: Context) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.applicationContext.startForegroundService(Intent(context, DLNARendererService::class.java))
//            } else {
            context.applicationContext.startService(Intent(context, DLNARendererService::class.java))
//            }
        }
    }

    private val logger = getLogger("RendererService")
    private val serviceBinder = RendererServiceBinderWrapper()
    private lateinit var avTransportControl: AvTransportControl
    private lateinit var audioControl: AudioControl
    private var localDevice: LocalDevice? = null

    override fun createConfiguration(): UpnpServiceConfiguration {
        return object : AndroidUpnpServiceConfiguration() {
            override fun getAliveIntervalMillis(): Int = 5 * 1000
        }
    }

    override fun onCreate() {
        logger.i("DLNARendererService create.")
        super.onCreate()
        avTransportControl = AVTransportController(applicationContext)
        audioControl = AudioRenderController(applicationContext)
        try {
            localDevice = createRendererDevice(Utils.getHttpBaseUrl(applicationContext))
            upnpService.registry.addDevice(localDevice)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent): IBinder? = serviceBinder

    override fun onDestroy() {
        logger.w("DLNARendererService destroy.")
        localDevice?.also { device ->
            upnpService.registry.removeDevice(device)
        }
        (avTransportControl as? AVTransportController)?.mediaControl = null
        super.onDestroy()
    }

    @Throws(ValidationException::class, IOException::class)
    protected fun createRendererDevice(baseUrl: String): LocalDevice {
        val info = "DLNA_MediaPlayer-$baseUrl-${Build.MODEL}-${Build.MANUFACTURER}"
        val udn = try {
            UDN(UUID.nameUUIDFromBytes(info.toByteArray()))
        } catch (ex: Exception) {
            UDN(UUID.randomUUID())
        }
        logger.i("create local device: [MediaRenderer][${udn.identifierString.split("-").last()}]($baseUrl)")
        return LocalDevice(
            DeviceIdentity(udn),
            UDADeviceType("MediaRenderer", 1),
            DeviceDetails(
                "DMR (${Build.MODEL})",
                ManufacturerDetails(Build.MANUFACTURER),
                ModelDetails(Build.MODEL, "MPI MediaPlayer", "v1", baseUrl)
            ),
            emptyArray(),
            generateLocalServices()
        )
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun generateLocalServices(): Array<LocalService<*>> {
        val serviceBinder = AnnotationLocalServiceBinder()
        // av transport service
        val avTransportService = serviceBinder.read(AVTransportServiceImpl::class.java) as LocalService<AVTransportServiceImpl>
        avTransportService.manager = object : LastChangeAwareServiceManager<AVTransportServiceImpl>(avTransportService, AVTransportLastChangeParser()) {
            override fun createServiceInstance(): AVTransportServiceImpl {
                return AVTransportServiceImpl(avTransportControl)
            }
        }
        // render service
        val renderingControlService = serviceBinder.read(AudioRenderServiceImpl::class.java) as LocalService<AudioRenderServiceImpl>
        renderingControlService.manager = object : LastChangeAwareServiceManager<AudioRenderServiceImpl>(renderingControlService, RenderingControlLastChangeParser()) {
            override fun createServiceInstance(): AudioRenderServiceImpl {
                return AudioRenderServiceImpl(audioControl)
            }
        }
        return arrayOf(avTransportService, renderingControlService)
    }

//    fun updateDevice() {
//        localDevice?.run {
//            upnpService.registry.addDevice(this)
//        }
//    }

    fun bindRealPlayer(control: RenderControl?) {
        (avTransportControl as? AVTransportController)?.mediaControl = control
    }

    fun notifyAvTransportLastChange(state: RenderState) {
        notifyAvTransportLastChange(AVTransportVariable.TransportState(state.toTransportState()))
    }

    private fun notifyAvTransportLastChange(event: EventedValue<*>) {
        val manager = localDevice?.findService(UDAServiceId("AVTransport"))?.manager
        (manager?.implementation as? AVTransportServiceImpl)?.lastChange?.setEventedValue(0, event)
        (manager as? LastChangeAwareServiceManager)?.fireLastChange()
    }

    fun notifyRenderControlLastChange(volume: Int) {
        val manager = localDevice?.findService(UDAServiceId("RenderingControl"))?.manager
        (manager?.implementation as? AudioRenderServiceImpl)?.lastChange?.setEventedValue(0, Volume(ChannelVolume(Channel.Master, volume)))
        (manager as? LastChangeAwareServiceManager)?.fireLastChange()
    }

    // ---- BinderWrapper
    protected inner class RendererServiceBinderWrapper : AndroidUpnpServiceImpl.Binder(), RendererServiceBinder {
        override val service: DLNARendererService
            get() = this@DLNARendererService
    }
}

interface RendererServiceBinder {
    val service: DLNARendererService
}
