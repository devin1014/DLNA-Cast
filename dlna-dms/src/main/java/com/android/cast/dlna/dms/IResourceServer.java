package com.android.cast.dlna.dms;

interface IResourceServer {
    void startServer();

    void stopServer();

    // ----------------------------------------------------------------------------
    // Factory
    // ----------------------------------------------------------------------------
    interface IResourceServerFactory {
        int getPort();

        IResourceServer getInstance();

        // ----------------------------------------------------------------------------
        // ---- implement
        // ----------------------------------------------------------------------------
        final class DefaultResourceServerFactoryImpl implements IResourceServerFactory {
            private final int port;
            private final boolean useJetty;

            public DefaultResourceServerFactoryImpl(int port) {
                this(port, true);
            }

            public DefaultResourceServerFactoryImpl(int port, boolean useJetty) {
                this.port = port;
                this.useJetty = useJetty;
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public IResourceServer getInstance() {
                if (useJetty)
                    return new JettyHttpServer(port);
                else
                    return new NanoHttpServer(port);
            }
        }
    }
}
