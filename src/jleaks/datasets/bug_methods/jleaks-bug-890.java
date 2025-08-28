  private static int checkPortIsFree(int port) {
    ServerSocket socket;
    try {
      socket = new ServerSocket();
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress("localhost", port));
      int localPort = socket.getLocalPort();
      socket.close();
      return localPort;
    } catch (IOException e) {
      return -1;
    }
  }
