public static <T> T readObjectFromFile(File file) throws IOException,
          ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
            new GZIPInputStream(new FileInputStream(file))))) {
      Object o = ois.readObject();
      return ErasureUtils.uncheckedCast(o);
    } catch (java.util.zip.ZipException e) {
      try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
              new FileInputStream(file)))) {
        Object o = ois.readObject();
        return ErasureUtils.uncheckedCast(o);
      }
    }
  }
