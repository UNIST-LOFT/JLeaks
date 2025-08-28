    public static int analyze(Analyzer analyzer, CharsRef toAnalyze, String field, TokenConsumer consumer) throws IOException {
        TokenStream ts = analyzer.tokenStream(
                field, new FastCharArrayReader(toAnalyze.chars, toAnalyze.offset, toAnalyze.length)
        );
        return analyze(ts, consumer);
    }
