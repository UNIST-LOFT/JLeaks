public SerializableVoid runTask(CompactConfig config, ArrayList<CompactTask> tasks,
      RunTaskContext context) throws Exception {
    Closer closer = Closer.create();
    boolean closed = false;
    Compactor compactor = new SequentialCompactor();
    for (CompactTask task : tasks) {
      ArrayList<String> inputs = task.getInputs();
      if (inputs.isEmpty()) {
        continue;
      }
      AlluxioURI output = new AlluxioURI(task.getOutput());
      List<TableReader> readers = Lists.newArrayList();
      TableWriter writer = null;
      try {
        for (String input : inputs) {
          readers.add(closer.register(TableReader.create(new AlluxioURI(input),
              config.getPartitionInfo())));
        }
        TableSchema schema = readers.get(0).getSchema();
        writer = closer.register(TableWriter.create(schema, output));
        compactor.compact(readers, writer);
      } catch (Throwable t) {
        closer.close();
        closed = true;
        try {
          context.getFileSystem().delete(output); // output is the compacted file
        } catch (Throwable e) {
          t.addSuppressed(e);
        }
        closer.rethrow(t);
      } finally {
        if (!closed) {
          closer.close();
        }
      }
    }
    return null;
  }