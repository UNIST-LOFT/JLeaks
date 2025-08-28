    protected byte[] readFileRaw(File fp) throws IOException {
        if (!fp.exists()) return null;
        FileInputStream fis = new FileInputStream(fp);
        byte[] data = new byte[(int) fp.length()];
        fis.read(data);
        fis.close();
        return data;
    }
