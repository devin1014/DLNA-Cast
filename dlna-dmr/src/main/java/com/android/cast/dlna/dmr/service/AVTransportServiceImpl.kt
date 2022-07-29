package com.android.cast.dlna.dmr.service

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.AVTransportException
import org.fourthline.cling.support.avtransport.AbstractAVTransportService
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.*

class AVTransportServiceImpl(
    lastChange: LastChange?,
    private val mRenderControlManager: RenderControlManager
) : AbstractAVTransportService(lastChange) {

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> = mRenderControlManager.avTransportCurrentInstanceIds

    @Throws(Exception::class)
    override fun getCurrentTransportActions(instanceId: UnsignedIntegerFourBytes): Array<TransportAction> {
        return mRenderControlManager.getAvTransportControl(instanceId)!!.currentTransportActions!!
    }

    override fun getDeviceCapabilities(instanceId: UnsignedIntegerFourBytes): DeviceCapabilities {
        return mRenderControlManager.getAvTransportControl(instanceId)!!.deviceCapabilities!!
    }

    override fun getMediaInfo(instanceId: UnsignedIntegerFourBytes): MediaInfo {
        return mRenderControlManager.getAvTransportControl(instanceId)!!.mediaInfo!!
    }

    override fun getPositionInfo(instanceId: UnsignedIntegerFourBytes): PositionInfo {
        return mRenderControlManager.getAvTransportControl(instanceId)!!.positionInfo!!
    }

    override fun getTransportInfo(instanceId: UnsignedIntegerFourBytes): TransportInfo {
        return mRenderControlManager.getAvTransportControl(instanceId)!!.transportInfo!!
    }

    override fun getTransportSettings(instanceId: UnsignedIntegerFourBytes): TransportSettings {
        return mRenderControlManager.getAvTransportControl(instanceId)!!.transportSettings!!
    }

    override fun next(instanceId: UnsignedIntegerFourBytes) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.next()
    }

    @Throws(AVTransportException::class)
    override fun pause(instanceId: UnsignedIntegerFourBytes) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.pause()
    }

    @Throws(AVTransportException::class)
    override fun play(instanceId: UnsignedIntegerFourBytes, arg1: String) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.play(arg1)
    }

    override fun previous(instanceId: UnsignedIntegerFourBytes) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.previous()
    }

    override fun record(instanceId: UnsignedIntegerFourBytes) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.record()
    }

    @Throws(AVTransportException::class)
    override fun seek(instanceId: UnsignedIntegerFourBytes, arg1: String, arg2: String) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.seek(arg1, arg2)
    }

    @Throws(AVTransportException::class)
    override fun setAVTransportURI(instanceId: UnsignedIntegerFourBytes, arg1: String, arg2: String) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.setAVTransportURI(arg1, arg2)
    }

    override fun setNextAVTransportURI(instanceId: UnsignedIntegerFourBytes, arg1: String, arg2: String) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.setNextAVTransportURI(arg1, arg2)
    }

    override fun setPlayMode(instanceId: UnsignedIntegerFourBytes, arg1: String) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.setPlayMode(arg1)
    }

    override fun setRecordQualityMode(instanceId: UnsignedIntegerFourBytes, arg1: String) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.setRecordQualityMode(arg1)
    }

    @Throws(AVTransportException::class)
    override fun stop(instanceId: UnsignedIntegerFourBytes) {
        mRenderControlManager.getAvTransportControl(instanceId)!!.stop()
    }
}