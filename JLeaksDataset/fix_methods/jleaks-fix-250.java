  public static void main(String[] args) throws Exception {
    Path configPath = Paths.get(args[0]);
    try (InputStream configInputStream = Files.newInputStream(configPath)) {
      BuildFarmServer server = new BuildFarmServer(toBuildFarmServerConfig(configInputStream));
      configInputStream.close();
      server.start();
      server.blockUntilShutdown();
    }
  }
