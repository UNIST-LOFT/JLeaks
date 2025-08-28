private static int checkPortIsFree(int port) 
{
    try (ServerSocket socket = new ServerSocket()) {
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress("localhost", port));
        return socket.getLocalPort();
    } catch (IOException e) {
        return -1;
    }
}