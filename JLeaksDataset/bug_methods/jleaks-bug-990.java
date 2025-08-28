  private static void write(final StringBuilder buf, final OutputStream sink) throws IOException {
    final OutputStreamWriter out = new OutputStreamWriter(sink);
    out.write(buf.toString());
    out.flush();
  }
