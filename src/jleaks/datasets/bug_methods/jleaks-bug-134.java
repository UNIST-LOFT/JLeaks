  public Reader createReader(final FileSystem fs, final Path path,
      CancelableProgressable reporter, boolean allowCustom)
      throws IOException {
    Class<? extends DefaultWALProvider.Reader> lrClass =
        allowCustom ? logReaderClass : ProtobufLogReader.class;

    try {
      // A wal file could be under recovery, so it may take several
      // tries to get it open. Instead of claiming it is corrupted, retry
      // to open it up to 5 minutes by default.
      long startWaiting = EnvironmentEdgeManager.currentTime();
      long openTimeout = timeoutMillis + startWaiting;
      int nbAttempt = 0;
      while (true) {
        try {
          if (lrClass != ProtobufLogReader.class) {
            // User is overriding the WAL reader, let them.
            DefaultWALProvider.Reader reader = lrClass.newInstance();
            reader.init(fs, path, conf, null);
            return reader;
          } else {
            FSDataInputStream stream = fs.open(path);
            // Note that zero-length file will fail to read PB magic, and attempt to create
            // a non-PB reader and fail the same way existing code expects it to. If we get
            // rid of the old reader entirely, we need to handle 0-size files differently from
            // merely non-PB files.
            byte[] magic = new byte[ProtobufLogReader.PB_WAL_MAGIC.length];
            boolean isPbWal = (stream.read(magic) == magic.length)
                && Arrays.equals(magic, ProtobufLogReader.PB_WAL_MAGIC);
            DefaultWALProvider.Reader reader =
                isPbWal ? new ProtobufLogReader() : new SequenceFileLogReader();
            reader.init(fs, path, conf, stream);
            return reader;
          }
        } catch (IOException e) {
          String msg = e.getMessage();
          if (msg != null && (msg.contains("Cannot obtain block length")
              || msg.contains("Could not obtain the last block")
              || msg.matches("Blocklist for [^ ]* has changed.*"))) {
            if (++nbAttempt == 1) {
              LOG.warn("Lease should have recovered. This is not expected. Will retry", e);
            }
            if (reporter != null && !reporter.progress()) {
              throw new InterruptedIOException("Operation is cancelled");
            }
            if (nbAttempt > 2 && openTimeout < EnvironmentEdgeManager.currentTime()) {
              LOG.error("Can't open after " + nbAttempt + " attempts and "
                + (EnvironmentEdgeManager.currentTime() - startWaiting)
                + "ms " + " for " + path);
            } else {
              try {
                Thread.sleep(nbAttempt < 3 ? 500 : 1000);
                continue; // retry
              } catch (InterruptedException ie) {
                InterruptedIOException iioe = new InterruptedIOException();
                iioe.initCause(ie);
                throw iioe;
              }
            }
          }
          throw e;
        }
      }
    } catch (IOException ie) {
      throw ie;
    } catch (Exception e) {
      throw new IOException("Cannot get log reader", e);
    }
  }
