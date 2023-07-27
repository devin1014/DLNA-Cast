package com.android.cast.dlna.dmr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.dmr.service.keyExtraCastAction

abstract class BaseRendererActivity : AppCompatActivity() {

    protected val logger = Logger.create(this.javaClass.simpleName)
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
        logger.i("onCreate")
        super.onCreate(savedInstanceState)
        bindService(Intent(this, DLNARendererService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onNewIntent(newIntent: Intent) {
        logger.i("onNewIntent: $intent")
        super.onNewIntent(newIntent)
        intent = newIntent
        if (!castAction?.stop.isNullOrBlank()) {
            finish()
        }
    }

    override fun onDestroy() {
        logger.w("onDestroy")
        unbindService(serviceConnection)
        rendererService?.bindRealPlayer(null)
        super.onDestroy()
    }
}