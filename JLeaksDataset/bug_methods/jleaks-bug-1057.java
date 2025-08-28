    public Distribution(StorDistributionConfig config) {
        int mask = 0;
        for (int i=0; i<=64; ++i) {
            distributionBitMasks[i] = mask;
            mask = (mask << 1) | 1;
        }
        configSubscriber.configure(config);
    }
