    public void close(TaskAttemptContext context)
    throws IOException {
      mutator.close();
      connection.close();
    }
