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

final class JettyHttpServer implements IResourceServer {
    private final Server mServer;

    public JettyHttpServer(int port) {
        mServer = new Server(port); // Has its own QueuedThreadPool
        mServer.setGracefulShutdown(1000); // Let's wait a second for ongoing transfers to complete
    }

    synchronized public void startServer() {
        if (!mServer.isStarted() && !mServer.isStarting()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ServletContextHandler context = new ServletContextHandler();
                    context.setContextPath("/");
                    context.setInitParameter("org.eclipse.jetty.servlet.Default.gzip", "false");
                    // context.addServlet(ContentResourceServlet.AudioResourceServlet.class, "/audio/*");
                    // context.addServlet(ContentResourceServlet.VideoResourceServlet.class, "/video/*");
                    context.addServlet(ContentResourceServlet.class, "/");
                    mServer.setHandler(context);
                    Logger.i("JettyServer start.");
                    try {
                        mServer.start();
                        mServer.join();
                        Logger.i("JettyServer complete.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    synchronized public void stopServer() {
        if (!mServer.isStopped() && !mServer.isStopping()) {
            try {
                Logger.i("JettyServer stop.");
                mServer.stop();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
