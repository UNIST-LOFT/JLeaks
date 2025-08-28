public static byte[] serialize(Object obj) throws IOException 
{
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    try (ObjectOutputStream so = new ObjectOutputStream(bo)) {
        so.writeObject(obj);
        so.flush();
        return bo.toByteArray();
    }
}