  public int read(ByteBuffer buffer)
      throws IOException {
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
          LOG.warn("Already attempted max of {} retries while reading '{}'; throwing exception.",
              maxRetries, StorageResourceId.createReadableString(bucketName, objectName));
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
          LOG.warn("Got exception while reading '{}'; retry # {}. Sleeping...",
              StorageResourceId.createReadableString(bucketName, objectName),
              retriesAttempted, ioe);

          try {
            boolean backOffSuccessful = BackOffUtils.next(sleeper, backOff);
            if (!backOffSuccessful) {
              LOG.warn("BackOff returned false; maximum total elapsed time exhausted. Giving up "
                      + "after {} retries for '{}'", retriesAttempted,
                      StorageResourceId.createReadableString(bucketName, objectName));
              throw ioe;
            }
          } catch (InterruptedException ie) {
            LOG.warn("Interrupted while sleeping before retry."
                + "Giving up after {} retries for '{}'", retriesAttempted,
                StorageResourceId.createReadableString(bucketName, objectName));
            ioe.addSuppressed(ie);
            throw ioe;
          }
          LOG.info("Done sleeping before retry for '{}'; retry # {}.",
              StorageResourceId.createReadableString(bucketName, objectName),
              retriesAttempted);

          if (buffer.remaining() != remainingBeforeRead) {
            int partialRead = remainingBeforeRead - buffer.remaining();
            LOG.info("Despite exception, had partial read of {} bytes; resetting retry count.",
                partialRead);
            retriesAttempted = 0;
            totalBytesRead += partialRead;
            currentPosition += partialRead;
          }

          // Force the stream to be reopened by seeking to the current position.
          long newPosition = currentPosition;
          currentPosition = -1;
          position(newPosition);
          performLazySeek();
        }
      }
    } while (buffer.remaining() > 0);

    // If this method was called when the stream was already at EOF
    // (indicated by totalBytesRead == 0) then return EOF else,
    // return the number of bytes read.
    return (totalBytesRead == 0) ? -1 : totalBytesRead;
  }
