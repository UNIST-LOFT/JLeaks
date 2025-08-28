public static void compact(String sourceFileName, String targetFileName, boolean compress) 
{
    MVMap<String, String> sourceMeta = source.getMetaMap();
    MVMap<String, String> targetMeta = target.getMetaMap();
    for (Entry<String, String> m : sourceMeta.entrySet()) {
        String key = m.getKey();
        if (key.startsWith("chunk.")) {
            // ignore
        } else if (key.startsWith("map.")) {
            // ignore
        } else if (key.startsWith("name.")) {
            // ignore
        } else if (key.startsWith("root.")) {
            // ignore
        } else {
            targetMeta.put(key, m.getValue());
        }
    }
    for (String mapName : source.getMapNames()) {
        MVMap.Builder<Object, Object> mp = new MVMap.Builder<>().keyType(new GenericDataType()).valueType(new GenericDataType());
        MVMap<Object, Object> sourceMap = source.openMap(mapName, mp);
        MVMap<Object, Object> targetMap = target.openMap(mapName, mp);
        targetMap.copyFrom(sourceMap);
    }
}