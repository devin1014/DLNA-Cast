/*
 * Copyright (C) 2014 Kevin Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.cast.dlna.dms;

import com.orhanobut.logger.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JettyResourceServer {
    private static final int JETTY_SERVER_PORT = 9090;
    private static final ExecutorService sThreadPool = Executors.newCachedThreadPool();
    private final Server mServer;

    public JettyResourceServer() {
        this(JETTY_SERVER_PORT);
    }

    public JettyResourceServer(int port) {
        mServer = new Server(port); // Has its own QueuedThreadPool
        mServer.setGracefulShutdown(1000); // Let's wait a second for ongoing transfers to complete
    }

    synchronized public void start() {
        if (!mServer.isStarted() && !mServer.isStarting()) {
            sThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ServletContextHandler context = new ServletContextHandler();
                    context.setContextPath("/");
                    context.setInitParameter("org.eclipse.jetty.servlet.Default.gzip", "false");
                    context.addServlet(AudioResourceServlet.class, "/audio/*");
                    context.addServlet(VideoResourceServlet.class, "/video/*");
                    mServer.setHandler(context);
                    Logger.i("Starting JettyResourceServer");
                    try {
                        mServer.start();
                    } catch (Exception ex) {
                        Logger.e(ex, "Couldn't start Jetty server!");
                    }
                }
            });
        }
    }

    synchronized public void stop() {
        if (!mServer.isStopped() && !mServer.isStopping()) {
            Logger.i("Stopping JettyResourceServer");
            try {
                mServer.stop();
            } catch (Exception ex) {
                Logger.e(ex, "Couldn't stop Jetty server!");
            }
        }
    }

    public String getServerState() {
        return mServer.getState();
    }
}
