public static void saveText(VirtualFile virtualFile, String text) throws IOException 
{
    OutputStream outputStream = virtualFile.getOutputStream(virtualFile);
    try {
        FileUtil.copy(new ByteArrayInputStream(text.getBytes(virtualFile.getCharset().name())), outputStream);
    } finally {
        outputStream.close();
    }
}