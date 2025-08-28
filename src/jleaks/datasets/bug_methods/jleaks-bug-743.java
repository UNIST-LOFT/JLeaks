    public void onStop() throws StopException {
        StopException firstCause = null;

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
            if (firstCause == null) {
                firstCause = new StopException("Thread join operation interrupted (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            }
        }

        // Attempt to cancel any remaining tasks
        cleanup(true, true, false);

        if (firstCause != null) {
            throw firstCause;
        }
    }
