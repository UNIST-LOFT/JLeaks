private void dumpJStack() {
  long now = System.currentTimeMillis();
  //dump every 10 minutes
  if (now - lastPrintTime < 10 * 60 * 1000) {
      return;
  }
  if (!guard.tryAcquire()) {
      return;
  }

  ExecutorService pool = Executors.newSingleThreadExecutor();
  pool.execute(() -> {
      String dumpPath = url.getParameter(Constants.DUMP_DIRECTORY, System.getProperty("user.home"));

      SimpleDateFormat sdf;

      String os = System.getProperty("os.name").toLowerCase();

      // window system don't support ":" in file name
      if (os.contains("win")) {
          sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
      } else {
          sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
      }

      String dateStr = sdf.format(new Date());
      //try-with-resources
      try (FileOutputStream jStackStream = new FileOutputStream(new File(dumpPath, "Dubbo_JStack.log" + "." + dateStr))) {
          JVMUtil.jstack(jStackStream);
      } catch (Throwable t) {
          logger.error("dump jStack error", t);
      } finally {
          guard.release();
      }
      lastPrintTime = System.currentTimeMillis();
  });
  //must shutdown thread pool ,if not will lead to OOM
  pool.shutdown();

}