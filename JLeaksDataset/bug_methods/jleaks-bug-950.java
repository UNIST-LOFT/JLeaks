  public static <T> T readObjectFromURLOrClasspathOrFileSystem(String filename) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(getInputStreamFromURLOrClasspathOrFileSystem(filename));
    Object o = ois.readObject();
    ois.close();
    return ErasureUtils.uncheckedCast(o);
  }
