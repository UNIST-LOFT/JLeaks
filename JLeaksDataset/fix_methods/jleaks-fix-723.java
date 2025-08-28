private byte[] serialize(Serializable value) 
{
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        oos.writeObject(value);
        oos.flush();
        return bos.toByteArray();
    } catch (Exception e) {
        throw new CacheException("Error serializing object.  Cause: " + e, e);
    }
}