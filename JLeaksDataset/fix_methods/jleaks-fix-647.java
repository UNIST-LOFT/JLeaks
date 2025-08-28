protected Object deserialize(byte[] in) 
{
    Object rv = null;
    ByteArrayInputStream bis = null;
    ObjectInputStream is = null;
    try {
        if (in != null) {
            bis = new ByteArrayInputStream(in);
            is = new ObjectInputStream(bis);
            rv = is.readObject();
        }
    } catch (IOException e) {
        log.error("Caught IOException decoding " + in.length + " bytes of data", e);
    } catch (ClassNotFoundException e) {
        log.error("Caught CNFE decoding " + in.length + " bytes of data", e);
    } finally {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
        if (bis != null) {
            try {
                bis.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    return rv;
}