long uploadJobResources(JobConfig jobConfig) 
{
    long jobId = newJobId();
    Map<String, byte[]> tmpMap = new HashMap<>();
    try {
        Supplier<IMap<String, byte[]>> jobFileStorage = Util.memoize(() -> getJobResources(jobId));
        for (ResourceConfig rc : jobConfig.getResourceConfigs().values()) {
            switch(rc.getResourceType()) {
                case CLASSPATH_RESOURCE:
                case CLASS:
                    try (InputStream in = rc.getUrl().openStream()) {
                        readStreamAndPutCompressedToMap(rc.getId(), tmpMap, in);
                    }
                    break;
                case FILE:
                    try (InputStream in = rc.getUrl().openStream();
                        IMapOutputStream os = new IMapOutputStream(jobFileStorage.get(), fileKeyName(rc.getId()))) {
                        packStreamIntoZip(in, os, rc.getId());
                    }
                    break;
                case DIRECTORY:
                    Path baseDir = validateAndGetDirectoryPath(rc);
                    try (IMapOutputStream os = new IMapOutputStream(jobFileStorage.get(), fileKeyName(rc.getId()))) {
                        packDirectoryIntoZip(baseDir, os);
                    }
                    break;
                case JAR:
                    loadJar(tmpMap, rc);
                    break;
                case JARS_IN_ZIP:
                    loadJarsInZip(tmpMap, rc.getUrl());
                    break;
                default:
                    throw new JetException("Unsupported resource type: " + rc.getResourceType());
            }
        }
    } catch (IOException | URISyntaxException e) {
        throw new JetException("Job resource upload failed", e);
    }
    // avoid creating resources map if map is empty
    if (tmpMap.size() > 0) {
        IMap<String, byte[]> jobResourcesMap = getJobResources(jobId);
        // now upload it all
        try {
            jobResourcesMap.putAll(tmpMap);
        } catch (Exception e) {
            jobResourcesMap.destroy();
            throw new JetException("Job resource upload failed", e);
        }
    }
    return jobId;
}