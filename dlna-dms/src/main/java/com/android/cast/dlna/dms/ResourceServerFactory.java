package com.android.cast.dlna.dms;

interface ResourceServerFactory {
    int getPort();

    LocalResourceServer getInstance();

    // ----------------------------------------------------------------------------
    // ---- implement
    // ----------------------------------------------------------------------------
    final class DefaultResourceServerFactoryImpl implements ResourceServerFactory {
        private final int port;

        public DefaultResourceServerFactoryImpl(int port) {
            this.port = port;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public LocalResourceServer getInstance() {
            return new JettyServer(port);
        }
    }
}
