public final static void paddedCopy(
    Bucket from,
    Bucket to,
    long nBytes,
    int blockSize)
    throws IOException {
    if (nBytes > blockSize) {
        throw new IllegalArgumentException("nBytes > blockSize");
    }
    OutputStream out = null;
    InputStream in = null;
    try {
        out = to.getOutputStream();
        byte[] buffer = new byte[16384];
        in = from.getInputStream();
        long count = 0;
        while (count != nBytes) {
            long nRequired = nBytes - count;
            if (nRequired > buffer.length) {
                nRequired = buffer.length;
            }
            long nRead = in.read(buffer, 0, (int) nRequired);
            if (nRead == -1) {
                throw new IOException("Not enough data in source bucket.");
            }
            out.write(buffer, 0, (int) nRead);
            count += nRead;
        }
        if (count < blockSize) {
            // hmmm... better to just allocate a new buffer
            // instead of explicitly zeroing the old one?
            // Zero pad to blockSize
            long padLength = buffer.length;
            if (padLength > blockSize - nBytes) {
                padLength = blockSize - nBytes;
            }
            for (int i = 0; i < padLength; i++) {
                buffer[i] = 0;
            }
            while (count != blockSize) {
                long nRequired = blockSize - count;
                if (blockSize - count > buffer.length) {
                    nRequired = buffer.length;
                }
                out.write(buffer, 0, (int) nRequired);
                count += nRequired;
            }
        }
    } finally {
        if (in != null)
            in.close();
        if (out != null)
            out.close();
    }
}