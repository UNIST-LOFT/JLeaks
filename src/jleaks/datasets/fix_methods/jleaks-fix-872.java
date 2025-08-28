public boolean transmit(File targetFile, HttpResponse response, Channel channel) 
{
    final RandomAccessFile raf;
    try {
        raf = new RandomAccessFile(targetFile, "r");
    } catch (FileNotFoundException fnfe) {
        throw new RuntimeException(fnfe);
    }
    long fileLength;
    try {
        fileLength = raf.length();
    } catch (IOException e) {
        closeQuietly(raf);
        throw new RuntimeException(e);
    }
    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fileLength);
    HttpHeaders.setDateHeader(response, HttpHeaders.Names.LAST_MODIFIED, new Date(targetFile.lastModified()));
    // Write the initial line and the header.
    if (!channel.isOpen()) {
        closeQuietly(raf);
        return false;
    }
    try {
        channel.write(response);
    } catch (Exception e) {
        closeQuietly(raf);
    }
    // Write the content.
    ChannelFuture writeFuture;
    ChunkedFile message = null;
    try {
        message = new ChunkedFile(raf, 0, fileLength, 8192);
        writeFuture = channel.write(message);
    } catch (Exception ignore) {
        if (channel.isOpen()) {
            channel.close();
        }
        if (message != null) {
            try {
                message.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
    final ChunkedFile finalMessage = message;
    writeFuture.addListener(new ChannelFutureListener() {

        public void operationComplete(ChannelFuture future) {
            try {
                finalMessage.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            future.addListener(ChannelFutureListener.CLOSE);
        }
    });
    return true;
}