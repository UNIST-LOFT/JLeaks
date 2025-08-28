  ParquetReader(
      Configuration conf,
      InputRowSchema inputRowSchema,
      InputEntity source,
      File temporaryDirectory,
      JSONPathSpec flattenSpec,
      boolean binaryAsString
  ) throws IOException
  {
    this.inputRowSchema = inputRowSchema;
    this.flattener = ObjectFlatteners.create(flattenSpec, new ParquetGroupFlattenerMaker(binaryAsString));

    closer = Closer.create();
    byte[] buffer = new byte[InputEntity.DEFAULT_FETCH_BUFFER_SIZE];
    final InputEntity.CleanableFile file = closer.register(source.fetch(temporaryDirectory, buffer));
    final Path path = new Path(file.file().toURI());

    final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      reader = closer.register(org.apache.parquet.hadoop.ParquetReader.builder(new GroupReadSupport(), path)
                                                                      .withConf(conf)
                                                                      .build());
    }
    finally {
      Thread.currentThread().setContextClassLoader(currentClassLoader);
    }
  }
