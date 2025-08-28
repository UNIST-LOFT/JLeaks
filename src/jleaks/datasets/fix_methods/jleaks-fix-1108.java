public Void run() throws YarnException, IOException {
            ServiceClient sc = getServiceClient();
            try {
              sc.init(YARN_CONFIG);
              sc.start();
              sc.actionBuild(service);
            } finally {
              sc.close();
            }
            return null;
          }