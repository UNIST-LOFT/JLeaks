    @Override public synchronized void close() {
        closed = true;

        for (ClientChannelHolder hld : channels)
            hld.closeChannel();
    }
