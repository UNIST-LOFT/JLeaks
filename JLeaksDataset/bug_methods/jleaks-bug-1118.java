   public RestCfg(InputStream io) throws IOException {
       cfg.load(io);
       extractEndpoints();
       extractCredentials();
   }
