public static GameData cloneGameData(final GameData data, final boolean copyDelegates) 
{
    try {
        try (ByteArrayOutputStream sink = new ByteArrayOutputStream(10000)) {
            GameDataManager.saveGame(sink, data, copyDelegates);
            final ByteArrayInputStream source = new ByteArrayInputStream(sink.toByteArray());
            return GameDataManager.loadGame(source);
        }
    } catch (final IOException ex) {
        ClientLogger.logQuietly(ex);
        return null;
    }
}