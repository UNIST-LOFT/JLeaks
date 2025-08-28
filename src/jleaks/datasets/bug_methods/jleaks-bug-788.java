    public void writeConfiguration(InetAddress host, int port) throws IOException {
        Socket sock = new Socket(host, port);
        XMLEncoder e = new XMLEncoder(sock.getOutputStream());
        e.writeObject(threadConfig.get());
        e.close();
        IOUtils.close(sock);
    }
