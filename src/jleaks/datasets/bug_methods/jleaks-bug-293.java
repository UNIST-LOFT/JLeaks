private void dumpJStack() {
  long now = System.currentTimeMillis();
  //dump every 10 minutes
  if (now - lastPrintTime < 10 * 60 * 1000) {
      return;
  }
  if (!guard.tryAcquire()) {
      return;
  }

  Executors.newSingleThreadExecutor().execute(new Runnable() {
      @Override
      public void run() {
          String dumpPath = url.getParameter(Constants.DUMP_DIRECTORY, System.getProperty("user.home"));

          SimpleDateFormat sdf;

          String os = System.getProperty("os.name").toLowerCase();

          // window system don't support ":" in file name
          if(os.contains("win")){
              sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
          }else {
              sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
          }

          String dateStr = sdf.format(new Date());
          FileOutputStream jstackStream = null;
          try {
              jstackStream = new FileOutputStream(new File(dumpPath, "Dubbo_JStack.log" + "." + dateStr));
              JVMUtil.jstack(jstackStream);
          } catch (Throwable t) {
              logger.error("dump jstack error", t);
          } finally {
              guard.release();
              if (jstackStream != null) {
                  try {
                      jstackStream.flush();
                      jstackStream.close();
                  } catch (IOException e) {
                  }
              }
          }

          lastPrintTime = System.currentTimeMillis();
      }
  });

}