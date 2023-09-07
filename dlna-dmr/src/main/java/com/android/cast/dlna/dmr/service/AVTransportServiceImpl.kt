package com.android.cast.dlna.dmr.service

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.AbstractAVTransportService
import org.fourthline.cling.support.model.DeviceCapabilities
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportAction
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.TransportSettings

class AVTransportServiceImpl(private val avTransportControl: AvTransportControl) : AbstractAVTransportService() {
    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> = arrayOf(UnsignedIntegerFourBytes(0))
    override fun getCurrentTransportActions(instanceId: UnsignedIntegerFourBytes): Array<TransportAction> = avTransportControl.currentTransportActions
    override fun getDeviceCapabilities(instanceId: UnsignedIntegerFourBytes): DeviceCapabilities = avTransportControl.deviceCapabilities
    override fun getMediaInfo(instanceId: UnsignedIntegerFourBytes): MediaInfo = avTransportControl.mediaInfo
    override fun getPositionInfo(instanceId: UnsignedIntegerFourBytes): PositionInfo = avTransportControl.positionInfo
    override fun getTransportInfo(instanceId: UnsignedIntegerFourBytes): TransportInfo = avTransportControl.transportInfo
    override fun getTransportSettings(instanceId: UnsignedIntegerFourBytes): TransportSettings = avTransportControl.transportSettings
    override fun next(instanceId: UnsignedIntegerFourBytes) = avTransportControl.next()
    override fun pause(instanceId: UnsignedIntegerFourBytes) = avTransportControl.pause()
    override fun play(instanceId: UnsignedIntegerFourBytes, speed: String?) = avTransportControl.play(speed)
    override fun previous(instanceId: UnsignedIntegerFourBytes) = avTransportControl.previous()
    override fun seek(instanceId: UnsignedIntegerFourBytes, unit: String?, target: String?) = avTransportControl.seek(unit, target)
    override fun setAVTransportURI(instanceId: UnsignedIntegerFourBytes, currentURI: String, currentURIMetaData: String?) =
        avTransportControl.setAVTransportURI(currentURI, currentURIMetaData)
    override fun setNextAVTransportURI(instanceId: UnsignedIntegerFourBytes, nextURI: String, nextURIMetaData: String?) =
        avTransportControl.setNextAVTransportURI(nextURI, nextURIMetaData)
    override fun setPlayMode(instanceId: UnsignedIntegerFourBytes, newPlayMode: String) = avTransportControl.setPlayMode(newPlayMode)
    override fun stop(instanceId: UnsignedIntegerFourBytes) = avTransportControl.stop()
    override fun record(instanceId: UnsignedIntegerFourBytes) {} // ignore
    override fun setRecordQualityMode(instanceId: UnsignedIntegerFourBytes, newRecordQualityMode: String) {} // ignore
}
