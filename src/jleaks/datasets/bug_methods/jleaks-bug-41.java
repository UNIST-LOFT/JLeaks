  private long readBlockToCache(long position, long length) throws IOException {
    boolean isLastBlock = position + length == endPosition || length == C.LENGTH_UNSET;
    try {
      long resolvedLength = C.LENGTH_UNSET;
      boolean isDataSourceOpen = false;
      if (length != C.LENGTH_UNSET) {
        // If the length is specified, try to open the data source with a bounded request to avoid
        // the underlying network stack requesting more data than required.
        try {
          DataSpec boundedDataSpec =
              dataSpec.buildUpon().setPosition(position).setLength(length).build();
          resolvedLength = dataSource.open(boundedDataSpec);
          isDataSourceOpen = true;
        } catch (IOException exception) {
          if (allowShortContent
              && isLastBlock
              && DataSourceException.isCausedByPositionOutOfRange(exception)) {
            // The length of the request exceeds the length of the content. If we allow shorter
            // content and are reading the last block, fall through and try again with an unbounded
            // request to read up to the end of the content.
            Util.closeQuietly(dataSource);
          } else {
            throw exception;
          }
        }
      }
      if (!isDataSourceOpen) {
        // Either the length was unspecified, or we allow short content and our attempt to open the
        // DataSource with the specified length failed.
        throwIfCanceled();
        DataSpec unboundedDataSpec =
            dataSpec.buildUpon().setPosition(position).setLength(C.LENGTH_UNSET).build();
        resolvedLength = dataSource.open(unboundedDataSpec);
      }
      if (isLastBlock && resolvedLength != C.LENGTH_UNSET) {
        onRequestEndPosition(position + resolvedLength);
      }
      int totalBytesRead = 0;
      int bytesRead = 0;
      while (bytesRead != C.RESULT_END_OF_INPUT) {
        throwIfCanceled();
        bytesRead = dataSource.read(temporaryBuffer, /* offset= */ 0, temporaryBuffer.length);
        if (bytesRead != C.RESULT_END_OF_INPUT) {
          onNewBytesCached(bytesRead);
          totalBytesRead += bytesRead;
        }
      }
      if (isLastBlock) {
        onRequestEndPosition(position + totalBytesRead);
      }
      return totalBytesRead;
    } finally {
      Util.closeQuietly(dataSource);
    }
  }
