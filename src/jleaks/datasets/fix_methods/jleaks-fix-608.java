private List<SuppressionRule> parseSuppressionRules(InputStream inputStream, String schema) throws SuppressionParseException, SAXException 
{
    try (InputStream schemaStream = this.getClass().getClassLoader().getResourceAsStream(schema)) {
        final SuppressionHandler handler = new SuppressionHandler();
        final SAXParser saxParser = XmlUtils.buildSecureSaxParser(schemaStream);
        final XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setErrorHandler(new SuppressionErrorHandler());
        xmlReader.setContentHandler(handler);
        try (Reader reader = new InputStreamReader(inputStream, "UTF-8")) {
            final InputSource in = new InputSource(reader);
            xmlReader.parse(in);
            return handler.getSuppressionRules();
        }
    } catch (ParserConfigurationException | FileNotFoundException ex) {
        LOGGER.debug("", ex);
        throw new SuppressionParseException(ex);
    } catch (SAXException ex) {
        if (ex.getMessage().contains("Cannot find the declaration of element 'suppressions'.")) {
            throw ex;
        } else {
            LOGGER.debug("", ex);
            throw new SuppressionParseException(ex);
        }
    } catch (IOException ex) {
        LOGGER.debug("", ex);
        throw new SuppressionParseException(ex);
    }
}