private void loadOrCreateBloomFilter() throws IOException 
{
    Path filterFile = new Path(filterDir, BLOOMFILTER_FILE_NAME);
    if (fs.exists(filterFile)) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loading bloom filter for " + this.storeName);
        }
        BloomFilterDescriptor.BloomFilterType type = family.getBloomFilter().filterType;
        switch(type) {
            case BLOOMFILTER:
                bloomFilter = new BloomFilter();
                break;
            case COUNTING_BLOOMFILTER:
                bloomFilter = new CountingBloomFilter();
                break;
            case RETOUCHED_BLOOMFILTER:
                bloomFilter = new RetouchedBloomFilter();
        }
        FSDataInputStream in = fs.open(filterFile);
        try {
            bloomFilter.readFields(in);
        } finally {
            fs.close();
        }
    } else {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating bloom filter for " + this.storeName);
        }
        BloomFilterDescriptor.BloomFilterType type = family.getBloomFilter().filterType;
        switch(type) {
            case BLOOMFILTER:
                bloomFilter = new BloomFilter(family.getBloomFilter().vectorSize, family.getBloomFilter().nbHash);
                break;
            case COUNTING_BLOOMFILTER:
                bloomFilter = new CountingBloomFilter(family.getBloomFilter().vectorSize, family.getBloomFilter().nbHash);
                break;
            case RETOUCHED_BLOOMFILTER:
                bloomFilter = new RetouchedBloomFilter(family.getBloomFilter().vectorSize, family.getBloomFilter().nbHash);
        }
    }
}