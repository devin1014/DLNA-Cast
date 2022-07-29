package com.android.cast.dlna.dms

interface IResourceServer {
    fun startServer()
    fun stopServer()

    // ----------------------------------------------------------------------------
    // Factory
    // ----------------------------------------------------------------------------
    interface IResourceServerFactory {
        val port: Int
        val instance: IResourceServer

        // ----------------------------------------------------------------------------
        // ---- implement
        // ----------------------------------------------------------------------------
        class DefaultResourceServerFactoryImpl @JvmOverloads constructor(
            override val port: Int,
            useJetty: Boolean = true
        ) : IResourceServerFactory {
            override val instance: IResourceServer = if (useJetty) JettyHttpServer(port) else NanoHttpServer(port)
        }
    }
}