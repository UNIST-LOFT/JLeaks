  public static LottieResult<LottieComposition> fromJsonReaderSync(JsonReader reader, @Nullable String cacheKey) {
    return fromJsonReaderSyncInternal(reader, cacheKey, true);
  }

  private static LottieResult<LottieComposition> fromJsonReaderSyncInternal(
          JsonReader reader, @Nullable String cacheKey, boolean close) {
    try {
      LottieComposition composition = LottieCompositionParser.parse(reader);
      LottieCompositionCache.getInstance().put(cacheKey, composition);
      return new LottieResult<>(composition);
    } catch (Exception e) {
      return new LottieResult<>(e);
    } finally {
      if (close) {
        closeQuietly(reader);
      }
    }
  }
