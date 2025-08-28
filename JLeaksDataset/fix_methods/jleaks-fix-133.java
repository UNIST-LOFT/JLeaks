public void close() throws IOException 
{
    if (this.zkw != null) {
        this.zkw.close();
    }
    if (this.connection != null) {
        this.connection.close();
    }
}