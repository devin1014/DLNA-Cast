package com.android.cast.dlna.demo

import org.fourthline.cling.model.meta.Device

internal interface IDisplayDevice {
    fun setCastDevice(device: Device<*, *, *>?)
}