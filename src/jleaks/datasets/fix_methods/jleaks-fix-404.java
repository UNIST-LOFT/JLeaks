private Query newPossiblyAnalyzedQuery(String field, String termStr) 
{
    try (TokenStream source = getAnalyzer().tokenStream(field, termStr)) {
        source.reset();
        // Use the analyzer to get all the tokens, and then build a TermQuery,
        // PhraseQuery, or nothing based on the term count
        CachingTokenFilter buffer = new CachingTokenFilter(source);
        buffer.reset();
        TermToBytesRefAttribute termAtt = null;
        int numTokens = 0;
        boolean hasMoreTokens = false;
        termAtt = buffer.getAttribute(TermToBytesRefAttribute.class);
        if (termAtt != null) {
            try {
                hasMoreTokens = buffer.incrementToken();
                while (hasMoreTokens) {
                    numTokens++;
                    hasMoreTokens = buffer.incrementToken();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        // rewind buffer
        buffer.reset();
        BytesRef bytes = termAtt == null ? null : termAtt.getBytesRef();
        if (numTokens == 0) {
            return null;
        } else if (numTokens == 1) {
            try {
                boolean hasNext = buffer.incrementToken();
                assert hasNext == true;
                termAtt.fillBytesRef();
            } catch (IOException e) {
                // safe to ignore, because we know the number of tokens
            }
            return new PrefixQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
        } else {
            BooleanQuery bq = new BooleanQuery();
            for (int i = 0; i < numTokens; i++) {
                try {
                    boolean hasNext = buffer.incrementToken();
                    assert hasNext == true;
                    termAtt.fillBytesRef();
                } catch (IOException e) {
                    // safe to ignore, because we know the number of tokens
                }
                bq.add(new BooleanClause(new PrefixQuery(new Term(field, BytesRef.deepCopyOf(bytes))), BooleanClause.Occur.SHOULD));
            }
            return bq;
        }
    } catch (IOException e) {
        // Bail on any exceptions, going with a regular prefix query
        return new PrefixQuery(new Term(field, termStr));
    }
}