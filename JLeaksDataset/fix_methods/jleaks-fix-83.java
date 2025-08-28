private void flush() throws IOException 
{
    try {
        if (recordCount > 0) {
            parquetFileWriter.startBlock(recordCount);
            consumer.flush();
            store.flush();
            pageStore.flushToFileWriter(parquetFileWriter);
            recordCount = 0;
            parquetFileWriter.endBlock();
            // we are writing one single block per file
            parquetFileWriter.end(extraMetaData);
            parquetFileWriter = null;
        }
    } finally {
        store.close();
        pageStore.close();
        store = null;
        pageStore = null;
        index++;
    }
}