    byte[] readClassfile(String classname)
        throws NotFoundException, IOException
    {
        InputStream fin = openClassfile(classname);
        byte[] b = readStream(fin);
        fin.close();
        return b;
    }
