public void run() 
{
    Thread.currentThread().setName("WebsocketWriteThread");
    try {
        try {
            while (!Thread.interrupted()) {
                ByteBuffer buffer = engine.outQueue.take();
                ostream.write(buffer.array(), 0, buffer.limit());
                ostream.flush();
            }
        } catch (InterruptedException e) {
            for (ByteBuffer buffer : engine.outQueue) {
                ostream.write(buffer.array(), 0, buffer.limit());
                ostream.flush();
            }
        }
    } catch (IOException e) {
        handleIOException(e);
    } finally {
        closeOutputAndSocket();
    }
}