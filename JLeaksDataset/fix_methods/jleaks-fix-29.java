public static void writeBufferToFile(String path, byte[] buffer) throws IOException 
{
    FileOutputStream os = new FileOutputStream(path);
    try {
        os.write(buffer);
    } finally {
        os.close();
    }
}