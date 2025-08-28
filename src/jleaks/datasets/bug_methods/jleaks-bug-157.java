    public CharsRef analyze(String text, CharsRef reuse) throws IOException {
      TokenStream ts = analyzer.tokenStream("", text);
      CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
      PositionIncrementAttribute posIncAtt = ts.addAttribute(PositionIncrementAttribute.class);
      ts.reset();
      reuse.length = 0;
      while (ts.incrementToken()) {
        int length = termAtt.length();
        if (length == 0) {
          throw new IllegalArgumentException("term: " + text + " analyzed to a zero-length token");
        }
        if (posIncAtt.getPositionIncrement() != 1) {
          throw new IllegalArgumentException("term: " + text + " analyzed to a token with posinc != 1");
        }
        reuse.grow(reuse.length + length + 1); /* current + word + separator */
        int end = reuse.offset + reuse.length;
        if (reuse.length > 0) {
          reuse.chars[end++] = SynonymMap.WORD_SEPARATOR;
          reuse.length++;
        }
        System.arraycopy(termAtt.buffer(), 0, reuse.chars, end, length);
        reuse.length += length;
      }
      ts.end();
      ts.close();
      if (reuse.length == 0) {
        throw new IllegalArgumentException("term: " + text + " was completely eliminated by analyzer");
      }
      return reuse;
    }
