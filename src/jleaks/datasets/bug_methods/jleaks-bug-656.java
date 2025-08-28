  public LineNumbers(Class type) throws IOException {
    this.type = type;

    if (!type.isArray()) {
      InputStream in = type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class");
      if (in != null) {
        new ClassReader(in).accept(new LineNumberReader(), ClassReader.SKIP_FRAMES);
      }
    }
  }
