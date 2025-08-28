    private void readStreamAndPutCompressedToMap(InputStream in, String resourceId) throws IOException {
        String key = JetClassLoader.METADATA_RESOURCES_PREFIX + resourceId;
        // ignore duplicates: the first resource in first jar takes precedence
        if (tmpMap.containsKey(key)) {
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream compressor = new DeflaterOutputStream(baos);
        IOUtil.drainTo(in, compressor);
        compressor.close();
        tmpMap.put(key, baos.toByteArray());
    }
