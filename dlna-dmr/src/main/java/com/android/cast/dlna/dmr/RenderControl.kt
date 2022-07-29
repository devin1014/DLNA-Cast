package com.android.cast.dlna.dmr

import android.widget.VideoView

/**
 *
 */
interface RenderControl {
    val position: Long
    val duration: Long

    fun play()
    fun pause()
    fun seek(position: Long)
    fun stop()

    // -------------------------------------------------------------------------------------------
    // - VideoView impl
    // -------------------------------------------------------------------------------------------
    class VideoViewRenderControl(private val videoView: VideoView) : RenderControl {
        override val position: Long = videoView.currentPosition.toLong()
        override val duration: Long = videoView.duration.toLong()

        override fun play() = videoView.start()
        override fun pause() = videoView.pause()
        override fun seek(position: Long) = videoView.seekTo(position.toInt())
        override fun stop() = videoView.stopPlayback()
    }

    // -------------------------------------------------------------------------------------------
    // - Default impl
    // -------------------------------------------------------------------------------------------
    class DefaultRenderControl : RenderControl {
        override val position: Long = 0L
        override val duration: Long = 0L

        override fun play() {}
        override fun pause() {}
        override fun seek(position: Long) {}
        override fun stop() {}
    }
}