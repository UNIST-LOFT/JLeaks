    public void createIndex() {
        // Create new index by creating IndexWriter but not writing anything.
        try {
            IndexWriter indexWriter = new IndexWriter(directoryToIndex, new IndexWriterConfig(new EnglishStemAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));
            indexWriter.close();
        } catch (IOException e) {
            LOGGER.warn("Could not create new Index!", e);
        }
    }
