
  public static <T> T readObjectFromURLOrClasspathOrFileSystem(String filename) throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(getInputStreamFromURLOrClasspathOrFileSystem(filename))) {
      Object o = ois.readObject();
      return ErasureUtils.uncheckedCast(o);
    }
  }