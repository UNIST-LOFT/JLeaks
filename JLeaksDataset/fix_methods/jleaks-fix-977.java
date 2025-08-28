public void close() throws IOException 
{
    sslEngine.closeOutbound();
    sslEngine.getSession().invalidate();
    try {
        if (socketChannel.isOpen()) {
            socketChannel.write(wrap(emptybuffer));
        }
    } finally {
        // in case socketChannel.write produce exception - channel will never close
        socketChannel.close();
    }
}