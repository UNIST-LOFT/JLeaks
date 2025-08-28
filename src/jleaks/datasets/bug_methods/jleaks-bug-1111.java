    private void initBloomFilter(Path dirName, 
                                 Configuration conf) {
      try {
        FileSystem fs = dirName.getFileSystem(conf);
        DataInputStream in = fs.open(new Path(dirName, BLOOM_FILE_NAME));
        bloomFilter = new DynamicBloomFilter();
        bloomFilter.readFields(in);
        in.close();
      } catch (IOException ioe) {
        LOG.warn("Can't open BloomFilter: " + ioe + " - fallback to MapFile.");
        bloomFilter = null;
      }
    }
