  public synchronized void close() throws IOException {
    if (lingerTime > 0) {
      boolean sleeping = true;
      while (sleeping) {
         try {
             wait(lingerTime * (long) 1000);
         } catch (InterruptedException e) {
         }
         sleeping = false;
      }
    }
    shutdownInput();
    shutdownOutput();
    inner.close();
  }
