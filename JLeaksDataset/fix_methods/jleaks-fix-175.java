public synchronized void close() throws IOException 
{
    try {
        if (lock != null) {
            lock.release();
            lock = null;
        }
    } finally {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }
}