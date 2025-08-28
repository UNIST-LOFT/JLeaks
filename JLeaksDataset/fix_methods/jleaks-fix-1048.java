public synchronized T getConfig(String configId) 
{
    ConfigGetter<T> getter = new ConfigGetter<>(source, c);
    return getter.getConfig(configId);
}