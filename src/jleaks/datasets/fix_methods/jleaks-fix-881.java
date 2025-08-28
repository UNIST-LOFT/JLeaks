public void putFile(String path, byte[] data) throws IOException 
{
    try (InputStream is = url.openStream()) {
        putFile(path, is);
    }
}