  public static void main(String[] args) throws Exception {
    Path configPath = Paths.get(args[0]);
    InputStream configInputStream = Files.newInputStream(configPath);
    BuildFarmServer server = new BuildFarmServer(toBuildFarmServerConfig(configInputStream));
    server.start();
    server.blockUntilShutdown();
  }
