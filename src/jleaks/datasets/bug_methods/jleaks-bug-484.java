    long uploadJobResources(JobConfig jobConfig) {
        long jobId = newJobId();
        Map<String, byte[]> tmpMap = new HashMap<>();
        try {
            for (ResourceConfig rc : jobConfig.getResourceConfigs()) {
                switch (rc.getResourceType()) {
                    case REGULAR_FILE:
                        InputStream in = rc.getUrl().openStream();
                        readStreamAndPutCompressedToMap(rc.getId(), tmpMap, in);
                        in.close();
                        break;
                    case JAR:
                        InputStream is = rc.getUrl().openStream();
                        loadJarFromInputStream(tmpMap, is);
                        is.close();
                        break;
                    case JARS_IN_ZIP:
                        loadJarsInZip(tmpMap, rc.getUrl());
                        break;
                    default:
                        throw new JetException("Unsupported resource type: " + rc.getResourceType());
                }
            }
        } catch (Exception e) {
            throw new JetException("Job resource upload failed", e);
        }
        // avoid creating resources map if map is empty
        if (tmpMap.size() > 0) {
            IMap<String, Object> jobResourcesMap = getJobResources(jobId).get();
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
