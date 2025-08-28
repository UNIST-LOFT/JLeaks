public void testConnection() throws NetworkIOException 
{
    ApnsConnectionImpl testConnection = null;
    try {
        testConnection = new ApnsConnectionImpl(factory, host, port, reconnectPolicy.copy(), ApnsDelegate.EMPTY);
        testConnection.sendMessage(new SimpleApnsNotification(new byte[] { 0 }, new byte[] { 0 }));
    } finally {
        if (testConnection != null)
            testConnection.close();
    }
}