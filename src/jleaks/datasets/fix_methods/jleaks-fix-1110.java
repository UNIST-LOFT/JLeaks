public InputStream serialize() throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    try {
      out.writeByte(fileType.ordinal());
      if (isFile()) {
        out.writeInt(blocks.length);
        for (int i = 0; i < blocks.length; i++) {
          out.writeLong(blocks[i].getId());
          out.writeLong(blocks[i].getLength());
        }
      }
      out.close();
      out = null;
    } finally {
      IOUtils.closeStream(out);
    }
  }