public void close() throws IOException 
{
    // Ensure that we close both fileOutErr and actionFileSystem even if one throws.
    try {
        fileOutErr.close();
    } finally {
        if (actionFileSystem instanceof Closeable) {
            ((Closeable) actionFileSystem).close();
        }
    }
}