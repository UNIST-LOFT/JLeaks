  public static void saveText(VirtualFile virtualFile, String text) throws IOException {
    OutputStream outputStream = virtualFile.getOutputStream(virtualFile);
    FileUtil.copy(new ByteArrayInputStream(text.getBytes(virtualFile.getCharset().name())), outputStream);
    outputStream.close();
  }
