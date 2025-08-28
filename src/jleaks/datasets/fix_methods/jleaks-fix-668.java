public static void deleteMessages(long companyId, long threadId){
    BooleanQuery booleanQuery = new BooleanQuery();
    LuceneUtil.addRequiredTerm(booleanQuery, LuceneFields.PORTLET_ID, PORTLET_ID);
    LuceneUtil.addRequiredTerm(booleanQuery, "threadId", threadId);
    Searcher searcher = LuceneUtil.getSearcher(companyId);
    try {
        Hits hits = searcher.search(booleanQuery);
        if (hits.length() > 0) {
            IndexReader reader = null;
            try {
                LuceneUtil.acquireLock(companyId);
                reader = LuceneUtil.getReader(companyId);
                for (int i = 0; i < hits.length(); i++) {
                    Document doc = hits.doc(i);
                    Field field = doc.getField(LuceneFields.UID);
                    reader.deleteDocuments(new Term(LuceneFields.UID, field.stringValue()));
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
                LuceneUtil.releaseLock(companyId);
            }
        }
    } finally {
        LuceneUtil.closeSearcher(searcher);
    }
}