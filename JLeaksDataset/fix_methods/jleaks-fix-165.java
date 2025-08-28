public void close() throws IOException 
{
    // only close the file if it has not been closed yet
    if (isOpen) {
        boolean success = false;
        try {
            super.close();
            success = true;
        } finally {
            isOpen = false;
            if (!success) {
                try {
                    file.close();
                } catch (Throwable t) {
                    // Suppress so we don't mask original exception
                }
            } else
                file.close();
        }
    }
}