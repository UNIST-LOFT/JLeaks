public void load() throws IOException 
{
    if (root != this) {
        root.load();
        return;
    }
    if (buffer != null)
        return;
    if (file == null) {
        throw new IllegalStateException("No file associated with this ByteBuffer!");
    }
    try (InputStream is = getInputStream();
        DataInputStream dis = new DataInputStream(is)) {
        buffer = new byte[(int) capacity()];
        offset = 0;
        dis.readFully(buffer);
    }
}