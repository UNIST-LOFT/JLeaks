  public int doLogic() throws Exception {
    int res = 0;

    // open reader or use existing one
    IndexSearcher searcher = getRunData().getIndexSearcher();

    IndexReader reader;

    final boolean closeSearcher;
    if (searcher == null) {
      // open our own reader
      Directory dir = getRunData().getDirectory();
      reader = IndexReader.open(dir, true);
      searcher = new IndexSearcher(reader);
      closeSearcher = true;
    } else {
      // use existing one; this passes +1 ref to us
      reader = searcher.getIndexReader();
      closeSearcher = false;
    }

    // optionally warm and add num docs traversed to count
    if (withWarm()) {
      Document doc = null;
      for (int m = 0; m < reader.maxDoc(); m++) {
        if (!reader.isDeleted(m)) {
          doc = reader.document(m);
          res += (doc == null ? 0 : 1);
        }
      }
    }

    if (withSearch()) {
      res++;
      Query q = queryMaker.makeQuery();
      Sort sort = getSort();
      TopDocs hits;
      final int numHits = numHits();
      if (numHits > 0) {
        if (sort != null) {
          Weight w = q.weight(searcher);
          TopFieldCollector collector = TopFieldCollector.create(sort, numHits,
                                                                 true, withScore(),
                                                                 withMaxScore(),
                                                                 !w.scoresDocsOutOfOrder());
          searcher.search(w, null, collector);
          hits = collector.topDocs();
        } else {
          hits = searcher.search(q, numHits);
        }

        final String printHitsField = getRunData().getConfig().get("print.hits.field", null);
        if (printHitsField != null && printHitsField.length() > 0) {
          if (q instanceof MultiTermQuery) {
            System.out.println("MultiTermQuery term count = " + ((MultiTermQuery) q).getTotalNumberOfTerms());
          }
          System.out.println("totalHits = " + hits.totalHits);
          System.out.println("maxDoc()  = " + reader.maxDoc());
          System.out.println("numDocs() = " + reader.numDocs());
          for(int i=0;i<hits.scoreDocs.length;i++) {
            final int docID = hits.scoreDocs[i].doc;
            final Document doc = reader.document(docID);
            System.out.println("  " + i + ": doc=" + docID + " score=" + hits.scoreDocs[i].score + " " + printHitsField + " =" + doc.get(printHitsField));
          }
        }

        if (withTraverse()) {
          final ScoreDoc[] scoreDocs = hits.scoreDocs;
          int traversalSize = Math.min(scoreDocs.length, traversalSize());

          if (traversalSize > 0) {
            boolean retrieve = withRetrieve();
            int numHighlight = Math.min(numToHighlight(), scoreDocs.length);
            Analyzer analyzer = getRunData().getAnalyzer();
            BenchmarkHighlighter highlighter = null;
            if (numHighlight > 0) {
              highlighter = getBenchmarkHighlighter(q);
            }
            for (int m = 0; m < traversalSize; m++) {
              int id = scoreDocs[m].doc;
              res++;
              if (retrieve) {
                Document document = retrieveDoc(reader, id);
                res += document != null ? 1 : 0;
                if (numHighlight > 0 && m < numHighlight) {
                  Collection<String> fieldsToHighlight = getFieldsToHighlight(document);
                  for (final String field : fieldsToHighlight) {
                    String text = document.get(field);
                    res += highlighter.doHighlight(reader, id, field, document, analyzer, text);
                  }
                }
              }
            }
          }
        }
      }
    }

    if (closeSearcher) {
      searcher.close();
    } else {
      // Release our +1 ref from above
      reader.decRef();
    }
    return res;
  }
