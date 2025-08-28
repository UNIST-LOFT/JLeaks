byte[] readClassfile(String classname){
    InputStream fin = openClassfile(classname);
    byte[] b;
    try {
        b = readStream(fin);
    } finally {
        fin.close();
    }
    return b;
}