  public static void main(String[] args) throws Exception {
    if (args.length < 3 || args.length > 4) {
      System.out.println("Usage: RandomData <schemafile> <outputfile> <count> [codec]");
      System.exit(-1);
    }
    Schema sch = new Schema.Parser().parse(new File(args[0]));
    DataFileWriter<Object> writer = new DataFileWriter<>(new GenericDatumWriter<>());
    writer.setCodec(CodecFactory.fromString(args.length >= 4 ? args[3] : "null"));
    writer.create(sch, new File(args[1]));
    try {
      for (Object datum : new RandomData(sch, Integer.parseInt(args[2]))) {
        writer.append(datum);
      }
    } finally {
      writer.close();
    }
  }
