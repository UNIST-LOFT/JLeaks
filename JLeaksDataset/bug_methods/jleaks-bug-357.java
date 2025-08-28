    private byte[] getBytes(ZipEntry ze) throws IOException {
        byte[] b = new byte[(int)ze.getSize()];
        DataInputStream is = new DataInputStream(super.getInputStream(ze));
        is.readFully(b, 0, b.length);
        is.close();
        return b;
    }
