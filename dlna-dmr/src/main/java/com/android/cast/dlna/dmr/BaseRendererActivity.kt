package com.android.cast.dlna.dmr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.dmr.service.keyExtraCastAction
import org.fourthline.cling.support.model.TransportState.STOPPED

abstract class BaseRendererActivity : AppCompatActivity() {

    protected var rendererService: DLNARendererService? = null
        private set

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            rendererService = (service as RendererServiceBinder).service
            onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            rendererService = null
        }
    }

    open fun onServiceConnected() {
    }

    protected var castAction: CastAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        castAction = intent.getParcelableExtra(keyExtraCastAction)
        bindService(Intent(this, DLNARendererService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(intent)
        intent = newIntent
        castAction = newIntent.getParcelableExtra(keyExtraCastAction)
    }

    override fun onDestroy() {
        rendererService?.changeTransportState(STOPPED)
        unbindService(serviceConnection)
        rendererService?.bindRealPlayer(null)
        super.onDestroy()
    }
}