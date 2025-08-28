public long pipe(ClickHouseOutputStream output) throws IOException 
{
    long count = 0L;
    if (output == null || output.isClosed()) {
        return count;
    }
    ensureOpen();
    try {
        byte[] b = buffer;
        int l = limit;
        int p = position;
        int remain = l - p;
        if (remain > 0) {
            output.transferBytes(b, p, remain);
            count += remain;
            while ((remain = updateBuffer()) > 0) {
                b = buffer;
                output.transferBytes(b, 0, remain);
                count += remain;
            }
        }
    } finally {
        close();
    }
    return count;
}