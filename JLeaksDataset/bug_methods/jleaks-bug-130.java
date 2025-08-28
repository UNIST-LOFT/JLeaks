  public void stop() {
    if (tomActivated){
      this.timeoutMonitor.interrupt();
      this.timerUpdater.interrupt();
    }
  }
