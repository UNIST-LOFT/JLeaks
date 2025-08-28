public void close() 
{
    this.scanner.close();
    try {
        this.htable.close();
    } catch (IOException ioe) {
        LOG.warn("Error closing table", ioe);
    }
}