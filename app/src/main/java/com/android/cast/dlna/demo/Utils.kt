package com.android.cast.dlna.demo

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class Utils {
    companion object {
        private const val WIFI_DISABLED = "<disabled>"
        private const val WIFI_NO_CONNECT = "<not connect>"
        private const val WIFI_NO_PERMISSION = "<permission deny>"
        private const val UNKNOWN = "<unknown>"

        /**
         * need permission 'Manifest.permission.ACCESS_FINE_LOCATION' and 'Manifest.permission.ACCESS_WIFI_STATE' if system sdk >= Android O.
         */
        @SuppressLint("MissingPermission")
        fun getWiFiInfoSSID(context: Context): String {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) return WIFI_DISABLED
            val wifiInfo = wifiManager.connectionInfo ?: return WIFI_NO_CONNECT
            return if (wifiInfo.ssid == WifiManager.UNKNOWN_SSID) {
                if (VERSION.SDK_INT >= VERSION_CODES.O) {
                    if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(context, permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return WIFI_NO_PERMISSION
                    }
                    if (wifiManager.configuredNetworks != null) {
                        for (config in wifiManager.configuredNetworks) {
                            if (config.networkId == wifiInfo.networkId) {
                                return config.SSID.replace("\"".toRegex(), "")
                            }
                        }
                    }
                } else {
                    return WIFI_NO_CONNECT
                }
                UNKNOWN
            } else {
                wifiInfo.ssid.replace("\"".toRegex(), "")
            }
        }
    }
}

internal fun AppCompatActivity.replace(id: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(id, fragment).commit()
}

internal fun Fragment.replace(id: Int, fragment: Fragment) {
    childFragmentManager.beginTransaction().replace(id, fragment).commit()
}