  public void close() throws IOException {

    IOException ioe = null;
    // close checksum file
    try {
      if (checksumOut != null) {
        checksumOut.flush();
        if (datanode.syncOnClose && (cout instanceof FileOutputStream)) {
          ((FileOutputStream)cout).getChannel().force(true);
        }
        checksumOut.close();
        checksumOut = null;
      }
    } catch(IOException e) {
      ioe = e;
    }
    // close block file
    try {
      if (out != null) {
        out.flush();
        if (datanode.syncOnClose && (out instanceof FileOutputStream)) {
          ((FileOutputStream)out).getChannel().force(true);
        }
        out.close();
        out = null;
      }
    } catch (IOException e) {
      ioe = e;
    }
    // disk check
    if(ioe != null) {
      datanode.checkDiskError(ioe);
      throw ioe;
    }
  }
