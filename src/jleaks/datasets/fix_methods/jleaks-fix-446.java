public static void savePhoto(String path, byte[] data) 
{
    File tempFile = new File(path);
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write(data);
        fos.flush();
    } catch (IOException e) {
        Timber.e(e);
    }
}