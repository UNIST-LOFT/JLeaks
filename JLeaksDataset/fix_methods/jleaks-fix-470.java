public void close() throws IOException 
{
    try (MessageProtocolHandler protocolHandler = this.protocolHandler) {
        connection.close();
    }
}