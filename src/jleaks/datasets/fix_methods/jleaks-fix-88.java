protected CloseableIterator<Group> intermediateRowIterator() throws IOException
{
  final Closer closer = Closer.create();
  byte[] buffer = new byte[InputEntity.DEFAULT_FETCH_BUFFER_SIZE];
  final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
  final org.apache.parquet.hadoop.ParquetReader<Group> reader;
  try {
    final CleanableFile file = closer.register(source.fetch(temporaryDirectory, buffer));
    final Path path = new Path(file.file().toURI());

    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    reader = closer.register(org.apache.parquet.hadoop.ParquetReader.builder(new GroupReadSupport(), path)
                                                                    .withConf(conf)
                                                                    .build());
  }
  catch (Exception e) {
    // We don't expect to see any exceptions thrown in the above try clause,
    // but we catch it just in case to avoid any potential resource leak.
    closer.close();
    throw new RuntimeException(e);
  }
  finally {
    Thread.currentThread().setContextClassLoader(currentClassLoader);
  }
  return new CloseableIterator<Group>()
    {
      Group value = null;
      @Override
      public boolean hasNext()
      {
        if (value == null) {
          try {
            value = reader.read();
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        return value != null;
      }
      @Override
      public Group next()
      {
        if (value == null) {
          throw new NoSuchElementException();
        }
        Group currentValue = value;
        value = null;
        return currentValue;
      }
      @Override
      public void close() throws IOException
      {
        closer.close();
      }
    };
  }