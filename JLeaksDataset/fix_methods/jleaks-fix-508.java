public T fetch(byte[] routingKey, byte[] fullKey, boolean dontPromote, boolean canReadClientCache, boolean canReadSlashdotCache, boolean ignoreOldBlocks, BlockMetadata meta) throws IOException 
{
    ByteArrayWrapper key = new ByteArrayWrapper(routingKey);
    DiskBlock block;
    long timeAccessed;
    synchronized (this) {
        block = blocksByRoutingKey.get(key);
        if (block == null) {
            misses++;
            return null;
        }
        timeAccessed = block.lastAccessed;
    }
    byte[] fk = new byte[fullKeySize];
    byte[] header = new byte[headerSize];
    byte[] data = new byte[dataSize];
    InputStream in = block.data.getInputStream();
    try {
        DataInputStream dis = new DataInputStream(in);
        dis.readFully(fk);
        dis.readFully(header);
        dis.readFully(data);
    } finally {
        in.close();
    }
    try {
        T ret = callback.construct(data, header, routingKey, fk, canReadClientCache, canReadSlashdotCache, null, null);
        synchronized (this) {
            hits++;
            if (!dontPromote) {
                block.lastAccessed = System.currentTimeMillis();
                blocksByRoutingKey.push(key, block);
            }
        }
        if (logDEBUG)
            Logger.debug(this, "Block was last accessed " + (System.currentTimeMillis() - timeAccessed) + "ms ago");
        return ret;
    } catch (KeyVerifyException e) {
        block.data.free();
        synchronized (this) {
            blocksByRoutingKey.removeKey(key);
            misses++;
        }
        return null;
    }
}