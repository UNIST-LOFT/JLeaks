  private static RecordReader<NullWritable, ArrayWritable> constructRecordReader(RealtimeSplit split,
      JobConf jobConf, RecordReader<NullWritable, ArrayWritable> realReader) {
    try {
      if (canSkipMerging(jobConf)) {
        LOG.info("Enabling un-merged reading of realtime records");
        return new RealtimeUnmergedRecordReader(split, jobConf, realReader);
      }
      LOG.info("Enabling merged reading of realtime records for split " + split);
      return new RealtimeCompactedRecordReader(split, jobConf, realReader);
    } catch (IOException ex) {
      LOG.error("Got exception when constructing record reader", ex);
      throw new HoodieException(ex);
    }
  }
