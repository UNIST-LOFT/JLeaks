  public static String[] getServerStats(String server, int timeout)
  throws IOException {
    String[] sp = server.split(":");
    if (sp == null || sp.length == 0) {
      return null;
    }

    String host = sp[0];
    int port = sp.length > 1 ? Integer.parseInt(sp[1])
        : HConstants.DEFAULT_ZOOKEPER_CLIENT_PORT;

    Socket socket = new Socket();
    InetSocketAddress sockAddr = new InetSocketAddress(host, port);
    socket.connect(sockAddr, timeout);

    socket.setSoTimeout(timeout);
    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader in = new BufferedReader(new InputStreamReader(
      socket.getInputStream()));
    out.println("stat");
    out.flush();
    ArrayList<String> res = new ArrayList<String>();
    while (true) {
      String line = in.readLine();
      if (line != null) {
        res.add(line);
      } else {
        break;
      }
    }
    socket.close();
    return res.toArray(new String[res.size()]);
  }
