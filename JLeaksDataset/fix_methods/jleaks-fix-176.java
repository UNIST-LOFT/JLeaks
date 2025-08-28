public void close() throws IOException 
{
    try {
        if (!closed) {
            closed = true;
            onClose(dir, this);
        }
    } finally {
        super.close();
    }
}