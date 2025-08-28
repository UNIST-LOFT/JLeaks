  public static <T> T translateIntoOtherGameData(final T object, final GameData translateInto) {
    try {
      ByteArrayOutputStream sink = new ByteArrayOutputStream(1024);
      final GameObjectOutputStream out = new GameObjectOutputStream(sink);
      out.writeObject(object);
      out.flush();
      out.close();
      final ByteArrayInputStream source = new ByteArrayInputStream(sink.toByteArray());
      sink = null;
      final GameObjectStreamFactory factory = new GameObjectStreamFactory(translateInto);
      final ObjectInputStream in = factory.create(source);
      try {
        return (T) in.readObject();
      } catch (final ClassNotFoundException ex) {
        // should never happen
        throw new RuntimeException(ex);
      }
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
