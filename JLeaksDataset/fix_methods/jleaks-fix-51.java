  public void func() {
    try {
      InputStream in = Json.class.getResourceAsStream("/org/apache/avro/data/Json.avsc");
      try {
        SCHEMA = Schema.parse(in);
      } finally {
        in.close();
      }
    } catch (IOException e) {
      throw new AvroRuntimeException(e);
    }
  }