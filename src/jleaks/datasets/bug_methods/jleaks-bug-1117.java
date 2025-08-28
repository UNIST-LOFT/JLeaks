    private static String getMode(String hostPort) throws NumberFormatException, UnknownHostException, IOException {
        String parts[] = hostPort.split(":");
        Socket s = new Socket(parts[0], Integer.parseInt(parts[1]));
        s.getOutputStream().write("stat".getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String line;
        while((line = br.readLine()) != null) {
            if (line.startsWith("Mode: ")) {
                return line.substring(6);
            }
        }
        return "unknown";
    }
