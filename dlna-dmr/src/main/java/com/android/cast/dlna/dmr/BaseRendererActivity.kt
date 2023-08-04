package com.android.cast.dlna.dmr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.dmr.service.keyExtraCastAction

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

    open fun onServiceConnected() {}

    protected val castAction: CastAction?
        get() = intent.getParcelableExtra(keyExtraCastAction)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!castAction?.stop.isNullOrBlank()) {
            finish()
            return
        }
        bindService(Intent(this, DLNARendererService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        intent = newIntent
        if (!castAction?.stop.isNullOrBlank()) {
            finish()
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        rendererService?.bindRealPlayer(null)
        super.onDestroy()
    }
}