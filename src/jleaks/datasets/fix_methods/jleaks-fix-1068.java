public static byte[] readBytes(File file) throws IOException 
{
    byte[] bytes = new byte[(int) file.length()];
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
        fileInputStream.read(bytes);
    }
    return bytes;
}