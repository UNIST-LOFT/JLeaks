public static byte[] tryCompress(String str, String encoding) 
{
    if (str == null || str.length() == 0) {
        return new byte[0];
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
        gzip.write(str.getBytes(encoding));
    } catch (Exception e) {
        e.printStackTrace();
    }
    return out.toByteArray();
}