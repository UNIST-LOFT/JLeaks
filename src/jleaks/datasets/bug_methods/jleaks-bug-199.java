  public static void main(String[] args)
      throws Exception {
    if (args.length != 3 && args.length != 4) {
      System.err.println("3 or 4 arguments required: QUERY_DIR, RESOURCE_URL, NUM_CLIENTS, TEST_TIME (seconds).");
      return;
    }

    File queryDir = new File(args[0]);
    String resourceUrl = args[1];
    final int numClients = Integer.parseInt(args[2]);
    final long endTime;
    if (args.length == 3) {
      endTime = Long.MAX_VALUE;
    } else {
      endTime = System.currentTimeMillis() + Integer.parseInt(args[3]) * MILLIS_PER_SECOND;
    }

    File[] queryFiles = queryDir.listFiles();
    assert queryFiles != null;
    Arrays.sort(queryFiles);

    final int numQueries = queryFiles.length;
    final HttpPost[] httpPosts = new HttpPost[numQueries];
    for (int i = 0; i < numQueries; i++) {
      HttpPost httpPost = new HttpPost(resourceUrl);
      String query = new BufferedReader(new FileReader(queryFiles[i])).readLine();
      httpPost.setEntity(new StringEntity("{\"pql\":\"" + query + "\"}"));
      httpPosts[i] = httpPost;
    }

    final AtomicInteger counter = new AtomicInteger(0);
    final AtomicLong totalResponseTime = new AtomicLong(0L);
    final ExecutorService executorService = Executors.newFixedThreadPool(numClients);

    for (int i = 0; i < numClients; i++) {
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            while (System.currentTimeMillis() < endTime) {
              long startTime = System.currentTimeMillis();
              CloseableHttpResponse httpResponse = httpClient.execute(httpPosts[RANDOM.nextInt(numQueries)]);
              httpResponse.close();
              long responseTime = System.currentTimeMillis() - startTime;
              counter.getAndIncrement();
              totalResponseTime.getAndAdd(responseTime);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }
    executorService.shutdown();

    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < endTime) {
      Thread.sleep(REPORT_INTERVAL_MILLIS);
      double timePassedSeconds = ((double) (System.currentTimeMillis() - startTime)) / MILLIS_PER_SECOND;
      int count = counter.get();
      double avgResponseTime = ((double) totalResponseTime.get()) / count;
      System.out.println(
          "Time Passed: " + timePassedSeconds + "s, Query Executed: " + count + ", QPS: " + count / timePassedSeconds
              + ", Avg Response Time: " + avgResponseTime + "ms");
    }
  }
