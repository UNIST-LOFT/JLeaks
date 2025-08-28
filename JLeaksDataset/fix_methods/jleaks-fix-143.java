private static RecordReader<NullWritable, ArrayWritable> constructRecordReader(RealtimeSplit split,
JobConf jobConf, RecordReader<NullWritable, ArrayWritable> realReader) {
    try {
        if (canSkipMerging(jobConf)) {
            LOG.info("Enabling un-merged reading of realtime records");
            return new RealtimeUnmergedRecordReader(split, jobConf, realReader);
        }
        LOG.info("Enabling merged reading of realtime records for split " + split);
        return new RealtimeCompactedRecordReader(split, jobConf, realReader);
    } catch (Exception e) {
        LOG.error("Got exception when constructing record reader", e);
        try {
            if (null != realReader) {
                realReader.close();
            }
        } catch (IOException ioe) {
            LOG.error("Unable to close real reader", ioe);
        }
        throw new HoodieException("Exception when constructing record reader ", e);
    }
}