    public DataSource perform() throws TaskExecutionException {
        UpToDateChecker check = new UpToDateChecker();
        check.addInput(source.lastModified());
        File subsampleFile = getOutput();
        check.addOutput(subsampleFile);
        if (check.isUpToDate()) {
            logger.info("subsample {} up to date", getName());
            return makeDataSource();
        }
        try {
            logger.info("sampling {} of {}",
                        subsampleFraction, source.getName());
            Closer closer = Closer.create();
            RatingWriter subsampleWriter = closer.register(RatingWriters.csv(subsampleFile));
            try {
                mode.doSample(source, subsampleWriter, subsampleFraction, getProject().getRandom());
            } catch (Throwable th) { // NOSONAR using a closer
                throw closer.rethrow(th);
            } finally {
                closer.close();
            }
        } catch (IOException e) {
            throw new TaskExecutionException("Error writing output file", e);
        }
        return makeDataSource();
    }
