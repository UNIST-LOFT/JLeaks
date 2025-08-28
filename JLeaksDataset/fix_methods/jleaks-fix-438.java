public static void download(String source_url, File target_file) throws IOException 
{
    byte[] buffer = new byte[2048];
    ClientConnection connection = new ClientConnection(source_url);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int count;
    try {
        while ((count = connection.inputStream.read(buffer)) > 0) baos.write(buffer, 0, count);
    } catch (IOException e) {
    } finally {
        connection.close();
    }
    return baos.toByteArray();
}