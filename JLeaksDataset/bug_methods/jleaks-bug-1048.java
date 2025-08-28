    public synchronized T getConfig(String configId) {
        ConfigSubscriber subscriber;
        ConfigHandle<T> h;
        if (source == null) {
            subscriber = new ConfigSubscriber();
        } else {
            subscriber = new ConfigSubscriber(source);
        }
        h = subscriber.subscribe(clazz, configId);
        subscriber.nextConfig();
        T ret = h.getConfig();
        subscriber.close();
        return ret;
    }
