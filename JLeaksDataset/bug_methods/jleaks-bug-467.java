  public void encode(StreamingContent content, OutputStream out) throws IOException {
    GZIPOutputStream zipper = new GZIPOutputStream(out);
    content.writeTo(zipper);
    zipper.finish();
  }
