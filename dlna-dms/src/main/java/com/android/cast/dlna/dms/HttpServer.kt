package com.android.cast.dlna.dms

import android.text.TextUtils
import com.orhanobut.logger.Logger
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

// ------------------------------------------------
// ---- Jetty Http
// ------------------------------------------------
internal class JettyHttpServer(port: Int) : IResourceServer {
    private val server: Server

    init {
        server = Server(port)  // Has its own QueuedThreadPool
        server.gracefulShutdown = 1000 // Let's wait a second for ongoing transfers to complete
    }

    @Synchronized
    override fun startServer() {
        if (!server.isStarted && !server.isStarting) {
            Thread {
                val context = ServletContextHandler()
                context.contextPath = "/"
                context.setInitParameter("org.eclipse.jetty.servlet.Default.gzip", "false")
                // context.addServlet(ContentResourceServlet.AudioResourceServlet.class, "/audio/*");
                // context.addServlet(ContentResourceServlet.VideoResourceServlet.class, "/video/*");
                context.addServlet(ContentResourceServlet::class.java, "/")
                server.handler = context
                Logger.i("JettyServer start.")
                try {
                    server.start()
                    server.join()
                    Logger.i("JettyServer complete.")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }.start()
        }
    }

    @Synchronized
    override fun stopServer() {
        if (!server.isStopped && !server.isStopping) {
            try {
                Logger.i("JettyServer stop.")
                server.stop()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}

// ------------------------------------------------
// ---- Nano Http
// ------------------------------------------------
internal class NanoHttpServer(port: Int) : NanoHTTPD(port), IResourceServer {

    private val mimeType = mutableMapOf(
        "jpg" to "image/*",
        "jpeg" to "image/*",
        "png" to "image/*",
        "mp3" to "audio/*",
        "mp4" to "video/*",
        "wav" to "video/*",
    )
    private val textPlain = "text/plain"

    override fun serve(session: IHTTPSession): Response {
        println("uri: " + session.uri)
        println("header: " + session.headers.toString())
        println("params: " + session.parms.toString())
        val uri = session.uri
        if (TextUtils.isEmpty(uri) || !uri.startsWith("/")) {
            return newChunkedResponse(BAD_REQUEST, textPlain, null)
        }
        val file = File(uri)
        if (!file.exists() || file.isDirectory) {
            return newChunkedResponse(NOT_FOUND, textPlain, null)
        }
        val type = uri.substring(uri.length.coerceAtMost(uri.lastIndexOf(".") + 1)).lowercase()
        var mimeType = mimeType[type]
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = textPlain
        }
        return try {
            newChunkedResponse(OK, mimeType, FileInputStream(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            newChunkedResponse(SERVICE_UNAVAILABLE, mimeType, null)
        }
    }

    override fun startServer() {
        try {
            start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun stopServer() {
        stop()
    }
}