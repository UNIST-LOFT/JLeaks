static public byte[] loadBytesRaw(File file) throws IOException 
{
    int size = (int) file.length();
    FileInputStream input = null;
    try {
        input = new FileInputStream(file);
        byte[] buffer = new byte[size];
        int offset = 0;
        int bytesRead;
        while ((bytesRead = input.read(buffer, offset, size - offset)) != -1) {
            offset += bytesRead;
            if (bytesRead == 0)
                break;
        }
        return buffer;
    } finally {
        if (input != null) {
            input.close();
        }
    }
}