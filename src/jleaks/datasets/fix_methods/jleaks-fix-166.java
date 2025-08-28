public void close() throws IOException 
{
    // only close the file if it has not been closed yet
    if (isOpen) {
        try {
            super.close();
            if (doSync)
                file.getFD().sync();
        } finally {
            file.close();
            isOpen = false;
        }
    }
}