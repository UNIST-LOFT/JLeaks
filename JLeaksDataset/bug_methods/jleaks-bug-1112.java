  public static int  writeCompressedByteArray(DataOutput out, 
                                              byte[] bytes) throws IOException {
    if (bytes != null) {
      ByteArrayOutputStream bos =  new ByteArrayOutputStream();
      GZIPOutputStream gzout = new GZIPOutputStream(bos);
      gzout.write(bytes, 0, bytes.length);
      gzout.close();
      byte[] buffer = bos.toByteArray();
      int len = buffer.length;
      out.writeInt(len);
      out.write(buffer, 0, len);
      /* debug only! Once we have confidence, can lose this. */
      return ((bytes.length != 0) ? (100*buffer.length)/bytes.length : 0);
    } else {
      out.writeInt(-1);
      return -1;
    }
  }
