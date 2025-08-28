    public String loadAsString() throws IOException {
        long sizeGuess;
        if(file.exists())
            sizeGuess = file.length();
        else
        if(gz.exists())
            sizeGuess = gz.length()*2;
        else
            return "";

        StringBuilder str = new StringBuilder((int)sizeGuess);

        Reader r = new InputStreamReader(read());
        char[] buf = new char[8192];
        int len;
        while((len=r.read(buf,0,buf.length))>0)
           str.append(buf,0,len);
        r.close();

        return str.toString();
    }
