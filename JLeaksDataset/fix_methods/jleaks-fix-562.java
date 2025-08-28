public static byte[] fetchClass(String host, int port,
String directory, String classname)
throws IOException
{
    byte[] b;
    URLConnection con = fetchClass0(host, port, directory + classname.replace('.', '/') + ".class");
    int size = con.getContentLength();
    InputStream s = con.getInputStream();
    try {
        if (size <= 0)
            b = ClassPoolTail.readStream(s);
        else {
            b = new byte[size];
            int len = 0;
            do {
                int n = s.read(b, len, size - len);
                if (n < 0)
                    throw new IOException("the stream was closed: " + classname);
                len += n;
            } while (len < size);
        }
    } finally {
        s.close();
    }
    return b;
}