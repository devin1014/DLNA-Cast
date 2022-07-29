package com.android.cast.dlna.dms

import android.content.Context
import android.os.Build
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.dms.IResourceServer.IResourceServerFactory
import com.android.cast.dlna.dms.IResourceServer.IResourceServerFactory.DefaultResourceServerFactoryImpl
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.ValidationException
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.DeviceType
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

class MediaServer @JvmOverloads constructor(
    context: Context,
    factory: IResourceServerFactory = DefaultResourceServerFactoryImpl(PORT)
) {

    companion object {
        private const val DMS_DESC = "MSI MediaServer"
        private const val ID_SALT = "GNaP-MediaServer"
        private const val TYPE_MEDIA_SERVER = "MediaServer"
        private const val VERSION = 1
        private const val PORT = 8192
    }

    var device: LocalDevice? = null
    private var mResourceServer: IResourceServer? = null
    val baseUrl: String


    init {
        val address = Utils.getWiFiInfoIPAddress(context)
        baseUrl = String.format("http://%s:%s", address, factory.port)
        ContentFactory.setServerUrl(context, baseUrl)
        try {
            device = createLocalDevice(context, address)
            mResourceServer = factory.instance
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun start() {
        mResourceServer?.startServer()
    }

    fun stop() {
        mResourceServer?.stopServer()
    }

    @Throws(ValidationException::class)
    protected fun createLocalDevice(context: Context, ipAddress: String): LocalDevice {
        val identity = DeviceIdentity(createUniqueSystemIdentifier(ID_SALT, ipAddress))
        val type: DeviceType = UDADeviceType(TYPE_MEDIA_SERVER, VERSION)
        val details = DeviceDetails(
            String.format("DMS  (%s)", Build.MODEL),
            ManufacturerDetails(Build.MANUFACTURER),
            ModelDetails(Build.MODEL, DMS_DESC, "v1", baseUrl)
        )
        val service = AnnotationLocalServiceBinder().read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>
        service.manager = DefaultServiceManager(service, ContentDirectoryService::class.java)
        var icon: Icon? = null
        try {
            icon = Icon(
                "image/png", 48, 48, 32, "msi.png",
                context.resources.assets.open("ic_launcher.png")
            )
        } catch (ignored: IOException) {
        }
        return LocalDevice(identity, type, details, icon, service)
    }

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