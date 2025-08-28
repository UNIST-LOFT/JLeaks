  public WALByteBufReader(File logFile, FileChannel channel) throws IOException {
    this.logFile = logFile;
    this.channel = channel;
    this.logStream = new DataInputStream(new WALInputStream(logFile));
    this.metaData = WALMetaData.readFromWALFile(logFile, channel);
    this.sizeIterator = metaData.getBuffersSize().iterator();
    channel.position(0);
  }
