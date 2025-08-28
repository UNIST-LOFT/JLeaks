  private void loadFromInputStream(InputStream serialObjectInputStream)
      throws IOException, ClassNotFoundException {
    ObjectInputStream input = new ObjectInputStream(serialObjectInputStream);
    bigramHashTable = (long[]) input.readObject();
    frequencyTable = (int[]) input.readObject();
    // log.info("load bigram dict from serialization.");
    input.close();
  }

  private void saveToObj(Path serialObj) {
    try {
      ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(
          serialObj));
      output.writeObject(bigramHashTable);
      output.writeObject(frequencyTable);
      output.close();
      // log.info("serialize bigram dict.");
    } catch (Exception e) {
      // log.warn(e.getMessage());
    }
  }
