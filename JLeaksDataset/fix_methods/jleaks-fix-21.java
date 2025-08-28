public SerializableVoid runTask(CompactConfig config, ArrayList<CompactTask> tasks,
      RunTaskContext context) throws Exception {
    for (CompactTask task : tasks) {
      ArrayList<String> inputs = task.getInputs();
      if (inputs.isEmpty()) {
        continue;
      }
      AlluxioURI output = new AlluxioURI(task.getOutput());

      TableSchema schema;
      try (TableReader reader = TableReader.create(new AlluxioURI(inputs.get(0)),
          config.getPartitionInfo())) {
        schema = reader.getSchema();
      }

      try (TableWriter writer = TableWriter.create(schema, output)) {
        for (String input : inputs) {
          try (TableReader reader = TableReader.create(new AlluxioURI(input),
              config.getPartitionInfo())) {
            for (TableRow row = reader.read(); row != null; row = reader.read()) {
              writer.write(row);
            }
          }
        }
      } catch (Throwable e) {
        try {
          context.getFileSystem().delete(output); // outputUri is the output file
        } catch (Throwable t) {
          e.addSuppressed(t);
        }
        throw e;
      }
    }
    return null;
  }