private Persistence getPersistence() 
{
    Persistence persistence = null;
    String persistenceXmlLocation = context.getPersistenceXmlLocation();
    final InputStream stream = xmlParserHelper.getInputStreamForResource(persistenceXmlLocation);
    if (stream == null) {
        return null;
    }
    try {
        Schema schema = xmlParserHelper.getSchema(PERSISTENCE_SCHEMA);
        persistence = xmlParserHelper.getJaxbRoot(stream, Persistence.class, schema);
    } catch (XmlParsingException e) {
        context.logMessage(Diagnostic.Kind.WARNING, "Unable to parse persistence.xml: " + e.getMessage());
    } finally {
        try {
            stream.close();
        } catch (IOException e) {
            // eat it
        }
    }
    return persistence;
}