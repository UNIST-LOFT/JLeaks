public Map<TypeDescription, Class<?>> load(ClassLoader classLoader, Map<TypeDescription, byte[]> types) 
{
    if (classLoader == null) {
        throw new IllegalArgumentException("Cannot inject classes into the bootstrap class loader on Android");
    }
    return super.load(classLoader, types);
}