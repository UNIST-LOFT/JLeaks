    public void close() throws IOException {
      LOG.info("Closing reader after reading {} records.", recordsReturned);
      if (reader != null) {
        reader = null;
      }
      if (serviceEntry != null) {
        serviceEntry.close();
        serviceEntry = null;
      }
    }
