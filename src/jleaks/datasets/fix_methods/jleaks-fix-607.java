private Hints parseHints(InputStream inputStream, String schema) throws HintParseException, SAXException 
{
    try (InputStream schemaStream = this.getClass().getClassLoader().getResourceAsStream(schema)) {
        final HintHandler handler = new HintHandler();
        final SAXParser saxParser = XmlUtils.buildSecureSaxParser(schemaStream);
        final XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setErrorHandler(new HintErrorHandler());
        xmlReader.setContentHandler(handler);
        try (Reader reader = new InputStreamReader(inputStream, "UTF-8")) {
            final InputSource in = new InputSource(reader);
            xmlReader.parse(in);
            final Hints hints = new Hints();
            hints.setHintRules(handler.getHintRules());
            hints.setVendorDuplicatingHintRules(handler.getVendorDuplicatingHintRules());
            return hints;
        }
    } catch (ParserConfigurationException | FileNotFoundException ex) {
        LOGGER.debug("", ex);
        throw new HintParseException(ex);
    } catch (SAXException ex) {
        if (ex.getMessage().contains("Cannot find the declaration of element 'hints'.")) {
            throw ex;
        } else {
            LOGGER.debug("", ex);
            throw new HintParseException(ex);
        }
    } catch (IOException ex) {
        LOGGER.debug("", ex);
        throw new HintParseException(ex);
    }
}