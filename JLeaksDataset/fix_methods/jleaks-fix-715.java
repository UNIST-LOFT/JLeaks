public void close() throws IOException{
    if (mConnection == null) {
        throw new IOException("Already closed");
    }
    synchronized (this) {
        if (mUsbRequest != null)
            mUsbRequest.cancel();
    }
    try {
        closeInt();
    } catch (Exception ignored) {
    }
    try {
        mConnection.close();
    } finally {
        mConnection = null;
    }
}