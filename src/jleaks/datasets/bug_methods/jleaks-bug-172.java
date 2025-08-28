    public HtmlDocument(File file) throws IOException {
        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root =
                tidy.parseDOM(new FileInputStream(file), null);
        rawDoc = root.getDocumentElement();
    }
