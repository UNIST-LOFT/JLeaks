public static void copy(String src, File dst) throws IOException 
{
    InputStream in = null;
    OutputStream out = null;
    try {
        in = FileCopier.class.getResourceAsStream(src);
        out = new FileOutputStream(dst);
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    } finally {
        if (in != null)
            in.close();
        if (out != null)
            out.close();
    }
}