public void close() throws IOException 
{
    try {
        flush();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        MessagePacker messagePacker = getMessagePacker();
        messagePacker.close();
    }
}