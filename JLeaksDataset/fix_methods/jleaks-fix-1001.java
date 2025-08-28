public static void main(final String[] args) throws Exception 
{
    handleCommandLineArgs(args);
    final File gameXmlFile = new FileOpen("Select xml file", mapFolderLocation, ".xml").getFile();
    if (gameXmlFile == null) {
        System.out.println("No file selected");
        return;
    }
    final InputStream source = XmlUpdater.class.getResourceAsStream("gameupdate.xslt");
    if (source == null) {
        throw new IllegalStateException("Could not find xslt file");
    }
    final Transformer trans = TransformerFactory.newInstance().newTransformer(new StreamSource(source));
    ByteArrayOutputStream resultBuf;
    try (final FileInputStream fileInputStream = new FileInputStream(gameXmlFile);
        final InputStream gameXmlStream = new BufferedInputStream(fileInputStream)) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        // use a dummy game.dtd, this prevents the xml parser from adding default values
        final URL url = XmlUpdater.class.getResource("");
        final String system = url.toExternalForm();
        final Source xmlSource = new StreamSource(gameXmlStream, system);
        resultBuf = new ByteArrayOutputStream();
        trans.transform(xmlSource, new StreamResult(resultBuf));
    }
    gameXmlFile.renameTo(new File(gameXmlFile.getAbsolutePath() + ".backup"));
    try (final FileOutputStream outStream = new FileOutputStream(gameXmlFile)) {
        outStream.write(resultBuf.toByteArray());
    }
    System.out.println("Successfully updated:" + gameXmlFile);
}