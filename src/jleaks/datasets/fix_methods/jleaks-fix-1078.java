private static byte[] transform(final InputStream body) throws IOException 
{
    final ByteArrayOutputStream result = new ByteArrayOutputStream();
    final JsonReader reader = Json.createReader(body);
    final Map<String, ?> props = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
    final JsonWriterFactory factory = Json.createWriterFactory(props);
    final JsonWriter writer = factory.createWriter(result);
    try {
        final JsonObject obj = reader.readObject();
        try {
            writer.writeObject(obj);
        } finally {
            writer.close();
        }
    } catch (final JsonException ex) {
        throw new IOException(ex);
    } finally {
        reader.close();
    }
    return result.toByteArray();
}