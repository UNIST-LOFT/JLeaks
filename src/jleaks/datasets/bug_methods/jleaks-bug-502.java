  private void startFileReporter() {
    fileReporterExecutor.scheduleAtFixedRate(() -> {
      try {
        File metricsFile = new File(uploaderConfiguration.getMetricsFilePath());
        Writer metricsFileWriter = new FileWriter(metricsFile, false);

        metricsFileWriter.write(mapper.writeValueAsString(registry.getMetrics()));
        metricsFileWriter.flush();
        metricsFileWriter.close();
      } catch (IOException e) {
        LOG.error("Unable to write metrics to file", e);
      }
    }, 10, 30, TimeUnit.SECONDS);
  }
