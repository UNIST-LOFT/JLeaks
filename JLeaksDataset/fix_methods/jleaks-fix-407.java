public void close() throws IOException 
{
    try {
        super.close();
    } finally {
        out.close();
    }
}