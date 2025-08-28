  public static GameData cloneGameData(final GameData data, final boolean copyDelegates) {
    try {
      ByteArrayOutputStream sink = new ByteArrayOutputStream(10000);
      GameDataManager.saveGame(sink, data, copyDelegates);
      sink.close();
      final ByteArrayInputStream source = new ByteArrayInputStream(sink.toByteArray());
      sink = null;
      return GameDataManager.loadGame(source);
    } catch (final IOException ex) {
      ClientLogger.logQuietly(ex);
      return null;
    }
  }
