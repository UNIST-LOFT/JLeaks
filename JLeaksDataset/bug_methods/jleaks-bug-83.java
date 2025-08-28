  private void flush() throws IOException {
    if (recordCount > 0) {
      parquetFileWriter.startBlock(recordCount);
      consumer.flush();
      store.flush();
      ColumnChunkPageWriteStoreExposer.flushPageStore(pageStore, parquetFileWriter);
      recordCount = 0;
      parquetFileWriter.endBlock();

      // we are writing one single block per file
      parquetFileWriter.end(extraMetaData);
      parquetFileWriter = null;
    }

    store.close();
    // TODO(jaltekruse) - review this close method should no longer be necessary
//    ColumnChunkPageWriteStoreExposer.close(pageStore);

    store = null;
    pageStore = null;
    index++;
  }
