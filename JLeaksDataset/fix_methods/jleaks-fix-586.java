    private void checkPort135Access(PrintStream logger, String name, InetAddress host) throws IOException {
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(host,135),5000);
        } catch (IOException e) {
            logger.println("Failed to connect to port 135 of "+name+". Is Windows firewall blocking this port? Or did you disable DCOM service?");
            // again, let it continue.
        } finally {
            s.close();
        }
    }
