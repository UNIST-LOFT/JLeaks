public void writeConfiguration(InetAddress host, int port) throws IOException 
{
    writeConfiguration(configServerSocket.getInetAddress(), configServerSocket.getLocalPort());
}