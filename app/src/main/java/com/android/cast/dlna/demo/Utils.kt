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

// ---------------------------------------------
// ---- URL
// ---------------------------------------------
const val castVideoMp4Url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
const val castVideoMp4Url_2 = "http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4"
const val castVideoMp4Url_3 = "https://video.699pic.com/videos/39/09/13/sUXhxQmpaNf91534390913.mp4"
const val castVideoM3u8Url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"
const val castVideoLocalUrl = "http://172.16.2.37:8192/storage/emulated/0/DCIM/Camera/20230705_165729.mp4"

data class VideoUrl(val url: String, val title: String)

val videoUrlList = mutableListOf(
    VideoUrl(castVideoMp4Url_3, "20秒的短视频, mp4格式"),
    VideoUrl(castVideoMp4Url, "1分钟的视频, mp4格式"),
    VideoUrl(castVideoMp4Url_2, "10分钟的视频, mp4格式"),
    VideoUrl(castVideoM3u8Url, "标准苹果测试流, m3u8格式"),
    VideoUrl(castVideoLocalUrl, "本地视频"),
)

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