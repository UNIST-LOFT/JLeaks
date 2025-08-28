public int read(ByteBuffer buffer){
    throwIfNotOpen();
    // Don't try to read if the buffer has no space.
    if (buffer.remaining() == 0) {
        return 0;
    }
    // Perform a lazy seek if not done already.
    performLazySeek();
    int totalBytesRead = 0;
    int retriesAttempted = 0;
    // We read from a streaming source. We may not get all the bytes we asked for
    // in the first read. Therefore, loop till we either read the required number of
    // bytes or we reach end-of-stream.
    do {
        int remainingBeforeRead = buffer.remaining();
        try {
            int numBytesRead = readChannel.read(buffer);
            Preconditions.checkState(numBytesRead != 0, "Read 0 bytes without blocking!");
            if (numBytesRead < 0) {
                break;
            }
            totalBytesRead += numBytesRead;
            currentPosition += numBytesRead;
            // The count of retriesAttempted is per low-level readChannel.read call; each time we make
            // progress we reset the retry counter.
            retriesAttempted = 0;
        } catch (IOException ioe) {
            // TODO: Refactor any reusable logic for retries into a separate RetryHelper class.
            if (retriesAttempted == maxRetries) {
                LOG.warn("Already attempted max of {} retries while reading '{}'; throwing exception.", maxRetries, StorageResourceId.createReadableString(bucketName, objectName));
                throw ioe;
            } else {
                if (retriesAttempted == 0) {
                    // If this is the first of a series of retries, we also want to reset the backOff
                    // to have fresh initial values.
                    if (backOff == null) {
                        backOff = createBackOff();
                    } else {
                        backOff.reset();
                    }
                }
                ++retriesAttempted;
                LOG.warn("Got exception while reading '{}'; retry # {}. Sleeping...", StorageResourceId.createReadableString(bucketName, objectName), retriesAttempted, ioe);
                try {
                    boolean backOffSuccessful = BackOffUtils.next(sleeper, backOff);
                    if (!backOffSuccessful) {
                        LOG.warn("BackOff returned false; maximum total elapsed time exhausted. Giving up " + "after {} retries for '{}'", retriesAttempted, StorageResourceId.createReadableString(bucketName, objectName));
                        throw ioe;
                    }
                } catch (InterruptedException ie) {
                    LOG.warn("Interrupted while sleeping before retry." + "Giving up after {} retries for '{}'", retriesAttempted, StorageResourceId.createReadableString(bucketName, objectName));
                    ioe.addSuppressed(ie);
                    throw ioe;
                }
                LOG.info("Done sleeping before retry for '{}'; retry # {}.", StorageResourceId.createReadableString(bucketName, objectName), retriesAttempted);
                if (buffer.remaining() != remainingBeforeRead) {
                    int partialRead = remainingBeforeRead - buffer.remaining();
                    LOG.info("Despite exception, had partial read of {} bytes; resetting retry count.", partialRead);
                    retriesAttempted = 0;
                    totalBytesRead += partialRead;
                    currentPosition += partialRead;
                }
                // Force the stream to be reopened by seeking to the current position.
                long newPosition = currentPosition;
                currentPosition = -1;
                position(newPosition);
                // Before performing lazy seek, explicitly close the underlying channel if necessary,
                // catching and ignoring SSLException since the retry indicates an error occurred, so
                // there's a high probability that SSL connections would be broken in a way that
                // causes close() itself to throw an exception, even though underlying sockets have
                // already been cleaned up; close() on an SSLSocketImpl requires a shutdown handshake
                // in order to shutdown cleanly, and if the connection has been broken already, then
                // this is not possible, and the SSLSocketImpl was already responsible for performing
                // local cleanup at the time the exception was raised.
                if (lazySeekPending && readChannel != null) {
                    try {
                        readChannel.close();
                        readChannel = null;
                    } catch (SSLException ssle) {
                        LOG.warn("Got SSLException on readChannel.close() before retry; ignoring it.", ssle);
                        readChannel = null;
                    }
                    // For "other" exceptions, we'll let it propagate out without setting readChannel to
                    // null, in case the caller is able to handle it and then properly try to close()
                    // again.
                }
                performLazySeek();
            }
        }
    } while (buffer.remaining() > 0);
    // If this method was called when the stream was already at EOF
    // (indicated by totalBytesRead == 0) then return EOF else,
    // return the number of bytes read.
    return (totalBytesRead == 0) ? -1 : totalBytesRead;
}