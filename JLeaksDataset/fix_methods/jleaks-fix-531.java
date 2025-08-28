public void doTransfer() throws IOException 
{
    if (!isActivatable()) {
        throw new IOException("Transfer missing party");
    }
    InputStream in = null;
    OutputStream out = null;
    try {
        in = getInputStream();
        out = new ProxyOutputStream(getOutputStream());
        final byte[] b = new byte[BUFFER_SIZE];
        int count = 0;
        amountWritten = 0;
        do {
            // write to the output stream
            out.write(b, 0, count);
            amountWritten += count;
            // read more bytes from the input stream
            count = in.read(b);
        } while (count >= 0);
    } finally {
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                Log.error(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}