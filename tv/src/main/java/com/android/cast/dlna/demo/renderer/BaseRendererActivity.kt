package com.android.cast.dlna.demo.renderer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.dmr.DLNARendererService
import com.android.cast.dlna.dmr.RendererService
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable
import org.fourthline.cling.support.model.Channel.Master
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.TransportState.STOPPED
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable.Volume

abstract class BaseRendererActivity : AppCompatActivity() {

    private val instanceId = UnsignedIntegerFourBytes(0)
    protected var rendererService: DLNARendererService? = null
        private set

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            onServiceConnected((service as RendererService).service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            rendererService = null
        }
    }

    @CallSuper
    open fun onServiceConnected(service: DLNARendererService) {
        rendererService = service
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService(Intent(this, DLNARendererService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        notifyTransportStateChanged(STOPPED)
        unbindService(serviceConnection)
        super.onDestroy()
    }

    protected fun notifyTransportStateChanged(transportState: TransportState) {
        rendererService?.avTransportLastChange?.setEventedValue(instanceId, AVTransportVariable.TransportState(transportState))
    }

    protected fun notifyRenderVolumeChanged(volume: Int) {
        rendererService?.audioControlLastChange?.setEventedValue(instanceId, Volume(ChannelVolume(Master, volume)))
    }
}