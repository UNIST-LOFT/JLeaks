private void startFileReporter() 
{
    fileReporterExecutor.scheduleAtFixedRate(() -> {
        File metricsFile = new File(downloaderConfiguration.getMetricsFilePath());
        try (Writer metricsFileWriter = new FileWriter(metricsFile, false)) {
            metricsFileWriter.write(mapper.writeValueAsString(registry.getMetrics()));
            metricsFileWriter.flush();
        } catch (IOException e) {
            LOG.error("Unable to write metrics to file", e);
        }
    }, 10, 30, TimeUnit.SECONDS);
}