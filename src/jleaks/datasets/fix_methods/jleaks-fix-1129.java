protected void init(final ProcessorInitializationContext context) {
        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
        final InputStream resourceStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("file.txt");
        try {
            this.resourceData = IOUtils.toString(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load resources", e);
        } finally {
            IOUtils.closeQuietly(resourceStream);
        }

    }