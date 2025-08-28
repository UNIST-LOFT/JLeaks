  public void close() throws IOException {
    writeSerializationMappings(stateFile, serializer.getSerializationMappings());
  }
