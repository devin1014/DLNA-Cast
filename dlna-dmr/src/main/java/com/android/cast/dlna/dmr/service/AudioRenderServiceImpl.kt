package com.android.cast.dlna.dmr.service

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.Channel
import org.fourthline.cling.support.model.Channel.Master
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl

class AudioRenderServiceImpl(lastChange: LastChange?, private val renderControlManager: RenderControlManager) : AbstractAudioRenderingControl(lastChange) {
    override fun setMute(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredMute: Boolean) {
        renderControlManager.getAudioControl(instanceId)!!.setMute(channelName, desiredMute)
    }

    override fun getMute(instanceId: UnsignedIntegerFourBytes, channelName: String): Boolean {
        return renderControlManager.getAudioControl(instanceId)!!.getMute(channelName)
    }

    override fun setVolume(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredVolume: UnsignedIntegerTwoBytes) {
        renderControlManager.getAudioControl(instanceId)!!.setVolume(channelName, desiredVolume)
    }

    override fun getVolume(instanceId: UnsignedIntegerFourBytes, channelName: String): UnsignedIntegerTwoBytes {
        return renderControlManager.getAudioControl(instanceId)!!.getVolume(channelName)!!
    }

    override fun getCurrentChannels(): Array<Channel> {
        return mMasterChannel
    }

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> {
        return renderControlManager.audioControlCurrentInstanceIds
    }

    companion object {
        private val mMasterChannel = arrayOf(Master)
    }
}