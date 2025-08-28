    public static String readAll(Reader reader) throws IOException {
        StringBuilder ret=new StringBuilder();
        BufferedReader buffered = new BufferedReader(reader);
        int c;
        while ((c=buffered.read())!=-1)
            ret.appendCodePoint(c);
        buffered.close();
        return ret.toString();
    }
