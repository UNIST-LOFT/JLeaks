    public void run() {
        try {
        	sink.initialize(Collections.<String, Object>emptyMap());
        	
            (new BlockInputStream(input, parser)).process();
            
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to process PBF stream", e);
        } finally {
        	sink.close();
        }
    }
