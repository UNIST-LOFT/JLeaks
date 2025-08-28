private int readFromRemote(byte[] b, int off, int len) throws IOException 
{
    // We read at most len bytes, but if mPos + len exceeds the length of the block, we only
    // read up to the end of the block.
    int toRead = (int) Math.min(len, remaining());
    int bytesLeft = toRead;
    while (bytesLeft > 0) {
        // TODO(calvin): Fix needing to recreate reader each time.
        RemoteBlockReader reader = RemoteBlockReader.Factory.createRemoteBlockReader(ClientContext.getConf());
        try {
            ByteBuffer data = reader.readRemoteBlock(mLocation, mBlockId, getPosition(), bytesLeft);
            int bytesRead = data.remaining();
            data.get(b, off, bytesRead);
            bytesLeft -= bytesRead;
            incrementBytesReadMetric(bytesRead);
        } finally {
            reader.close();
        }
    }
    return toRead;
}