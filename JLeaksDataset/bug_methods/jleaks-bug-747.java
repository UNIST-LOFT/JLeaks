    public void testConnection() throws NetworkIOException {
        ApnsConnectionImpl testConnection = new ApnsConnectionImpl(factory, host, port, reconnectPolicy.copy(), ApnsDelegate.EMPTY);
        testConnection.sendMessage(new SimpleApnsNotification(new byte[] {0}, new byte[]{0}));
        testConnection.close();
    }
