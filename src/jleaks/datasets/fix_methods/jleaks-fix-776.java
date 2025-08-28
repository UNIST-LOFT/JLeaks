    public void run() {
        try {
        	sink.initialize(Collections.<String, Object>emptyMap());

            try (BlockInputStream blockInputStream = new BlockInputStream(new FileInputStream(pbfFile), parser)) {
                blockInputStream.process();
            }
            
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to process PBF stream", e);
        } finally {
        	sink.close();
        }
    }
