public void func() {
    try {
      SCHEMA = Schema.parse
        (Json.class.getResourceAsStream("/org/apache/avro/data/Json.avsc"));
    } catch (IOException e) {
      throw new AvroRuntimeException(e);
    }
  }
