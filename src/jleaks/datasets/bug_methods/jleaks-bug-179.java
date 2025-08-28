  private void saveToObj(Path serialObj) {
    try {
      ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(
          serialObj));
      output.writeObject(wordIndexTable);
      output.writeObject(charIndexTable);
      output.writeObject(wordItem_charArrayTable);
      output.writeObject(wordItem_frequencyTable);
      output.close();
      // log.info("serialize core dict.");
    } catch (Exception e) {
      // log.warn(e.getMessage());
    }
  }
