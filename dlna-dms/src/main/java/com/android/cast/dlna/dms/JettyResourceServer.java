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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.logging.Logger;

public class JettyResourceServer implements Runnable {
    private final static Logger log = Logger.getLogger(JettyResourceServer.class.getName());
    public static final int JETTY_SERVER_PORT = 9090;
    private final Server mServer;

    public JettyResourceServer() {
        mServer = new Server(JETTY_SERVER_PORT); // Has its own QueuedThreadPool
        mServer.setGracefulShutdown(1000); // Let's wait a second for ongoing transfers to complete
    }

    synchronized public void startIfNotRunning() {
        if (!mServer.isStarted() && !mServer.isStarting()) {
            log.info("Starting JettyResourceServer");
            try {
                mServer.start();
            } catch (Exception ex) {
                log.severe("Couldn't start Jetty server: " + ex);
                throw new RuntimeException(ex);
            }
        }
    }

    synchronized public void stopIfRunning() {
        if (!mServer.isStopped() && !mServer.isStopping()) {
            log.info("Stopping JettyResourceServer");
            try {
                mServer.stop();
            } catch (Exception ex) {
                log.severe("Couldn't stop Jetty server: " + ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public String getServerState() {
        return mServer.getState();
    }

    @Override
    public void run() {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setInitParameter("org.eclipse.jetty.servlet.Default.gzip", "false");
        mServer.setHandler(context);
        context.addServlet(AudioResourceServlet.class, "/audio/*");
        context.addServlet(VideoResourceServlet.class, "/video/*");
        startIfNotRunning();
    }

}
