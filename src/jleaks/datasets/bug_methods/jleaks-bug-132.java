  public void preShutdown() throws IOException {
    ObserverContext<MasterCoprocessorEnvironment> ctx = null;
    for (MasterEnvironment env: coprocessors) {
      if (env.getInstance() instanceof MasterObserver) {
        ctx = ObserverContext.createAndPrepare(env, ctx);
        Thread currentThread = Thread.currentThread();
        ClassLoader cl = currentThread.getContextClassLoader();
        try {
          currentThread.setContextClassLoader(env.getClassLoader());
          ((MasterObserver)env.getInstance()).preShutdown(ctx);
        } catch (Throwable e) {
          handleCoprocessorThrowable(env, e);
        } finally {
          currentThread.setContextClassLoader(cl);
        }
        if (ctx.shouldComplete()) {
          break;
        }
      }
    }
  }