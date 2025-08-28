public static byte[] readFileFromSDCard(String filePath, String fileName) 
{
    byte[] buffer = null;
    FileInputStream fin = null;
    try {
        if (isSDCardEnable()) {
            String filePaht = filePath + "/" + fileName;
            fin = new FileInputStream(filePaht);
            int length = fin.available();
            buffer = new byte[length];
            fin.read(buffer);
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (fin != null)
                fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    return buffer;
}