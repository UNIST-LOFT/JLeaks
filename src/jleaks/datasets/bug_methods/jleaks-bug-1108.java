          public Void run() throws YarnException, IOException {
            ServiceClient sc = getServiceClient();
            sc.init(YARN_CONFIG);
            sc.start();
            sc.actionBuild(service);
            sc.close();
            return null;
          }
