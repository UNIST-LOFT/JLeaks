  public synchronized void close() throws IOException {
    if (lockExists()) {
      try {
        lock.release();
      } finally {
        lock = null;
        try {
          channel.close();
        } finally {
          channel = null;
        }
      }
    } else {
      // if we don't hold the lock, and somebody still called release(), for
      // example as a result of calling IndexWriter.unlock(), we should attempt
      // to obtain the lock and release it. If the obtain fails, it means the
      // lock cannot be released, and we should throw a proper exception rather
      // than silently failing/not doing anything.
      boolean obtained = false;
      try {
        if (!(obtained = obtain())) {
          throw new LockReleaseFailedException(
              "Cannot forcefully unlock a NativeFSLock which is held by another indexer component: "
                  + path);
        }
      } finally {
        if (obtained) {
          close();
        }
      }
    }
  }
