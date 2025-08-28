
    public Distribution(String configId) {
        int mask = 0;
        for (int i=0; i<=64; ++i) {
            distributionBitMasks[i] = mask;
            mask = (mask << 1) | 1;
        }
        try {
            configSub = new ConfigSubscriber();
            configSub.subscribe(configSubscriber, StorDistributionConfig.class, configId);
        } catch (Throwable e) {
            close();
            throw e;
        }
    }