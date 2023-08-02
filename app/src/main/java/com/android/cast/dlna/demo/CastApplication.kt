package com.android.cast.dlna.demo

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.logging.Level

class CastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        java.util.logging.Logger.getLogger("org.fourthline.cling").level = Level.CONFIG
        com.android.cast.dlna.core.Logger.printThread = true
        com.android.cast.dlna.core.Logger.enabled = true
        com.android.cast.dlna.core.Logger.level = com.android.cast.dlna.core.Level.D
        com.android.cast.dlna.core.Logger.create("CastApplication").i("Application onCreate.")
    }
}

// ---------------------------------------------
// ---- URL
// ---------------------------------------------
const val castVideoMp4Url_20s = "https://video.699pic.com/videos/39/09/13/sUXhxQmpaNf91534390913.mp4"
const val castVideoMp4Url_1min = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
const val castVideoMp4Url_10min = "http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4"
const val castVideoM3u8Url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"

//const val castVideoM3u8Url_480x270 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/v2/prog_index.m3u8"
const val castVideoM3u8Url_960x540 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/v5/prog_index.m3u8"
const val castVideoM3u8Url_1920x1080 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/v9/prog_index.m3u8"

data class VideoUrl(val url: String, val title: String)

val videoUrlList = mutableListOf(
    VideoUrl(castVideoMp4Url_20s, "20秒的短视频(mp4)"),
    VideoUrl(castVideoMp4Url_1min, "1分钟的视频(mp4)"),
    VideoUrl(castVideoMp4Url_10min, "10分钟的视频(mp4)"),
    VideoUrl(castVideoM3u8Url, "标准苹果测试流(m3u8)-多码率[部分Tv端国内App不支持]"),
    VideoUrl(castVideoM3u8Url_960x540, "标准苹果测试流(m3u8)-960x540"),
    VideoUrl(castVideoM3u8Url_1920x1080, "标准苹果测试流(m3u8)-1920x1080"),
)

internal fun AppCompatActivity.replace(id: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(id, fragment).commit()
}

internal fun Fragment.replace(id: Int, fragment: Fragment) {
    childFragmentManager.beginTransaction().replace(id, fragment).commit()
}