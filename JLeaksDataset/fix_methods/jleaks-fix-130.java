public void stop() {
    shutdown(); // Stop executor service, etc
    if (tomActivated){
      this.timeoutMonitor.interrupt();
      this.timerUpdater.interrupt();
    }
  }