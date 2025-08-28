public void encode(final Object object, OutputStream outputStream) 
{
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    encode(object, bos);
    byte[] bytes = bos.toByteArray();
    bos.close();
    return bytes;
}