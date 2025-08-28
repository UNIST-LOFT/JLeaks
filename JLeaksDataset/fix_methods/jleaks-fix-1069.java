private Object deserialize(final String info) throws IOException, ClassNotFoundException 
{
    byte[] data = Base64.getDecoder().decode(info.trim());
    try (final Unmarshaller unmarshaller = factory.createUnmarshaller(configuration)) {
        unmarshaller.start(new ByteBufferInput(ByteBuffer.wrap(data)));
        return unmarshaller.readObject();
    }
}