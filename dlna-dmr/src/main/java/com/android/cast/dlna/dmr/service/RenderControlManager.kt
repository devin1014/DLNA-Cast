package com.android.cast.dlna.dmr.service

import com.android.cast.dlna.dmr.service.IRendererInterface.IAVTransportControl
import com.android.cast.dlna.dmr.service.IRendererInterface.IAudioControl
import com.android.cast.dlna.dmr.service.IRendererInterface.IControl
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes

class RenderControlManager {
    private var avControlUnsignedIntegerFourBytes: Array<UnsignedIntegerFourBytes>? = null
    private val avControlMap: MutableMap<UnsignedIntegerFourBytes, IAVTransportControl> = LinkedHashMap()
    private var audioControlUnsignedIntegerFourBytes: Array<UnsignedIntegerFourBytes>? = null
    private val audioControlMap: MutableMap<UnsignedIntegerFourBytes, IAudioControl> = LinkedHashMap()

    fun addControl(control: IControl) {
        if (control is IAVTransportControl) {
            avControlMap[control.instanceId] = control
            avControlUnsignedIntegerFourBytes = null
        } else if (control is IAudioControl) {
            audioControlMap[control.instanceId] = control
            audioControlUnsignedIntegerFourBytes = null
        }
    }

    // ---- Av
    fun getAvTransportControl(instanceId: UnsignedIntegerFourBytes): IAVTransportControl? = avControlMap[instanceId]

    val avTransportCurrentInstanceIds: Array<UnsignedIntegerFourBytes>
        get() {
            if (avControlUnsignedIntegerFourBytes == null) {
                avControlUnsignedIntegerFourBytes = avControlMap.keys.toTypedArray()
            }
            return avControlUnsignedIntegerFourBytes!!
        }

    // ---- Audio
    fun getAudioControl(instanceId: UnsignedIntegerFourBytes): IAudioControl? = audioControlMap[instanceId]

    val audioControlCurrentInstanceIds: Array<UnsignedIntegerFourBytes>
        get() {
            if (audioControlUnsignedIntegerFourBytes == null) {
                audioControlUnsignedIntegerFourBytes = audioControlMap.keys.toTypedArray()
            }
            return audioControlUnsignedIntegerFourBytes!!
        }
}