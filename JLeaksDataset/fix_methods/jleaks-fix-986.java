public void cacheGameProperties(final GameData gameData) 
{
    final Map<String, Object> serializableMap = new HashMap<>();
    for (final IEditableProperty property : gameData.getProperties().getEditableProperties()) {
        if (property.getValue() instanceof Serializable) {
            serializableMap.put(property.getName(), property.getValue());
        }
    }
    final File cache = getCacheFile(gameData);
    try {
        // create the directory if it doesn't already exists
        if (!cache.getParentFile().exists()) {
            cache.getParentFile().mkdirs();
        }
        try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cache))) {
            out.writeObject(serializableMap);
        }
    } catch (final IOException e) {
        ClientLogger.logQuietly(e);
    }
}