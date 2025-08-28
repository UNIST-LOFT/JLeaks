public DataSource perform() throws TaskExecutionException 
{
    UpToDateChecker check = new UpToDateChecker();
    check.addInput(source.lastModified());
    File subsampleFile = getOutput();
    check.addOutput(subsampleFile);
    if (check.isUpToDate()) {
        logger.info("subsample {} up to date", getName());
        return makeDataSource();
    }
    try (RatingWriter subsampleWriter = RatingWriters.csv(subsampleFile)) {
        logger.info("sampling {} of {}", subsampleFraction, source.getName());
        mode.doSample(source, subsampleWriter, subsampleFraction, getProject().getRandom());
    } catch (IOException e) {
        throw new TaskExecutionException("Error writing output file", e);
    }
    return makeDataSource();
}