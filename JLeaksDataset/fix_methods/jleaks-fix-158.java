public Object evaluate(Tuple tuple) throws IOException 
{
    String value = tuple.getString(fieldName);
    if (value == null) {
        return null;
    }
    List<String> tokens = new ArrayList();
    try (TokenStream tokenStream = analyzer.tokenStream(analyzerField, value)) {
        CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            tokens.add(termAtt.toString());
        }
        tokenStream.end();
    }
    return tokens;
}