package com.android.cast.dlna.core.http

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

interface HttpServer {
    fun startServer()
    fun stopServer()
    fun isRunning(): Boolean

    interface LocalHttpServerFactory {
        val port: Int
        val httpServer: HttpServer

        class DefaultResourceServerFactoryImpl @JvmOverloads constructor(
            override val port: Int,
            useJetty: Boolean = true,
        ) : LocalHttpServerFactory {
            override val httpServer: HttpServer = if (useJetty) JettyHttpServer(port) else NanoHttpServer(port)
        }
    }
}

class LocalServer(
    context: Context,
    private val port: Int = 8192,
    jetty: Boolean = true,
    httpServer: HttpServer = if (jetty) JettyHttpServer(port) else NanoHttpServer(port),
) : HttpServer by httpServer {

    val ip: String = getIpAddress(context)
    val baseUrl: String = "http://$ip:$port"
    val available: Boolean = ip.isNotEmpty()

    private fun getIpAddress(context: Context): String {
        var ipAddress = 0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            ipAddress = wifiManager.connectionInfo.ipAddress
        } else {
            val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.run {
                activeNetwork?.let { network ->
                    (getNetworkCapabilities(network)?.transportInfo as? WifiInfo)?.let { wifiInfo ->
                        ipAddress = wifiInfo.ipAddress
                    }
                }
            }
        }
        if (ipAddress == 0) return ""
        return (ipAddress and 0xFF).toString() + "." + (ipAddress shr 8 and 0xFF) + "." + (ipAddress shr 16 and 0xFF) + "." + (ipAddress shr 24 and 0xFF)
    }
}