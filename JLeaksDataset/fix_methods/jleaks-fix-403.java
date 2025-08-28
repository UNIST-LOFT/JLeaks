public static int analyze(Analyzer analyzer, CharsRef toAnalyze, String field, TokenConsumer consumer) throws IOException 
{
    stream.reset();
    consumer.reset(stream);
    int numTokens = 0;
    while (stream.incrementToken()) {
        consumer.nextToken();
        numTokens++;
    }
    consumer.end();
    return numTokens;
}