private void initBloomFilter(Path dirName, 
                                 Configuration conf) {

      DataInputStream in = null;
      try {
        FileSystem fs = dirName.getFileSystem(conf);
        in = fs.open(new Path(dirName, BLOOM_FILE_NAME));
        bloomFilter = new DynamicBloomFilter();
        bloomFilter.readFields(in);
        in.close();
        in = null;
      } catch (IOException ioe) {
        LOG.warn("Can't open BloomFilter: " + ioe + " - fallback to MapFile.");
        bloomFilter = null;
      } finally {
        IOUtils.closeStream(in);
      }
    }