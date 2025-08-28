public static int findAvailableSocketPort() throws IOException 
{
    final ServerSocket serverSocket = new ServerSocket(0);
    try {
        int port = serverSocket.getLocalPort();
        // workaround for linux : calling close() immediately after opening socket
        // may result that socket is not closed
        synchronized (serverSocket) {
            try {
                serverSocket.wait(1);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
        return port;
    } finally {
        serverSocket.close();
    }
}