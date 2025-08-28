  public WALByteBufReader(WALEntryPosition walEntryPosition) throws IOException {
    WALInputStream walInputStream = walEntryPosition.openReadFileStream();
    try {
      this.logStream = new DataInputStream(walInputStream);
      this.metaData = walInputStream.getWALMetaData();
      this.sizeIterator = metaData.getBuffersSize().iterator();
    } catch (Exception e) {
      walInputStream.close();
      throw e;
    }
  }
