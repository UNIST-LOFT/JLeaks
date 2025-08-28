public static byte[] read(File file) 
{
    byte[] bytes = new byte[(int) file.length()];
    try (InputStream is = new FileInputStream(file)) {
        is.read(bytes);
    } catch (IOException e) {
        Timber.e(e);
    }
    return bytes;
}