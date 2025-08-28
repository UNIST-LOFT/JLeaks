public RestCfg(InputStream io) throws IOException {
     try {
       cfg.load(io);
       extractEndpoints();
       extractCredentials();
     } finally {
       io.close();
     }
   }