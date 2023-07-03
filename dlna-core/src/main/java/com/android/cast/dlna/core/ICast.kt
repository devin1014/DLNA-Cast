package com.android.cast.dlna.core

interface ICast {
    val id: String
    val uri: String
    val name: String?

    interface ICastVideo : ICast {
        val durationMillSeconds: Long // video duration, ms
        val size: Long
        val bitrate: Long
    }

    interface ICastAudio : ICast {
        val durationMillSeconds: Long // audio duration, ms
        val size: Long
    }

    interface ICastImage : ICast {
        val size: Long
    }
}