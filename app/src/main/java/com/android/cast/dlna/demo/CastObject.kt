package com.android.cast.dlna.demo

import com.android.cast.dlna.core.ICast
import com.android.cast.dlna.core.ICast.ICastImage
import com.android.cast.dlna.core.ICast.ICastVideo

// ---------------------------------------------
// ---- URL
// ---------------------------------------------
const val CAST_VIDEO_MP4 = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
const val CAST_VIDEO_M3U8 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"
const val CAST_IMAGE_JPG = "https://seopic.699pic.com/photo/40011/2135.jpg_wh1200.jpg"

// ---------------------------------------------
// ---- Cast Object
// ---------------------------------------------
object CastObject {

    // demo
    fun newInstance(url: String, id: String, name: String): ICast {
        return if (url.endsWith(".mp4") || url.endsWith(".m3u8")) {
            CastVideo(url, id, name)
        } else if (url.endsWith(".jpg")) {
            CastImage(url, id, name)
        } else {
            throw IllegalArgumentException("please check cast object type.")
        }
    }
}

data class CastImage(
    private val url: String,
    private val id: String,
    private val name: String,
    private val size: Long = 0L
) : ICastImage {

    override fun getId(): String = id
    override fun getUri(): String = url
    override fun getName(): String = name
    override fun getSize(): Long = size
}

data class CastVideo(
    private val url: String,
    private val id: String,
    private val name: String,
    private val size: Long = 0L,
    private val bitrate: Long = 0
) : ICastVideo {

    var duration: Long = 0

    override fun getId(): String = id
    override fun getUri(): String = url
    override fun getName(): String = name
    override fun getDurationMillSeconds(): Long = duration
    override fun getSize(): Long = size
    override fun getBitrate(): Long = bitrate
}