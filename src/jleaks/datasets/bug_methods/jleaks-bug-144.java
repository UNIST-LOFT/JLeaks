  public void syncTableMetadata() {
    // Open up the metadata table again, for syncing
    SparkHoodieBackedTableMetadataWriter.create(hadoopConf, config, context);
  }
