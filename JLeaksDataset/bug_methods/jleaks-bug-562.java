    public static byte[] fetchClass(String host, int port,
                                    String directory, String classname)
        throws IOException
    {
        byte[] b;
        URLConnection con = fetchClass0(host, port,
                directory + classname.replace('.', '/') + ".class");
        int size = con.getContentLength();
        InputStream s = con.getInputStream();
        if (size <= 0)
            b = ClassPoolTail.readStream(s);
        else {
            b = new byte[size];
            int len = 0;
            do {
                int n = s.read(b, len, size - len);
                if (n < 0) {
                    s.close();
                    throw new IOException("the stream was closed: "
                                          + classname);
                }
                len += n;
            } while (len < size);
        }

        s.close();
        return b;
    }
