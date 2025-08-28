private void buildIndex(CveDB cve) throws IndexException 
{
    try (Analyzer analyzer = createSearchingAnalyzer();
        IndexWriter indexWriter = new IndexWriter(index, new IndexWriterConfig(LuceneUtils.CURRENT_VERSION, analyzer))) {
        // Tip: reuse the Document and Fields for performance...
        // See "Re-use Document and Field instances" from
        // http://wiki.apache.org/lucene-java/ImproveIndexingSpeed
        final Document doc = new Document();
        final Field v = new TextField(Fields.VENDOR, Fields.VENDOR, Field.Store.YES);
        final Field p = new TextField(Fields.PRODUCT, Fields.PRODUCT, Field.Store.YES);
        doc.add(v);
        doc.add(p);
        final Set<Pair<String, String>> data = cve.getVendorProductList();
        for (Pair<String, String> pair : data) {
            if (pair.getLeft() != null && pair.getRight() != null) {
                v.setStringValue(pair.getLeft());
                p.setStringValue(pair.getRight());
                indexWriter.addDocument(doc);
                resetFieldAnalyzer();
            }
        }
        indexWriter.commit();
        indexWriter.close(true);
    } catch (DatabaseException ex) {
        LOGGER.debug("", ex);
        throw new IndexException("Error reading CPE data", ex);
    } catch (CorruptIndexException ex) {
        throw new IndexException("Unable to close an in-memory index", ex);
    } catch (IOException ex) {
        throw new IndexException("Unable to close an in-memory index", ex);
    }
}