  private static void write(final StringBuilder buf, final OutputStream sink) throws IOException {
    try (Writer out = new OutputStreamWriter(new CloseShieldOutputStream(sink))) {
      out.write(buf.toString());
    }
  }
