private void parseMultiPartData() throws IOException 
{
    Bucket part = (Bucket) this.parts.get(name);
    if (part == null)
        return "";
    if (part.size() > maxlength)
        return "";
    InputStream is = null;
    DataInputStream dis = null;
    try {
        is = part.getInputStream();
        dis = new DataInputStream(is);
        byte[] buf = new byte[is.available()];
        dis.readFully(buf);
        return new String(buf);
    } catch (IOException ioe) {
        Logger.error(this, "Caught IOE:" + ioe.getMessage());
    } finally {
        try {
            if (dis != null)
                dis.close();
            if (is != null)
                is.close();
        } catch (IOException ioe) {
        }
    }
    return "";
}