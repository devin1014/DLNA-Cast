package com.android.cast.dlna.dmr

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import org.fourthline.cling.support.model.TransportState

/**
 *
 */
interface RenderControl {
    val currentPosition: Long
    val duration: Long

    fun play(speed: Double? = 1.0)
    fun pause()
    fun seek(millSeconds: Long)
    fun stop()
    fun getState(): RenderState
}

enum class RenderState {
    IDLE, PREPARING, PLAYING, PAUSED, STOPPED, ERROR;

    fun toTransportState(): TransportState {
        return when (this) {
            PLAYING, PREPARING -> TransportState.PLAYING
            PAUSED -> TransportState.PAUSED_PLAYBACK
            STOPPED, ERROR -> TransportState.STOPPED
            else -> TransportState.NO_MEDIA_PRESENT
        }
    }
}

class CastAction(
    var currentURI: String? = null,
    var currentURIMetaData: String? = null,
    var nextURI: String? = null,
    var nextURIMetaData: String? = null,
    var stop: String? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(currentURI)
        parcel.writeString(currentURIMetaData)
        parcel.writeString(nextURI)
        parcel.writeString(nextURIMetaData)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Creator<CastAction> {
        override fun createFromParcel(parcel: Parcel): CastAction {
            return CastAction(parcel)
        }

        override fun newArray(size: Int): Array<CastAction?> {
            return arrayOfNulls(size)
        }
    }
}