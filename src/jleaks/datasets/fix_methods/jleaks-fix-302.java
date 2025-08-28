public void close() throws IOException 
{
    if (!myClosed) {
        myClosed = true;
        try {
            flush();
        } finally {
            myStorage.close();
        }
    }
}