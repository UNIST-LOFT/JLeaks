    public static String loadFile(File logfile,Charset charset) throws IOException {
        if(!logfile.exists())
            return "";

        StringBuilder str = new StringBuilder((int)logfile.length());

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(logfile),charset));
        char[] buf = new char[1024];
        int len;
        while((len=r.read(buf,0,buf.length))>0)
           str.append(buf,0,len);
        r.close();

        return str.toString();
    }
