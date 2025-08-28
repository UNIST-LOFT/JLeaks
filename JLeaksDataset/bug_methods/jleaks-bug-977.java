  public void close() throws IOException {
    sslEngine.closeOutbound();
    sslEngine.getSession().invalidate();
    if (socketChannel.isOpen()) {
      socketChannel.write(wrap(emptybuffer));// FIXME what if not all bytes can be written
    }
    socketChannel.close();
  }
