public void close() throws IOException 
{
    try {
        writeSerializationMappings(stateFile, serializer.getSerializationMappings());
    } catch (IOException e) {
        throw new RuntimeException("Unable to persist the state of this serializer", e);
    } finally {
        serializer.close();
    }
}