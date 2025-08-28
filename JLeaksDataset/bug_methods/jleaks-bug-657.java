  public static LottieResult<LottieComposition> fromJsonReaderSync(JsonReader reader, @Nullable String cacheKey) {
    try {
      LottieComposition composition = LottieCompositionParser.parse(reader);
      LottieCompositionCache.getInstance().put(cacheKey, composition);
      return new LottieResult<>(composition);
    } catch (Exception e) {
      return new LottieResult<>(e);
    }
  }
