
    public HtmlDocument(File file) throws IOException {
        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root = null;
        InputStream is = new FileInputStream(file);
        try {
          root =  tidy.parseDOM(is, null);
        } finally {
          is.close();
        }
        rawDoc = root.getDocumentElement();
    }