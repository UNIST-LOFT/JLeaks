  public Object evaluate(Tuple tuple) throws IOException {
    String value = tuple.getString(fieldName);
    if(value == null) {
      return null;
    }

    TokenStream tokenStream = analyzer.tokenStream(analyzerField, value);
    CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
    tokenStream.reset();
    List<String> tokens = new ArrayList();
    while (tokenStream.incrementToken()) {
      tokens.add(termAtt.toString());
    }

    tokenStream.end();
    tokenStream.close();

    return tokens;
  }
