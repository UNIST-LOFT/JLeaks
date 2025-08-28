public void onStop() throws StopException 
{
    StopException firstCause = null;
    synchronized (clientReaders) {
        disposing.set(true);
        // Prevent any new client threads from being submitted to the executor
        executor.shutdown();
    }
    if (serverSocket != null) {
        // Close the server socket
        try {
            logger.debug("Closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
            serverSocket.close();
        } catch (IOException e) {
            firstCause = new StopException("Error closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
        }
    }
    // Join the connector thread
    try {
        disposeThread(false);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new StopException("Thread join operation interrupted (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
    }
    synchronized (clientReaders) {
        for (TcpReader reader : clientReaders) {
            try {
                synchronized (reader) {
                    reader.setCanRead(false);
                    /*
                         * We only want to close the worker's socket if it's currently in the read()
                         * method. If keep connection open is true and the receive timeout is zero,
                         * that read() would have blocked forever, so we need to close the socket
                         * here so it will throw an exception. However even if the worker was in the
                         * middle of reading bytes from the input stream, we still want to close the
                         * socket. That message would never have been dispatched to the channel
                         * anyway because the connectors current state would not be equal to
                         * STARTED.
                         */
                    if (reader.isReading()) {
                        reader.getSocket().close();
                    }
                }
            } catch (IOException e) {
                if (firstCause == null) {
                    firstCause = new StopException("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                }
            }
        }
        clientReaders.clear();
    }
    // Wait for any remaining tasks to complete
    try {
        cleanup(true, false, false);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new StopException("Client thread disposal interrupted (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
    }
    // Close all client sockets after canceling tasks in case a task failed to complete
    synchronized (clientReaders) {
        for (TcpReader reader : clientReaders) {
            try {
                reader.getSocket().close();
            } catch (IOException e) {
                if (firstCause == null) {
                    firstCause = new StopException("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                }
            }
        }
        clientReaders.clear();
    }
    if (firstCause != null) {
        throw firstCause;
    }
}