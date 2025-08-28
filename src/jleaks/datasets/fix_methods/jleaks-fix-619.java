    protected final void implAccept(Socket s) throws IOException {
        SocketImpl si = s.impl();

        // Socket has no SocketImpl
        if (si == null) {
            si = implAccept();
            try {
                s.setConnectedImpl(si);
            } catch (SocketException e) {
                // s has been closed so newly accepted connection needs to be closed
                si.closeQuietly();
                throw e;
            }
            return;
        }

        // Socket has a SOCKS or HTTP SocketImpl, need delegate
        if (si instanceof DelegatingSocketImpl dsi) {
            si = dsi.delegate();
            assert si instanceof PlatformSocketImpl;
        }

        // Accept connection with a platform or custom SocketImpl.
        // For the platform SocketImpl case:
        // - the connection is accepted with a new SocketImpl
        // - the SO_TIMEOUT socket option is copied to the new SocketImpl
        // - the Socket is connected to the new SocketImpl
        // - the existing/old SocketImpl is closed
        // For the custom SocketImpl case, the connection is accepted with the
        // existing custom SocketImpl.
        ensureCompatible(si);
        if (impl instanceof PlatformSocketImpl) {
            SocketImpl psi = platformImplAccept();
            si.copyOptionsTo(psi);
            try {
                s.setConnectedImpl(psi);
            } catch (SocketException e) {
                // s has been closed so newly accepted connection needs to be closed
                psi.closeQuietly();
                throw e;
            }
        } else {
            s.setImpl(null);    // temporarily break connection to impl
            try {
                customImplAccept(si);
            } finally {
                s.setImpl(si);  // restore connection to impl
            }
            s.setConnected();
        }
    }